package com.ticketplatform.ticketservice.controller;

import com.ticketplatform.api.dto.*;
import com.ticketplatform.api.enums.Category;
import com.ticketplatform.api.enums.Status;
import com.ticketplatform.ticketservice.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(
            @Valid @RequestBody CreateTicketRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        TicketResponse created = ticketService.create(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ticketService.getById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<TicketResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Category category) {
        return ResponseEntity.ok(ticketService.list(page, size, status, category));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTicketStatusRequest request,
            @RequestHeader(value = "X-Admin-User", required = false) String adminUser) {
        return ResponseEntity.ok(ticketService.updateStatus(id, request, adminUser));
    }

    @PutMapping("/{id}/ai-suggestion")
    public ResponseEntity<AiSuggestionResponse> putAiSuggestion(
            @PathVariable UUID id,
            @Valid @RequestBody AiSuggestionRequest request) {
        return ResponseEntity.ok(ticketService.putAiSuggestion(id, request));
    }

    @GetMapping("/{id}/ai-suggestion")
    public ResponseEntity<AiSuggestionResponse> getAiSuggestion(@PathVariable UUID id) {
        return ResponseEntity.ok(ticketService.getAiSuggestion(id));
    }

    @PutMapping("/{id}/response-approval")
    public ResponseEntity<TicketResponse> putResponseApproval(
            @PathVariable UUID id,
            @Valid @RequestBody ResponseApprovalRequest request,
            @RequestHeader(value = "X-Admin-User", required = false) String adminUser) {
        return ResponseEntity.ok(ticketService.putResponseApproval(id, request, adminUser));
    }

    @GetMapping("/{id}/similar")
    public ResponseEntity<PageResponse<TicketResponse>> getSimilar(@PathVariable UUID id) {
        return ResponseEntity.ok(ticketService.getSimilar(id));
    }
}
