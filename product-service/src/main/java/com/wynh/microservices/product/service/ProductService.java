package com.wynh.microservices.product.service;

import com.wynh.microservices.common.ApiException;
import com.wynh.microservices.product.dto.*;
import com.wynh.microservices.product.model.Product;
import com.wynh.microservices.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repository;

    public ProductResponse createProduct(ProductRequest request) {
        var product = Product.builder().skuCode(request.skuCode()).name(request.name().strip())
                .description(request.description().strip()).price(request.price()).build();
        try {
            return response(repository.insert(product));
        } catch (DuplicateKeyException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "A product already exists for this SKU");
        }
    }

    public Page<ProductResponse> list(int page, int size) {
        return repository.findAll(PageRequest.of(page, size, Sort.by("skuCode"))).map(this::response);
    }

    public ProductResponse bySku(String sku) {
        return response(repository.findBySkuCode(sku)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found")));
    }

    private ProductResponse response(Product product) {
        return new ProductResponse(product.getId(), product.getSkuCode(), product.getName(),
                product.getDescription(), product.getPrice());
    }
}