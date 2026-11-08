package com.wynh.microservices.order.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name = "t_outbox")
@Getter @Setter @NoArgsConstructor
public class OutboxEvent {
    @Id @Column(length = 36) private String id;
    @Column(nullable = false, unique = true, length = 36) private String orderNumber;
    @Column(nullable = false) private Instant occurredAt;
    private Instant publishedAt;
    public OutboxEvent(String id, String orderNumber, Instant occurredAt) {
        this.id = id; this.orderNumber = orderNumber; this.occurredAt = occurredAt;
    }
}