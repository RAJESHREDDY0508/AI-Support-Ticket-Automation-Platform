"""Handle ticket.created.v1: call ticket-service PUT /internal/tickets/{id}/ai-suggestion."""
import json
from typing import Any

import httpx
from tenacity import (
    retry,
    retry_if_exception_type,
    stop_after_attempt,
    wait_exponential,
)

from ai_worker.config import (
    INTERNAL_API_TOKEN,
    RETRY_MAX_WAIT_SEC,
    RETRY_MIN_WAIT_SEC,
    TICKET_SERVICE_URL,
)
from ai_worker.log_config import get_logger

logger = get_logger(__name__)

# Valid enum values for ticket-service API
CATEGORIES = {"BILLING", "TECHNICAL", "ACCOUNT", "GENERAL", "FEEDBACK", "OTHER"}
PRIORITIES = {"LOW", "MEDIUM", "HIGH", "URGENT"}
CONFIDENCE_SCORES = {"LOW", "MEDIUM", "HIGH", "VERY_HIGH"}


def parse_envelope(raw: bytes) -> dict[str, Any]:
    """Parse event envelope JSON."""
    return json.loads(raw.decode("utf-8"))


def _normalize_category(category: str | None) -> str:
    if category and str(category).upper() in CATEGORIES:
        return str(category).upper()
    return "GENERAL"


def _normalize_priority(priority: str | None) -> str:
    if priority and str(priority).upper() in PRIORITIES:
        return str(priority).upper()
    return "MEDIUM"


@retry(
    retry=retry_if_exception_type((httpx.HTTPError, httpx.HTTPStatusError)),
    stop=stop_after_attempt(3),
    wait=wait_exponential(multiplier=1, min=RETRY_MIN_WAIT_SEC, max=RETRY_MAX_WAIT_SEC),
    reraise=True,
)
def process_ticket_created(envelope: dict[str, Any]) -> None:
    """
    Process ticket.created.v1: call PUT /internal/tickets/{id}/ai-suggestion.
    Ticket-service saves suggestion, sets status to AI_PROCESSED, emits ticket.ai_processed.v1.
    """
    payload = envelope.get("payload") or {}
    ticket_id = payload.get("ticketId")
    subject = payload.get("subject", "")
    category = payload.get("category")
    priority = payload.get("priority")
    correlation_id = envelope.get("correlationId")

    if not ticket_id:
        raise ValueError("payload.ticketId is required")

    logger.info(
        "processing_ticket_created",
        ticket_id=ticket_id,
        subject=subject[:80] if subject else None,
        correlation_id=correlation_id,
        event_id=envelope.get("eventId"),
    )

    # Placeholder AI suggestion (same category/priority as ticket, generic response)
    body = {
        "suggestedCategory": _normalize_category(category),
        "suggestedPriority": _normalize_priority(priority),
        "suggestedResponse": f"AI-generated response for ticket: {subject[:100] if subject else 'No subject'}.",
        "confidenceScore": "MEDIUM",
        "confidencePercent": 50,
        "reasoning": "AI worker placeholder; replace with real LLM.",
    }

    url = f"{TICKET_SERVICE_URL.rstrip('/')}/internal/tickets/{ticket_id}/ai-suggestion"
    headers = {"Content-Type": "application/json"}
    if INTERNAL_API_TOKEN:
        headers["X-Internal-Token"] = INTERNAL_API_TOKEN

    with httpx.Client(timeout=30.0) as client:
        response = client.put(url, json=body, headers=headers)

    response.raise_for_status()

    logger.info(
        "ticket_created_processed",
        ticket_id=ticket_id,
        correlation_id=correlation_id,
        status="AI_PROCESSED",
    )
