package com.wynh.microservices.product.controller;

import com.wynh.microservices.product.dto.*;
import com.wynh.microservices.product.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
@Validated
public class ProductController {
    private final ProductService service;
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) { return service.createProduct(request); }
    @GetMapping
    public Page<ProductResponse> list(@RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) { return service.list(page, size); }
    @GetMapping("/{skuCode}")
    public ProductResponse get(@PathVariable String skuCode) { return service.bySku(skuCode); }
}