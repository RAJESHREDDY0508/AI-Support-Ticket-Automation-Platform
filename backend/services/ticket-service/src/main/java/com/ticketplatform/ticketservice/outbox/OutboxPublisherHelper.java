package com.ticketplatform.ticketservice.outbox;

import com.ticketplatform.ticketservice.repository.OutboxRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Commits outbox updates in separate transactions so partial progress is persisted.
 */
@Component
public class OutboxPublisherHelper {

    private final OutboxRepository outboxRepository;

    public OutboxPublisherHelper(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPublished(UUID id, Instant publishedAt) {
        outboxRepository.markPublished(id, publishedAt);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(UUID id, String errorMessage) {
        outboxRepository.recordFailure(id, errorMessage);
    }
}
