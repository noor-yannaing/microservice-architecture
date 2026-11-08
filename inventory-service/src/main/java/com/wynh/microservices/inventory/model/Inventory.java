package com.wynh.microservices.inventory.model;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "t_inventory")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Inventory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 64) private String skuCode;
    @Column(nullable = false) private Integer quantity;
}