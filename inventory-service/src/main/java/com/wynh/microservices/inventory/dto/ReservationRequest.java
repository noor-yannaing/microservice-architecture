package com.wynh.microservices.inventory.dto;
import jakarta.validation.constraints.*;
public record ReservationRequest(@NotBlank @Pattern(regexp = "[0-9a-f-]{36}") String orderNumber,
        @NotBlank @Pattern(regexp = "[a-zA-Z0-9_-]{1,64}") String skuCode,
        @NotNull @Min(1) @Max(1000) Integer quantity) { }