package com.wynh.microservices.order.dto;
import com.wynh.microservices.order.model.Order;
import java.math.BigDecimal;
import java.time.Instant;
public record OrderResponse(String orderNumber, String skuCode, Integer quantity, BigDecimal unitPrice,
        BigDecimal total, String status, Instant createdAt) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getOrderNumber(), order.getSkuCode(), order.getQuantity(), order.getPrice(),
                order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())), order.getStatus(), order.getCreatedAt());
    }
}