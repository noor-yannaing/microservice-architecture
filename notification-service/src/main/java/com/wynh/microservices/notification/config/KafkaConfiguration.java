package com.wynh.microservices.notification.config;
import org.springframework.context.annotation.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration(proxyBeanMethods = false)
public class KafkaConfiguration {
    @Bean
    DefaultErrorHandler retryUntilStored() {
        // Do not acknowledge an event while the inbox database is unavailable.
        var handler = new DefaultErrorHandler(new FixedBackOff(2000, FixedBackOff.UNLIMITED_ATTEMPTS));
        handler.setClassifications(java.util.Map.of(Exception.class, true), true);
        return handler;
    }
}