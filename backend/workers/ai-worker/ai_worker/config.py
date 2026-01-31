"""Configuration from environment."""
import os

KAFKA_BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:29092")
KAFKA_GROUP_ID = os.getenv("KAFKA_GROUP_ID", "ai-worker-v1")

TOPIC_TICKET_CREATED = os.getenv("TOPIC_TICKET_CREATED", "ticket.created.v1")
TOPIC_DLQ = os.getenv("TOPIC_DLQ", "ticket.dlq.v1")

# Retry (tenacity)
RETRY_MIN_WAIT_SEC = float(os.getenv("RETRY_MIN_WAIT_SEC", "1"))
RETRY_MAX_WAIT_SEC = float(os.getenv("RETRY_MAX_WAIT_SEC", "60"))
RETRY_MAX_ATTEMPTS = int(os.getenv("RETRY_MAX_ATTEMPTS", "5"))

# Ticket service (for PUT /internal/tickets/{id}/ai-suggestion)
TICKET_SERVICE_URL = os.getenv("TICKET_SERVICE_URL", "http://ticket-service:8081")
INTERNAL_API_TOKEN = os.getenv("INTERNAL_API_TOKEN", "")

# Metrics
METRICS_PORT = int(os.getenv("METRICS_PORT", "9090"))
