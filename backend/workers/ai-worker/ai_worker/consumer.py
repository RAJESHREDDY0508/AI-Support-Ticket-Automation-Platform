"""Kafka consumer with retry and DLQ on failure."""
import asyncio

from aiokafka import AIOKafkaConsumer, AIOKafkaProducer
from tenacity import (
    retry,
    retry_if_exception_type,
    stop_after_attempt,
    wait_exponential,
)

from ai_worker.config import (
    KAFKA_BOOTSTRAP_SERVERS,
    KAFKA_GROUP_ID,
    RETRY_MAX_ATTEMPTS,
    RETRY_MAX_WAIT_SEC,
    RETRY_MIN_WAIT_SEC,
    TOPIC_TICKET_CREATED,
)
from ai_worker.dlq import publish_to_dlq
from ai_worker.handler import parse_envelope, process_ticket_created
from ai_worker.log_config import get_logger
from ai_worker.metrics import MESSAGES_CONSUMED, MESSAGES_FAILED, MESSAGES_PROCESSED, PROCESSING_DURATION

logger = get_logger(__name__)


@retry(
    retry=retry_if_exception_type((Exception,)),
    stop=stop_after_attempt(RETRY_MAX_ATTEMPTS),
    wait=wait_exponential(multiplier=1, min=RETRY_MIN_WAIT_SEC, max=RETRY_MAX_WAIT_SEC),
    reraise=True,
)
async def process_message(value: bytes, key: bytes | None, producer: AIOKafkaProducer) -> None:
    """Process one message with tenacity retry. On final failure, send to DLQ."""
    envelope = parse_envelope(value)
    event_type = envelope.get("eventType", "unknown")
    topic = TOPIC_TICKET_CREATED

    with PROCESSING_DURATION.labels(topic=topic).time():
        await asyncio.to_thread(process_ticket_created, envelope)

    MESSAGES_PROCESSED.labels(topic=topic).inc()


async def consume_loop() -> None:
    """Consume ticket.created.v1 and process; on failure after retries, publish to DLQ."""
    consumer = AIOKafkaConsumer(
        TOPIC_TICKET_CREATED,
        bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
        group_id=KAFKA_GROUP_ID,
        auto_offset_reset="earliest",
        value_deserializer=lambda x: x,
        key_deserializer=lambda x: x,
    )
    producer = AIOKafkaProducer(
        bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
        value_serializer=lambda v: v if isinstance(v, bytes) else v.encode("utf-8") if isinstance(v, str) else v,
        key_serializer=lambda k: k if isinstance(k, bytes) else (k or b"").encode("utf-8") if k else b"",
    )

    await consumer.start()
    await producer.start()

    try:
        logger.info("consumer_started", topic=TOPIC_TICKET_CREATED, group_id=KAFKA_GROUP_ID)
        async for msg in consumer:
            MESSAGES_CONSUMED.labels(topic=msg.topic).inc()
            try:
                await process_message(msg.value, msg.key, producer)
            except Exception as e:
                MESSAGES_FAILED.labels(topic=msg.topic).inc()
                logger.exception(
                    "message_processing_failed_sending_to_dlq",
                    topic=msg.topic,
                    partition=msg.partition,
                    offset=msg.offset,
                    error=str(e),
                )
                await publish_to_dlq(
                    producer,
                    raw_value=msg.value,
                    key=msg.key,
                    reason=str(e),
                    extra={"partition": msg.partition, "offset": msg.offset},
                )
    finally:
        await consumer.stop()
        await producer.stop()

