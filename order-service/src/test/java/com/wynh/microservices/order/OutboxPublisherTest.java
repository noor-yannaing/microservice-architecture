package com.wynh.microservices.order;
import com.wynh.microservices.common.OrderEvents;
import com.wynh.microservices.order.model.Order;
import com.wynh.microservices.order.model.OutboxEvent;
import com.wynh.microservices.order.repository.*;
import com.wynh.microservices.order.service.OutboxPublisher;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {
    @Mock OutboxRepository outbox;
    @Mock OrderRepository orders;
    @Mock KafkaTemplate<String, GenericRecord> kafka;
    @InjectMocks OutboxPublisher publisher;
    private OutboxEvent event;
    @BeforeEach void setUp() {
        event = new OutboxEvent("event-1", "order-1", Instant.parse("2026-01-01T00:00:00Z"));
        when(outbox.lock("event-1")).thenReturn(Optional.of(event));
    }
    private void order() {
        var order = new Order(); order.setOrderNumber("order-1"); order.setCustomerId("buyer");
        order.setEmail("buyer@example.com"); order.setSkuCode("keyboard"); order.setQuantity(2); order.setPrice(new BigDecimal("79.90"));
        when(orders.findByOrderNumber("order-1")).thenReturn(Optional.of(order));
    }
    @Test void brokerAcknowledgementMarksEventPublished() throws Exception {
        order();
        when(kafka.send(eq(OrderEvents.TOPIC), eq("order-1"), any(GenericRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
        publisher.publish("event-1");
        assertThat(event.getPublishedAt()).isNotNull();
        verify(kafka).send(eq(OrderEvents.TOPIC), eq("order-1"), argThat(record ->
                record.get("eventId").toString().equals("event-1") && record.get("total").toString().equals("159.80")));
    }
    @Test void brokerFailureLeavesEventUnpublished() {
        order();
        when(kafka.send(eq(OrderEvents.TOPIC), eq("order-1"), any(GenericRecord.class)))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("broker unavailable")));
        assertThatThrownBy(() -> publisher.publish("event-1")).isInstanceOf(ExecutionException.class);
        assertThat(event.getPublishedAt()).isNull();
    }
    @Test void alreadyPublishedEventDoesNotSendAgain() throws Exception {
        event.setPublishedAt(Instant.now());
        publisher.publish("event-1");
        verifyNoInteractions(kafka, orders);
    }
}
