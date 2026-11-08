package com.wynh.microservices.order.service;

import com.wynh.microservices.common.OrderEvents;
import com.wynh.microservices.order.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service @RequiredArgsConstructor
public class OutboxPublisher {
    private final OutboxRepository outbox;
    private final OrderRepository orders;
    private final KafkaTemplate<String, GenericRecord> kafka;

    @Transactional(rollbackFor = Exception.class)
    public void publish(String id) throws Exception {
        var event = outbox.lock(id).orElseThrow();
        if (event.getPublishedAt() != null) return;
        var order = orders.findByOrderNumber(event.getOrderNumber()).orElseThrow();
        var record = OrderEvents.confirmed(event.getId(), order.getOrderNumber(), order.getCustomerId(), order.getEmail(),
                order.getSkuCode(), order.getQuantity(), order.getPrice().multiply(java.math.BigDecimal.valueOf(order.getQuantity())).toPlainString(),
                event.getOccurredAt().toString());
        kafka.send(OrderEvents.TOPIC, order.getOrderNumber(), record).get(10, TimeUnit.SECONDS);
        event.setPublishedAt(Instant.now());
    }
}