package com.wynh.microservices.notification.repository;
import com.wynh.microservices.notification.model.Notification;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.*;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    @Query("select n.eventId from Notification n where n.sentAt is null and n.nextAttemptAt <= :now order by n.nextAttemptAt")
    List<String> pending(@Param("now") Instant now, Pageable page);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from Notification n where n.eventId = :id")
    Optional<Notification> lock(@Param("id") String id);
}