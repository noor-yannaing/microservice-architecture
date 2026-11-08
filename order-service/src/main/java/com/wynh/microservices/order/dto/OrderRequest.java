package com.wynh.microservices.order.dto;
import jakarta.validation.constraints.*;
public record OrderRequest(@NotBlank @Pattern(regexp = "[a-zA-Z0-9_-]{1,64}") String skuCode,
        @NotNull @Min(1) @Max(1000) Integer quantity,
        @NotBlank @Email @Size(max = 254) String email) { }