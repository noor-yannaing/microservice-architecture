package com.wynh.microservices.order.service;

import com.wynh.microservices.common.ApiException;
import com.wynh.microservices.order.client.ProductClient;
import com.wynh.microservices.order.dto.*;
import com.wynh.microservices.order.model.Order;
import com.wynh.microservices.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orders;
    private final ProductClient products;

    public OrderResponse placeOrder(String customer, String key, OrderRequest request) {
        var existing = orders.findByCustomerIdAndIdempotencyKey(customer, key);
        if (existing.isPresent()) return replay(existing.get(), request);
        var order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setCustomerId(customer);
        order.setIdempotencyKey(key);
        order.setSkuCode(request.skuCode());
        order.setQuantity(request.quantity());
        order.setEmail(request.email());
        order.setPrice(products.price(request.skuCode()));
        order.setStatus("PENDING");
        order.setCreatedAt(Instant.now());
        order.setNextAttemptAt(order.getCreatedAt());
        try {
            return OrderResponse.from(orders.saveAndFlush(order));
        } catch (DataIntegrityViolationException exception) {
            // Another request may have committed the same key while the price was being fetched.
            var winner = orders.findByCustomerIdAndIdempotencyKey(customer, key);
            if (winner.isPresent()) return replay(winner.get(), request);
            throw exception;
        }
    }

    public OrderResponse get(String customer, String number) {
        return orders.findByOrderNumberAndCustomerId(number, customer).map(OrderResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private OrderResponse replay(Order existing, OrderRequest request) {
        if (!existing.getSkuCode().equals(request.skuCode()) || !existing.getQuantity().equals(request.quantity())
                || !existing.getEmail().equals(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "Idempotency key was already used for a different order");
        }
        return OrderResponse.from(existing);
    }
}