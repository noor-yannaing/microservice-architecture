package com.wynh.microservices.notification.service;
import com.wynh.microservices.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;

@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "commerce.jobs.enabled", havingValue = "true", matchIfMissing = true)
@Component @RequiredArgsConstructor @Slf4j
public class NotificationJob {
    private final NotificationRepository repository;
    private final NotificationSender sender;
    @Scheduled(fixedDelayString = "${notifications.poll-delay-ms:2000}")
    public void deliver() {
        for (String id : repository.pending(Instant.now(), PageRequest.of(0, 20))) {
            try { sender.send(id); }
            catch (RuntimeException exception) { log.error("Notification processing failed for event {}", id, exception); }
        }
    }
}