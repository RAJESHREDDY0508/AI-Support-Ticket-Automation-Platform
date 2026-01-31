package com.ticketplatform.ticketservice.service;

import com.ticketplatform.api.dto.*;
import com.ticketplatform.api.enums.Category;
import com.ticketplatform.api.enums.Status;
import com.ticketplatform.ticketservice.entity.*;
import com.ticketplatform.ticketservice.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import com.ticketplatform.ticketservice.event.OutboxEvent;
import com.ticketplatform.ticketservice.event.TicketAiProcessedPayload;
import com.ticketplatform.ticketservice.event.TicketCreatedPayload;
import com.ticketplatform.ticketservice.event.TicketResponseApprovedPayload;
import com.ticketplatform.ticketservice.exception.AiSuggestionNotFoundException;
import com.ticketplatform.ticketservice.exception.TicketNotFoundException;

import java.util.Optional;
import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketAiSuggestionRepository aiSuggestionRepository;
    private final TicketAuditLogRepository auditLogRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final OutboxService outboxService;

    public TicketService(TicketRepository ticketRepository,
                         TicketAiSuggestionRepository aiSuggestionRepository,
                         TicketAuditLogRepository auditLogRepository,
                         IdempotencyKeyRepository idempotencyKeyRepository,
                         OutboxService outboxService) {
        this.ticketRepository = ticketRepository;
        this.aiSuggestionRepository = aiSuggestionRepository;
        this.auditLogRepository = auditLogRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.outboxService = outboxService;
    }

    @Transactional
    public TicketResponse create(CreateTicketRequest request, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            return idempotencyKeyRepository.findById(idempotencyKey)
                    .map(key -> ticketRepository.findById(key.getTicketId()))
                    .filter(Optional::isPresent)
                    .map(opt -> toResponse(opt.get()))
                    .orElseGet(() -> doCreate(request, idempotencyKey));
        }
        return doCreate(request, null);
    }

    private TicketResponse doCreate(CreateTicketRequest request, String idempotencyKey) {
        Ticket ticket = new Ticket();
        ticket.setSubject(request.subject());
        ticket.setDescription(request.description());
        ticket.setCategory(request.category());
        ticket.setPriority(request.priority());
        ticket.setStatus(Status.OPEN);
        ticket.setRequesterEmail(request.requesterEmail());
        ticket = ticketRepository.save(ticket);

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            IdempotencyKey key = new IdempotencyKey();
            key.setKey(idempotencyKey);
            key.setTicketId(ticket.getId());
            idempotencyKeyRepository.save(key);
        }

        TicketCreatedPayload payload = new TicketCreatedPayload(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getCategory(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getRequesterEmail(),
                ticket.getCreatedAt()
        );
        outboxService.append(
                OutboxEvent.TOPIC_TICKET_CREATED,
                OutboxEvent.EVENT_TYPE_TICKET_CREATED,
                OutboxEvent.SCHEMA_VERSION,
                payload
        );

        return toResponse(ticket);
    }

    public TicketResponse getById(UUID id) {
        return ticketRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }

    public PageResponse<TicketResponse> list(int page, int size, Status status, Category category) {
        var pageable = PageRequest.of(page, size);
        Specification<Ticket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        var result = ticketRepository.findAll(spec, pageable);
        List<TicketResponse> content = result.getContent().stream().map(this::toResponse).toList();
        return PageResponse.of(content, page, size, result.getTotalElements());
    }

    @Transactional
    public TicketResponse updateStatus(UUID id, UpdateTicketStatusRequest request, String changedBy) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
        Status oldStatus = ticket.getStatus();
        ticket.setStatus(request.status());
        ticket = ticketRepository.save(ticket);
        audit("STATUS_UPDATE", id, oldStatus.name(), request.status().name(), changedBy);
        return toResponse(ticket);
    }

    @Transactional
    public AiSuggestionResponse putAiSuggestion(UUID id, AiSuggestionRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
        TicketAiSuggestion suggestion = new TicketAiSuggestion();
        suggestion.setTicket(ticket);
        suggestion.setSuggestedCategory(request.suggestedCategory());
        suggestion.setSuggestedPriority(request.suggestedPriority());
        suggestion.setSuggestedResponse(request.suggestedResponse());
        suggestion.setConfidenceScore(request.confidenceScore());
        suggestion.setConfidencePercent(request.confidencePercent());
        suggestion.setReasoning(request.reasoning());
        suggestion = aiSuggestionRepository.save(suggestion);

        ticket.setStatus(Status.AI_PROCESSED);
        ticketRepository.save(ticket);

        TicketAiProcessedPayload payload = new TicketAiProcessedPayload(
                ticket.getId(),
                suggestion.getId(),
                suggestion.getSuggestedCategory(),
                suggestion.getSuggestedPriority(),
                suggestion.getSuggestedResponse(),
                suggestion.getConfidenceScore(),
                suggestion.getConfidencePercent()
        );
        outboxService.append(
                OutboxEvent.TOPIC_TICKET_AI_PROCESSED,
                OutboxEvent.EVENT_TYPE_TICKET_AI_PROCESSED,
                OutboxEvent.SCHEMA_VERSION,
                payload
        );

        return toAiSuggestionResponse(suggestion);
    }

    public AiSuggestionResponse getAiSuggestion(UUID id) {
        if (!ticketRepository.existsById(id)) {
            throw new TicketNotFoundException(id);
        }
        return aiSuggestionRepository.findFirstByTicketIdOrderByCreatedAtDesc(id)
                .map(this::toAiSuggestionResponse)
                .orElseThrow(() -> new AiSuggestionNotFoundException(id));
    }

    @Transactional
    public TicketResponse putResponseApproval(UUID id, ResponseApprovalRequest request, String changedBy) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
        String oldValue = ticket.getApprovedResponse();
        ticket.setApprovedResponse(request.approvedResponse());
        ticket = ticketRepository.save(ticket);
        audit("RESPONSE_APPROVAL", id, oldValue, request.approvedResponse(), changedBy);

        TicketResponseApprovedPayload payload = new TicketResponseApprovedPayload(ticket.getId(), ticket.getApprovedResponse());
        outboxService.append(
                OutboxEvent.TOPIC_TICKET_RESPONSE_APPROVED,
                OutboxEvent.EVENT_TYPE_TICKET_RESPONSE_APPROVED,
                OutboxEvent.SCHEMA_VERSION,
                payload
        );

        return toResponse(ticket);
    }

    public PageResponse<TicketResponse> getSimilar(UUID id) {
        // Placeholder - will come later
        return PageResponse.of(List.of(), 0, 20, 0);
    }

    private void audit(String action, UUID ticketId, String oldValue, String newValue, String changedBy) {
        Ticket ticket = ticketRepository.getReferenceById(ticketId);
        TicketAuditLog log = new TicketAuditLog();
        log.setTicket(ticket);
        log.setAction(action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setChangedBy(changedBy);
        auditLogRepository.save(log);
    }

    private TicketResponse toResponse(Ticket t) {
        return new TicketResponse(
                t.getId().toString(),
                t.getSubject(),
                t.getDescription(),
                t.getCategory(),
                t.getPriority(),
                t.getStatus(),
                t.getRequesterEmail(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }

    private AiSuggestionResponse toAiSuggestionResponse(TicketAiSuggestion s) {
        return new AiSuggestionResponse(
                s.getSuggestedCategory(),
                s.getSuggestedPriority(),
                s.getSuggestedResponse(),
                s.getConfidenceScore(),
                s.getConfidencePercent(),
                s.getReasoning()
        );
    }
}
