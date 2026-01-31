package com.ticketplatform.ticketservice.repository;

import com.ticketplatform.ticketservice.entity.TicketAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketAuditLogRepository extends JpaRepository<TicketAuditLog, UUID> {
}
