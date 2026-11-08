package com.wynh.microservices.order.controller;
import com.wynh.microservices.order.dto.*;
import com.wynh.microservices.order.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/order") @RequiredArgsConstructor @Validated
public class OrderController {
    private final OrderService service;
    @PostMapping @ResponseStatus(HttpStatus.ACCEPTED)
    public OrderResponse create(@RequestHeader("X-Customer-Id") @NotBlank @Size(max = 128) String customer,
            @RequestHeader("Idempotency-Key") @Pattern(regexp = "[a-zA-Z0-9_-]{1,128}") String key,
            @Valid @RequestBody OrderRequest request) { return service.placeOrder(customer, key, request); }
    @GetMapping("/{number}")
    public OrderResponse get(@RequestHeader("X-Customer-Id") String customer, @PathVariable String number) {
        return service.get(customer, number);
    }
}