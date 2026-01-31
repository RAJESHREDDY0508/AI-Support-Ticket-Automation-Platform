package com.ticketplatform.ticketservice.repository;

import com.ticketplatform.ticketservice.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {
}
