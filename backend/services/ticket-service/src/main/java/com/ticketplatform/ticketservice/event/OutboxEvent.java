package com.ticketplatform.ticketservice.event;

/**
 * Topic and event type constants for outbox.
 */
public final class OutboxEvent {

    public static final String TOPIC_TICKET_CREATED = "ticket.created.v1";
    public static final String TOPIC_TICKET_AI_PROCESSED = "ticket.ai_processed.v1";
    public static final String TOPIC_TICKET_RESPONSE_APPROVED = "ticket.response_approved.v1";
    public static final String TOPIC_DLQ = "ticket.dlq.v1";

    public static final String EVENT_TYPE_TICKET_CREATED = "ticket.created";
    public static final String EVENT_TYPE_TICKET_AI_PROCESSED = "ticket.ai_processed";
    public static final String EVENT_TYPE_TICKET_RESPONSE_APPROVED = "ticket.response_approved";

    public static final String SCHEMA_VERSION = "1";

    private OutboxEvent() {}
}
