package com.wynh.microservices.order.service;

import com.wynh.microservices.order.client.InventoryClient;
import com.wynh.microservices.order.model.OutboxEvent;
import com.wynh.microservices.order.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import java.time.Instant;
import java.util.UUID;

@Service @RequiredArgsConstructor @Slf4j
public class OrderProcessor {
    private final OrderRepository orders;
    private final OutboxRepository outbox;
    private final InventoryClient inventory;

    @Transactional
    public void process(Long id) {
        var order = orders.lock(id).orElseThrow();
        if (!order.getStatus().equals("PENDING") || order.getNextAttemptAt().isAfter(Instant.now())) return;
        boolean accepted;
        try {
            accepted = inventory.reserve(order.getOrderNumber(), order.getSkuCode(), order.getQuantity());
        } catch (RestClientException | IllegalStateException exception) {
            order.setAttempts(order.getAttempts() + 1);
            order.setNextAttemptAt(Instant.now().plusSeconds(Math.min(60, 1L << Math.min(order.getAttempts(), 6))));
            log.warn("Reservation will be retried for order {}", order.getOrderNumber());
            return;
        }
        order.setStatus(accepted ? "CONFIRMED" : "REJECTED");
        if (accepted) outbox.save(new OutboxEvent(UUID.randomUUID().toString(), order.getOrderNumber(), Instant.now()));
    }
}