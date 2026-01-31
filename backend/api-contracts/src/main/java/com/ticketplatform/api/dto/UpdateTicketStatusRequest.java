package com.ticketplatform.api.dto;

import com.ticketplatform.api.enums.Status;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * Request body for updating ticket status (admin).
 */
public record UpdateTicketStatusRequest(
        @NotNull(message = "Status is required")
        Status status
) implements Serializable {}
