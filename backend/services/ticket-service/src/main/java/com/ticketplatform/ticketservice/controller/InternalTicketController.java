package com.ticketplatform.ticketservice.controller;

import com.ticketplatform.api.dto.AiSuggestionRequest;
import com.ticketplatform.api.dto.AiSuggestionResponse;
import com.ticketplatform.ticketservice.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Internal endpoints for service-to-service calls (e.g. AI worker).
 * Secured via X-Internal-Token (see InternalAuthFilter).
 */
@RestController
@RequestMapping("/internal/tickets")
public class InternalTicketController {

    private final TicketService ticketService;

    public InternalTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PutMapping("/{id}/ai-suggestion")
    public ResponseEntity<AiSuggestionResponse> putAiSuggestion(
            @PathVariable UUID id,
            @Valid @RequestBody AiSuggestionRequest request) {
        return ResponseEntity.ok(ticketService.putAiSuggestion(id, request));
    }
}
