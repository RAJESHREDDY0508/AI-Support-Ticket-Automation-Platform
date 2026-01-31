package com.ticketplatform.ticketservice.repository;

import com.ticketplatform.ticketservice.entity.OutboxEntry;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEntry, UUID> {

    @Query("SELECT o FROM OutboxEntry o WHERE o.publishedAt IS NULL ORDER BY o.createdAt ASC")
    List<OutboxEntry> findUnpublishedOrderByCreatedAt(Pageable pageable);

    default List<OutboxEntry> findNextUnpublished(int batchSize) {
        return findUnpublishedOrderByCreatedAt(PageRequest.of(0, batchSize));
    }

    @Modifying
    @Query("UPDATE OutboxEntry o SET o.publishedAt = :publishedAt WHERE o.id = :id")
    void markPublished(@Param("id") UUID id, @Param("publishedAt") Instant publishedAt);

    @Modifying
    @Query("UPDATE OutboxEntry o SET o.errorMessage = :errorMessage, o.retryCount = o.retryCount + 1 WHERE o.id = :id")
    void recordFailure(@Param("id") UUID id, @Param("errorMessage") String errorMessage);
}
