package com.wynh.microservices.product.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Pattern(regexp = "[a-zA-Z0-9_-]{1,64}") String skuCode,
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 2000) String description,
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal price) { }
