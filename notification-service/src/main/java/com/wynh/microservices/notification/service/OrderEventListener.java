package com.wynh.microservices.notification.service;
import com.wynh.microservices.common.OrderEvents;
import com.wynh.microservices.notification.model.Notification;
import com.wynh.microservices.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Component @RequiredArgsConstructor
public class OrderEventListener {
    private final NotificationRepository repository;

    @KafkaListener(topics = OrderEvents.TOPIC, groupId = "notification-service")
    @Transactional
    public void receive(GenericRecord event) {
        String eventId = event.get("eventId").toString();
        if (repository.existsById(eventId)) return;
        var notification = new Notification();
        notification.setEventId(eventId);
        notification.setOrderNumber(event.get("orderNumber").toString());
        notification.setEmail(event.get("email").toString());
        notification.setTotal(event.get("total").toString());
        notification.setNextAttemptAt(Instant.now());
        repository.saveAndFlush(notification);
    }
}