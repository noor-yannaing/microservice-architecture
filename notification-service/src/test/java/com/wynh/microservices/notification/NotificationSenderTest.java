package com.wynh.microservices.notification;
import com.wynh.microservices.notification.model.Notification;
import com.wynh.microservices.notification.repository.NotificationRepository;
import com.wynh.microservices.notification.service.NotificationSender;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.Instant;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationSenderTest {
    @Mock NotificationRepository repository;
    @Mock JavaMailSender mail;
    @InjectMocks NotificationSender sender;
    private Notification notification;
    @BeforeEach void setUp() {
        notification = new Notification(); notification.setEventId("event-1"); notification.setOrderNumber("order-1");
        notification.setEmail("buyer@example.com"); notification.setTotal("159.80"); notification.setNextAttemptAt(Instant.EPOCH);
        when(repository.lock("event-1")).thenReturn(Optional.of(notification));
        ReflectionTestUtils.setField(sender, "from", "orders@commerce.local");
    }
    @Test void successfulDeliveryRecordsSentTimestamp() {
        sender.send("event-1");
        assertThat(notification.getSentAt()).isNotNull();
        verify(mail).send(argThat((SimpleMailMessage message) -> message.getSubject().contains("order-1")));
    }
    @Test void smtpFailureSchedulesAnotherAttempt() {
        doThrow(new MailSendException("unavailable")).when(mail).send(any(SimpleMailMessage.class));
        sender.send("event-1");
        assertThat(notification.getSentAt()).isNull();
        assertThat(notification.getAttempts()).isEqualTo(1);
        assertThat(notification.getNextAttemptAt()).isAfter(Instant.now());
    }
    @Test void sentNotificationIsNotDeliveredAgain() {
        notification.setSentAt(Instant.now());
        sender.send("event-1");
        verifyNoInteractions(mail);
    }
}