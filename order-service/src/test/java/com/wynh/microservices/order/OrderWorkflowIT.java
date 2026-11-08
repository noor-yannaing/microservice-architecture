package com.wynh.microservices.order;
import com.wynh.microservices.order.client.*;
import com.wynh.microservices.order.dto.OrderRequest;
import com.wynh.microservices.order.repository.*;
import com.wynh.microservices.order.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"commerce.jobs.enabled=false", "management.tracing.enabled=false"})
@Import(TestcontainersConfiguration.class)
class OrderWorkflowIT {
    @Autowired OrderService service;
    @Autowired OrderProcessor processor;
    @Autowired OrderRepository orders;
    @Autowired OutboxRepository outbox;
    @Autowired PlatformTransactionManager transactions;
    @MockitoBean ProductClient products;
    @MockitoBean InventoryClient inventory;
    @BeforeEach void clean() { outbox.deleteAll(); orders.deleteAll(); }

    @Test void acceptedOrderAndOutboxCommitTogether() {
        when(products.price("keyboard")).thenReturn(new BigDecimal("79.90"));
        var result = service.placeOrder("buyer", "one", new OrderRequest("keyboard", 2, "buyer@example.com"));
        var order = orders.findByOrderNumber(result.orderNumber()).orElseThrow();
        when(inventory.reserve(result.orderNumber(), "keyboard", 2)).thenReturn(true);
        processor.process(order.getId());
        assertThat(orders.findById(order.getId()).orElseThrow().getStatus()).isEqualTo("CONFIRMED");
        assertThat(outbox.findAll()).singleElement().satisfies(event -> assertThat(event.getPublishedAt()).isNull());
    }

    @Test void rollbackCannotLeaveConfirmationWithoutOutbox() {
        when(products.price("keyboard")).thenReturn(BigDecimal.TEN);
        var result = service.placeOrder("buyer", "two", new OrderRequest("keyboard", 1, "buyer@example.com"));
        var order = orders.findByOrderNumber(result.orderNumber()).orElseThrow();
        when(inventory.reserve(result.orderNumber(), "keyboard", 1)).thenReturn(true);
        assertThatThrownBy(() -> new TransactionTemplate(transactions).executeWithoutResult(status -> {
            processor.process(order.getId());
            throw new IllegalStateException("simulated commit failure");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(orders.findById(order.getId()).orElseThrow().getStatus()).isEqualTo("PENDING");
        assertThat(outbox.count()).isZero();
    }
}