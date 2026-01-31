package com.ticketplatform.api.dto;

import com.ticketplatform.api.enums.Category;
import com.ticketplatform.api.enums.Priority;
import jakarta.validation.constraints.*;

import java.io.Serializable;

/**
 * Request body for creating a new support ticket.
 */
public record CreateTicketRequest(
        @NotBlank(message = "Subject is required")
        @Size(max = 200)
        String subject,

        @NotBlank(message = "Description is required")
        @Size(max = 5000)
        String description,

        @NotNull(message = "Category is required")
        Category category,

        @NotNull(message = "Priority is required")
        Priority priority,

        @NotBlank(message = "Requester email is required")
        @Email
        @Size(max = 255)
        String requesterEmail
) implements Serializable {}
