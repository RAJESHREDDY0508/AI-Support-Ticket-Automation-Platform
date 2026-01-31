package com.ticketplatform.api.dto;

import com.ticketplatform.api.enums.Category;
import com.ticketplatform.api.enums.ConfidenceScore;
import com.ticketplatform.api.enums.Priority;

import java.io.Serializable;

/**
 * AI-generated suggestion for a ticket (category, priority, draft response).
 */
public record AiSuggestionResponse(
        Category suggestedCategory,
        Priority suggestedPriority,
        String suggestedResponse,
        ConfidenceScore confidenceScore,
        Integer confidencePercent,
        String reasoning
) implements Serializable {}
