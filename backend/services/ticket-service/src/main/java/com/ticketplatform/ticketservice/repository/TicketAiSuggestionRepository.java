package com.ticketplatform.ticketservice.repository;

import com.ticketplatform.ticketservice.entity.TicketAiSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TicketAiSuggestionRepository extends JpaRepository<TicketAiSuggestion, UUID> {

    Optional<TicketAiSuggestion> findFirstByTicketIdOrderByCreatedAtDesc(UUID ticketId);
}
