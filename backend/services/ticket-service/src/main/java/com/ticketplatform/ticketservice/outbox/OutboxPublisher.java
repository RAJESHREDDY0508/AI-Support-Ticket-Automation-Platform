package com.ticketplatform.ticketservice.outbox;

import com.ticketplatform.ticketservice.entity.OutboxEntry;
import com.ticketplatform.ticketservice.event.OutboxEvent;
import com.ticketplatform.ticketservice.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Background publisher: polls outbox for unpublished events and publishes to Kafka.
 * On failure after max retries, publishes to DLQ and marks as published so processing continues.
 */
@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    @Value("${app.outbox.batch-size:50}")
    private int batchSize;

    @Value("${app.outbox.max-retries:5}")
    private int maxRetries;

    private final OutboxRepository outboxRepository;
    private final OutboxPublisherHelper publisherHelper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(OutboxRepository outboxRepository,
                           OutboxPublisherHelper publisherHelper,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.publisherHelper = publisherHelper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:2000}")
    public void publishPending() {
        List<OutboxEntry> entries = outboxRepository.findNextUnpublished(batchSize);
        for (OutboxEntry entry : entries) {
            try {
                publishOne(entry);
            } catch (Exception e) {
                log.warn("Outbox publish failed for entry {}: {}", entry.getId(), e.getMessage());
            }
        }
    }

    private void publishOne(OutboxEntry entry) {
        if (entry.getRetryCount() >= maxRetries) {
            sendToDlq(entry);
            publisherHelper.markPublished(entry.getId(), Instant.now());
            return;
        }

        String topic = entry.getTopic();
        String payload = entry.getPayload();
        String key = entry.getEventId().toString();

        try {
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, payload);
            future.get(10, java.util.concurrent.TimeUnit.SECONDS);
            publisherHelper.markPublished(entry.getId(), Instant.now());
            log.debug("Published outbox event {} to {}", entry.getEventId(), topic);
        } catch (Exception e) {
            publisherHelper.recordFailure(entry.getId(), e.getMessage());
            log.debug("Recorded failure for outbox entry {}: {}", entry.getId(), e.getMessage());
        }
    }

    private void sendToDlq(OutboxEntry entry) {
        try {
            kafkaTemplate.send(OutboxEvent.TOPIC_DLQ, entry.getEventId().toString(), entry.getPayload())
                    .get(5, java.util.concurrent.TimeUnit.SECONDS);
            log.info("Sent failed outbox entry {} to DLQ after {} retries", entry.getId(), entry.getRetryCount());
        } catch (Exception e) {
            log.error("Failed to send outbox entry {} to DLQ: {}", entry.getId(), e.getMessage());
        }
    }
}
