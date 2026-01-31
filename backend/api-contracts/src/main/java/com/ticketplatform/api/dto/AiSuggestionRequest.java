package com.ticketplatform.api.dto;

import com.ticketplatform.api.enums.Category;
import com.ticketplatform.api.enums.ConfidenceScore;
import com.ticketplatform.api.enums.Priority;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * Request body for PUT /tickets/{id}/ai-suggestion (internal - from Python worker).
 */
public record AiSuggestionRequest(
        @NotNull Category suggestedCategory,
        @NotNull Priority suggestedPriority,
        String suggestedResponse,
        @NotNull ConfidenceScore confidenceScore,
        Integer confidencePercent,
        String reasoning
) implements Serializable {}
