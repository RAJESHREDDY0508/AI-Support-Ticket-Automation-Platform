package com.ticketplatform.ticketservice.event;

import com.ticketplatform.api.enums.Category;
import com.ticketplatform.api.enums.ConfidenceScore;
import com.ticketplatform.api.enums.Priority;

import java.util.UUID;

public record TicketAiProcessedPayload(
        UUID ticketId,
        UUID suggestionId,
        Category suggestedCategory,
        Priority suggestedPriority,
        String suggestedResponse,
        ConfidenceScore confidenceScore,
        Integer confidencePercent
) {}
