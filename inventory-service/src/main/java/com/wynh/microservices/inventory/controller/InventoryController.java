package com.wynh.microservices.inventory.controller;

import com.wynh.microservices.inventory.dto.*;
import com.wynh.microservices.inventory.service.InventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor @Validated
public class InventoryController {
    private final InventoryService service;
    @GetMapping("/api/inventory")
    public boolean inStock(@RequestParam @Pattern(regexp = "[a-zA-Z0-9_-]{1,64}") String skuCode,
            @RequestParam @Min(1) @Max(1000) Integer quantity) { return service.isInStock(skuCode, quantity); }

    @PutMapping("/api/inventory/{sku}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void stock(@PathVariable @Pattern(regexp = "[a-zA-Z0-9_-]{1,64}") String sku,
            @Valid @RequestBody StockRequest request) { service.setStock(sku, request.quantity()); }

    @PostMapping("/internal/inventory/reservations")
    public boolean reserve(@Valid @RequestBody ReservationRequest request) { return service.reserve(request); }
}