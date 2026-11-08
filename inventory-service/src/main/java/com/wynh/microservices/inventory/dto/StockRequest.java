package com.wynh.microservices.inventory.dto;
import jakarta.validation.constraints.*;
public record StockRequest(@NotNull @Min(0) Integer quantity) { }