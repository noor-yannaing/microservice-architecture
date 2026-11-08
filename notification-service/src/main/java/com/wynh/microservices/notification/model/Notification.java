package com.wynh.microservices.notification.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name = "t_notifications")
@Getter @Setter @NoArgsConstructor
public class Notification {
    @Id @Column(length = 36) private String eventId;
    @Column(nullable = false, length = 36) private String orderNumber;
    @Column(nullable = false, length = 254) private String email;
    @Column(nullable = false, length = 64) private String total;
    @Column(nullable = false) private Instant nextAttemptAt;
    private Instant sentAt;
    @Column(nullable = false) private int attempts;
}