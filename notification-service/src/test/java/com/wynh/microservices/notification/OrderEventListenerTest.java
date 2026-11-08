package com.wynh.microservices.notification;
import com.wynh.microservices.common.OrderEvents;
import com.wynh.microservices.notification.repository.NotificationRepository;
import com.wynh.microservices.notification.service.OrderEventListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventListenerTest {
    @Mock NotificationRepository repository;
    @InjectMocks OrderEventListener listener;
    @Test void duplicateEventDoesNotCreateAnotherEmail() {
        when(repository.existsById("event-1")).thenReturn(true);
        listener.receive(OrderEvents.confirmed("event-1", "order-1", "customer", "buyer@example.com", "keyboard", 2, "159.80", "2026-01-01T00:00:00Z"));
        verify(repository, never()).saveAndFlush(any());
    }
    @Test void persistsDeliveryBeforeAcknowledgingEvent() {
        listener.receive(OrderEvents.confirmed("event-1", "order-1", "customer", "buyer@example.com", "keyboard", 2, "159.80", "2026-01-01T00:00:00Z"));
        verify(repository).saveAndFlush(argThat(n -> n.getEventId().equals("event-1") && n.getEmail().equals("buyer@example.com") && n.getSentAt() == null));
    }
}