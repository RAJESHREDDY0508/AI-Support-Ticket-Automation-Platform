package com.ticketplatform.ticketservice.event;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

/**
 * Event envelope: eventId, eventType, schemaVersion, occurredAt, correlationId, payload.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventEnvelope(
        UUID eventId,
        String eventType,
        String schemaVersion,
        Instant occurredAt,
        String correlationId,
        Object payload
) {}
