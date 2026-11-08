package com.wynh.microservices.product.model;
import java.math.BigDecimal;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Document("product")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
    @Id private String id;
    @Indexed(unique = true) private String skuCode;
    private String name;
    private String description;
    private BigDecimal price;
}