package com.wynh.microservices.product;
import com.wynh.microservices.common.ApiException;
import com.wynh.microservices.product.dto.*;
import com.wynh.microservices.product.model.Product;
import com.wynh.microservices.product.repository.ProductRepository;
import com.wynh.microservices.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceApplicationTests {
    @Mock ProductRepository repository;
    @InjectMocks ProductService service;
    @Test void createsProductWithServerGeneratedId() {
        when(repository.insert(any(Product.class))).thenAnswer(call -> {
            Product product = call.getArgument(0); product.setId("product-1"); return product;
        });
        var result = service.createProduct(new ProductRequest("keyboard", "Keyboard", "Mechanical keyboard", new BigDecimal("79.90")));
        assertThat(result.id()).isEqualTo("product-1");
        assertThat(result.skuCode()).isEqualTo("keyboard");
        assertThat(result.price()).isEqualByComparingTo("79.90");
    }
    @Test void duplicateSkuIsAConflict() {
        when(repository.insert(any(Product.class))).thenThrow(new DuplicateKeyException("sku"));
        assertThatThrownBy(() -> service.createProduct(new ProductRequest("keyboard", "Keyboard", "Description", BigDecimal.TEN)))
            .isInstanceOf(ApiException.class).hasMessageContaining("already exists");
    }
    @Test void missingProductReturnsNotFound() {
        when(repository.findBySkuCode("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.bySku("missing")).isInstanceOf(ApiException.class);
    }
}