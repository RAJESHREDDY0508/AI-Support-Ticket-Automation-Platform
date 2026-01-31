"""Prometheus metrics."""
from prometheus_client import Counter, Histogram, start_http_server

MESSAGES_CONSUMED = Counter(
    "ai_worker_messages_consumed_total",
    "Total messages consumed from Kafka",
    ["topic"],
)
MESSAGES_PROCESSED = Counter(
    "ai_worker_messages_processed_total",
    "Total messages processed successfully",
    ["topic"],
)
MESSAGES_FAILED = Counter(
    "ai_worker_messages_failed_total",
    "Total messages that failed processing (sent to DLQ)",
    ["topic"],
)
MESSAGES_SENT_TO_DLQ = Counter(
    "ai_worker_messages_sent_to_dlq_total",
    "Total messages published to DLQ",
)
PROCESSING_DURATION = Histogram(
    "ai_worker_processing_duration_seconds",
    "Time spent processing a message",
    ["topic"],
    buckets=(0.01, 0.05, 0.1, 0.25, 0.5, 1.0, 2.5, 5.0),
)


def start_metrics_server(port: int = 9090) -> None:
    """Start HTTP server for Prometheus scraping."""
    start_http_server(port)
