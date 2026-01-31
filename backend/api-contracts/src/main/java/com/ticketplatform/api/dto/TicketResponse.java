package com.ticketplatform.api.dto;

import com.ticketplatform.api.enums.Category;
import com.ticketplatform.api.enums.Priority;
import com.ticketplatform.api.enums.Status;

import java.io.Serializable;
import java.time.Instant;

/**
 * Response body for a single ticket (get by id).
 */
public record TicketResponse(
        String id,
        String subject,
        String description,
        Category category,
        Priority priority,
        Status status,
        String requesterEmail,
        Instant createdAt,
        Instant updatedAt
) implements Serializable {}
