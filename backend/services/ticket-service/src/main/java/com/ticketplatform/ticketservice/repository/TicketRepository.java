package com.ticketplatform.ticketservice.repository;

import com.ticketplatform.api.enums.Category;
import com.ticketplatform.api.enums.Status;
import com.ticketplatform.ticketservice.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {
}
