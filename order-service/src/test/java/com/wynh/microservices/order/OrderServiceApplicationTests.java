package com.wynh.microservices.order;
import com.wynh.microservices.common.ApiException;
import com.wynh.microservices.order.client.ProductClient;
import com.wynh.microservices.order.dto.OrderRequest;
import com.wynh.microservices.order.model.Order;
import com.wynh.microservices.order.repository.OrderRepository;
import com.wynh.microservices.order.service.OrderService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceApplicationTests {
    @Mock OrderRepository orders;
    @Mock ProductClient products;
    @InjectMocks OrderService service;
    private final OrderRequest request = new OrderRequest("keyboard", 2, "buyer@example.com");
    @Test void usesCataloguePriceAndStartsPending() {
        when(orders.findByCustomerIdAndIdempotencyKey("buyer", "key")).thenReturn(Optional.empty());
        when(products.price("keyboard")).thenReturn(new BigDecimal("79.90"));
        when(orders.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        var result = service.placeOrder("buyer", "key", request);
        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.total()).isEqualByComparingTo("159.80");
        assertThat(result.orderNumber()).isNotBlank();
    }
    @Test void repeatedKeyReturnsOriginalWithoutCallingCatalogue() {
        var existing = existing();
        when(orders.findByCustomerIdAndIdempotencyKey("buyer", "key")).thenReturn(Optional.of(existing));
        assertThat(service.placeOrder("buyer", "key", request).orderNumber()).isEqualTo("order-1");
        verifyNoInteractions(products);
        verify(orders, never()).saveAndFlush(any());
    }
    @Test void reusedKeyWithDifferentPayloadIsRejected() {
        when(orders.findByCustomerIdAndIdempotencyKey("buyer", "key")).thenReturn(Optional.of(existing()));
        assertThatThrownBy(() -> service.placeOrder("buyer", "key", new OrderRequest("keyboard", 3, request.email())))
            .isInstanceOf(ApiException.class);
    }
    @Test void customerCannotReadAnotherCustomersOrder() {
        when(orders.findByOrderNumberAndCustomerId("order-1", "another")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get("another", "order-1")).isInstanceOf(ApiException.class);
    }
    private Order existing() {
        var order = new Order(); order.setOrderNumber("order-1"); order.setSkuCode(request.skuCode());
        order.setQuantity(request.quantity()); order.setEmail(request.email()); order.setPrice(BigDecimal.TEN); order.setStatus("CONFIRMED");
        return order;
    }
}