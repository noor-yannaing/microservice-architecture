package com.wynh.microservices.order;
import com.wynh.microservices.order.client.InventoryClient;
import com.wynh.microservices.order.model.Order;
import com.wynh.microservices.order.repository.*;
import com.wynh.microservices.order.service.OrderProcessor;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import java.time.Instant;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderProcessorTest {
    @Mock OrderRepository orders;
    @Mock OutboxRepository outbox;
    @Mock InventoryClient inventory;
    @InjectMocks OrderProcessor processor;
    private Order order;
    @BeforeEach void setUp() {
        order = new Order(); order.setOrderNumber("number"); order.setSkuCode("keyboard"); order.setQuantity(2);
        order.setStatus("PENDING"); order.setNextAttemptAt(Instant.EPOCH);
        when(orders.lock(1L)).thenReturn(Optional.of(order));
    }
    @Test void confirmationCreatesOutboxEvent() {
        when(inventory.reserve("number", "keyboard", 2)).thenReturn(true);
        processor.process(1L);
        assertThat(order.getStatus()).isEqualTo("CONFIRMED");
        verify(outbox).save(argThat(event -> event.getOrderNumber().equals("number") && event.getId() != null));
    }
    @Test void rejectionDoesNotEmitConfirmation() {
        when(inventory.reserve("number", "keyboard", 2)).thenReturn(false);
        processor.process(1L);
        assertThat(order.getStatus()).isEqualTo("REJECTED");
        verifyNoInteractions(outbox);
    }
    @Test void ambiguousTimeoutKeepsOrderPendingForIdempotentRetry() {
        when(inventory.reserve("number", "keyboard", 2)).thenThrow(new ResourceAccessException("timeout"));
        processor.process(1L);
        assertThat(order.getStatus()).isEqualTo("PENDING");
        assertThat(order.getAttempts()).isEqualTo(1);
        assertThat(order.getNextAttemptAt()).isAfter(Instant.now());
        verifyNoInteractions(outbox);
    }
    @Test void confirmedOrderIsNotReservedAgain() {
        order.setStatus("CONFIRMED");
        processor.process(1L);
        verifyNoInteractions(inventory, outbox);
    }
}