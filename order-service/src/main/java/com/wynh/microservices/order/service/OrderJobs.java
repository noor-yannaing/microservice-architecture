package com.wynh.microservices.order.service;
import com.wynh.microservices.order.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;

@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "commerce.jobs.enabled", havingValue = "true", matchIfMissing = true)
@Component @RequiredArgsConstructor @Slf4j
public class OrderJobs {
    private final OrderRepository orders;
    private final OutboxRepository outbox;
    private final OrderProcessor processor;
    private final OutboxPublisher publisher;

    @Scheduled(fixedDelayString = "${orders.poll-delay-ms:1000}")
    public void processOrders() {
        for (Long id : orders.pending(Instant.now(), PageRequest.of(0, 20))) {
            try { processor.process(id); }
            catch (RuntimeException exception) { log.error("Order processing failed for id {}", id, exception); }
        }
    }

    @Scheduled(fixedDelayString = "${outbox.poll-delay-ms:2000}")
    public void publishEvents() {
        for (String id : outbox.unpublished(PageRequest.of(0, 20))) {
            try { publisher.publish(id); }
            catch (InterruptedException exception) { Thread.currentThread().interrupt(); return; }
            catch (Exception exception) { log.warn("Outbox event {} will be retried: {}", id, exception.getClass().getSimpleName()); break; }
        }
    }
}