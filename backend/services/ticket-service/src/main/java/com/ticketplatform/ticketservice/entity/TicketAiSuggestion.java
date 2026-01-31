package com.ticketplatform.ticketservice.entity;

import com.ticketplatform.api.enums.Category;
import com.ticketplatform.api.enums.ConfidenceScore;
import com.ticketplatform.api.enums.Priority;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket_ai_suggestions")
public class TicketAiSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(name = "suggested_category", nullable = false, length = 50)
    private Category suggestedCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "suggested_priority", nullable = false, length = 50)
    private Priority suggestedPriority;

    @Column(name = "suggested_response", columnDefinition = "TEXT")
    private String suggestedResponse;

    @Enumerated(EnumType.STRING)
    @Column(name = "confidence_score", nullable = false, length = 50)
    private ConfidenceScore confidenceScore;

    @Column(name = "confidence_percent")
    private Integer confidencePercent;

    @Column(columnDefinition = "TEXT")
    private String reasoning;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }
    public Category getSuggestedCategory() { return suggestedCategory; }
    public void setSuggestedCategory(Category suggestedCategory) { this.suggestedCategory = suggestedCategory; }
    public Priority getSuggestedPriority() { return suggestedPriority; }
    public void setSuggestedPriority(Priority suggestedPriority) { this.suggestedPriority = suggestedPriority; }
    public String getSuggestedResponse() { return suggestedResponse; }
    public void setSuggestedResponse(String suggestedResponse) { this.suggestedResponse = suggestedResponse; }
    public ConfidenceScore getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(ConfidenceScore confidenceScore) { this.confidenceScore = confidenceScore; }
    public Integer getConfidencePercent() { return confidencePercent; }
    public void setConfidencePercent(Integer confidencePercent) { this.confidencePercent = confidencePercent; }
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    public Instant getCreatedAt() { return createdAt; }
}
