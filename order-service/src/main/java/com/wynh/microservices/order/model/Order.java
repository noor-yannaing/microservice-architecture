package com.wynh.microservices.order.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name = "t_orders")
@Getter @Setter @NoArgsConstructor
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 36) private String orderNumber;
    @Column(nullable = false, length = 64) private String skuCode;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal price;
    @Column(nullable = false) private Integer quantity;
    @Column(nullable = false, length = 128) private String customerId;
    @Column(nullable = false, length = 128) private String idempotencyKey;
    @Column(nullable = false, length = 254) private String email;
    @Column(nullable = false, length = 20) private String status;
    @Column(nullable = false) private Instant createdAt;
    @Column(nullable = false) private Instant nextAttemptAt;
    @Column(nullable = false) private int attempts;
}