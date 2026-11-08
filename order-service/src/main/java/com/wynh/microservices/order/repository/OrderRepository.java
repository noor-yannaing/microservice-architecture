package com.wynh.microservices.order.repository;
import com.wynh.microservices.order.model.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.*;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByCustomerIdAndIdempotencyKey(String customerId, String idempotencyKey);
    Optional<Order> findByOrderNumberAndCustomerId(String orderNumber, String customerId);
    Optional<Order> findByOrderNumber(String orderNumber);
    @Query("select o.id from Order o where o.status = 'PENDING' and o.nextAttemptAt <= :now order by o.id")
    List<Long> pending(@Param("now") Instant now, Pageable page);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> lock(@Param("id") Long id);
}