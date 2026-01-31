package com.ticketplatform.ticketservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketplatform.ticketservice.entity.OutboxEntry;
import com.ticketplatform.ticketservice.event.EventEnvelope;
import com.ticketplatform.ticketservice.repository.OutboxRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Appends events to the outbox table in the same transaction as the business operation.
 * Call from TicketService within the same @Transactional method.
 */
@Service
public class OutboxService {

    public static final String MDC_CORRELATION_ID = "correlationId";

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void append(String topic, String eventType, String schemaVersion, Object payload) {
        String correlationId = MDC.get(MDC_CORRELATION_ID);
        append(topic, eventType, schemaVersion, payload, correlationId);
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void append(String topic, String eventType, String schemaVersion, Object payload, String correlationId) {
        UUID eventId = UUID.randomUUID();
        Instant occurredAt = Instant.now();
        EventEnvelope envelope = new EventEnvelope(eventId, eventType, schemaVersion, occurredAt, correlationId, payload);
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize event payload", e);
        }

        OutboxEntry entry = new OutboxEntry();
        entry.setEventId(eventId);
        entry.setEventType(eventType);
        entry.setTopic(topic);
        entry.setSchemaVersion(schemaVersion);
        entry.setOccurredAt(occurredAt);
        entry.setCorrelationId(correlationId);
        entry.setPayload(payloadJson);
        outboxRepository.save(entry);
    }
}
