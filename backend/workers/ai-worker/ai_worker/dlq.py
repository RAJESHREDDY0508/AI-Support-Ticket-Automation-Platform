"""Publish failed messages to DLQ."""
import json
from typing import Any

from aiokafka import AIOKafkaProducer

from ai_worker.config import KAFKA_BOOTSTRAP_SERVERS, TOPIC_DLQ
from ai_worker.log_config import get_logger
from ai_worker.metrics import MESSAGES_SENT_TO_DLQ

logger = get_logger(__name__)


async def publish_to_dlq(
    producer: AIOKafkaProducer,
    raw_value: bytes,
    key: bytes | None,
    reason: str,
    extra: dict[str, Any] | None = None,
) -> None:
    """Send message to DLQ. Optionally wrap in envelope with failure reason."""
    # DLQ receives same payload as original (for replay); we log the reason
    await producer.send_and_wait(TOPIC_DLQ, value=raw_value, key=key)
    MESSAGES_SENT_TO_DLQ.inc()
    logger.warning(
        "message_sent_to_dlq",
        topic_dlq=TOPIC_DLQ,
        reason=reason,
        key=key.decode("utf-8") if key else None,
        **(extra or {}),
    )
