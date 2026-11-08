package com.wynh.microservices.notification.service;
import com.wynh.microservices.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service @RequiredArgsConstructor @Slf4j
public class NotificationSender {
    private final NotificationRepository repository;
    private final JavaMailSender mail;
    @Value("${notifications.from}") private String from;

    @Transactional
    public void send(String id) {
        var notification = repository.lock(id).orElseThrow();
        if (notification.getSentAt() != null || notification.getNextAttemptAt().isAfter(Instant.now())) return;
        var message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(notification.getEmail());
        message.setSubject("Order " + notification.getOrderNumber() + " confirmed");
        message.setText("Your order is confirmed. Order number: " + notification.getOrderNumber()
                + "\nTotal: " + notification.getTotal() + " USD");
        try {
            mail.send(message);
            notification.setSentAt(Instant.now());
        } catch (MailException exception) {
            notification.setAttempts(notification.getAttempts() + 1);
            notification.setNextAttemptAt(Instant.now().plusSeconds(Math.min(300, 1L << Math.min(notification.getAttempts(), 9))));
            log.warn("Delivery will be retried for event {}", id);
        }
    }
}