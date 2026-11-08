package com.wynh.microservices.inventory.model;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "t_reservations")
@Getter @NoArgsConstructor @AllArgsConstructor
public class Reservation {
    @Id @Column(length = 36) private String orderNumber;
    @Column(nullable = false, length = 64) private String skuCode;
    @Column(nullable = false) private Integer quantity;
    @Column(nullable = false) private boolean accepted;
}