package com.ticketplatform.ticketservice.exception;

import java.util.UUID;

public class AiSuggestionNotFoundException extends RuntimeException {

    public AiSuggestionNotFoundException(UUID ticketId) {
        super("AI suggestion not found for ticket: " + ticketId);
    }
}
