package com.wynh.microservices.order.repository;
import com.wynh.microservices.order.model.OutboxEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface OutboxRepository extends JpaRepository<OutboxEvent, String> {
    @Query("select e.id from OutboxEvent e where e.publishedAt is null order by e.occurredAt")
    List<String> unpublished(Pageable page);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from OutboxEvent e where e.id = :id")
    Optional<OutboxEvent> lock(@Param("id") String id);
}