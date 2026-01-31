package com.ticketplatform.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * Standard error response used across services (gateway, ticket-service).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp,
        List<FieldError> fieldErrors
) implements Serializable {
    public record FieldError(String field, String message) implements Serializable {}
}
