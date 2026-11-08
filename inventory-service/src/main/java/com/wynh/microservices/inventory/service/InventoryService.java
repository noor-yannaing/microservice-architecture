package com.wynh.microservices.inventory.service;

import com.wynh.microservices.common.ApiException;
import com.wynh.microservices.inventory.dto.ReservationRequest;
import com.wynh.microservices.inventory.model.*;
import com.wynh.microservices.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventory;
    private final ReservationRepository reservations;

    @Transactional(readOnly = true)
    public boolean isInStock(String skuCode, Integer quantity) {
        return inventory.existsBySkuCodeAndQuantityIsGreaterThanEqual(skuCode, quantity);
    }

    @Transactional
    public boolean reserve(ReservationRequest request) {
        // Lock stock before checking the reservation so concurrent retries cannot deduct twice.
        var stock = inventory.lockBySku(request.skuCode());
        var existing = reservations.findById(request.orderNumber());
        if (existing.isPresent()) {
            var reservation = existing.get();
            if (!reservation.getSkuCode().equals(request.skuCode()) || !reservation.getQuantity().equals(request.quantity())) {
                throw new ApiException(HttpStatus.CONFLICT, "Order number belongs to a different reservation");
            }
            return reservation.isAccepted();
        }
        boolean accepted = stock.isPresent() && stock.get().getQuantity() >= request.quantity();
        if (accepted) stock.get().setQuantity(stock.get().getQuantity() - request.quantity());
        reservations.saveAndFlush(new Reservation(request.orderNumber(), request.skuCode(), request.quantity(), accepted));
        return accepted;
    }

    @Transactional
    public void setStock(String sku, int quantity) {
        var stock = inventory.lockBySku(sku).orElseGet(() -> new Inventory(null, sku, 0));
        stock.setQuantity(quantity);
        inventory.saveAndFlush(stock);
    }
}