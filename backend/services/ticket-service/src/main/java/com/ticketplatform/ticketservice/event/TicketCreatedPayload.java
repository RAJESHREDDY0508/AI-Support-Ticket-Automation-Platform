package com.ticketplatform.ticketservice.event;

import com.ticketplatform.api.enums.Category;
import com.ticketplatform.api.enums.Priority;
import com.ticketplatform.api.enums.Status;

import java.time.Instant;
import java.util.UUID;

public record TicketCreatedPayload(
        UUID ticketId,
        String subject,
        String description,
        Category category,
        Priority priority,
        Status status,
        String requesterEmail,
        Instant createdAt
) {}
