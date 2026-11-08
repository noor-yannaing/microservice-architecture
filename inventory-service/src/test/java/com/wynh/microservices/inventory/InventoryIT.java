package com.wynh.microservices.inventory;

import com.wynh.microservices.inventory.dto.ReservationRequest;
import com.wynh.microservices.inventory.repository.*;
import com.wynh.microservices.inventory.service.InventoryService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = "management.tracing.enabled=false")
@Import(TestcontainersConfiguration.class)
class InventoryIT {
    @Autowired InventoryService service;
    @Autowired InventoryRepository inventory;
    @Autowired ReservationRepository reservations;

    @BeforeEach void clean() { reservations.deleteAll(); inventory.deleteAll(); }

    @Test void concurrentOrdersCannotOversell() throws Exception {
        service.setStock("limited", 5);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < 12; i++) results.add(executor.submit(() -> {
                start.await();
                return service.reserve(new ReservationRequest(UUID.randomUUID().toString(), "limited", 1));
            }));
            start.countDown();
            int accepted = 0;
            for (var result : results) if (result.get(30, TimeUnit.SECONDS)) accepted++;
            assertThat(accepted).isEqualTo(5);
            assertThat(inventory.findAll().getFirst().getQuantity()).isZero();
            assertThat(reservations.count()).isEqualTo(12);
        }
    }

    @Test void concurrentRetriesDeductOnlyOnce() throws Exception {
        service.setStock("limited", 5);
        var request = new ReservationRequest(UUID.randomUUID().toString(), "limited", 2);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < 8; i++) results.add(executor.submit(() -> service.reserve(request)));
            for (var result : results) assertThat(result.get(30, TimeUnit.SECONDS)).isTrue();
        }
        assertThat(inventory.findAll().getFirst().getQuantity()).isEqualTo(3);
        assertThat(reservations.count()).isEqualTo(1);
    }

    @Test void rejectionRemainsStableAfterRestock() {
        service.setStock("limited", 0);
        var request = new ReservationRequest(UUID.randomUUID().toString(), "limited", 1);
        assertThat(service.reserve(request)).isFalse();
        service.setStock("limited", 10);
        assertThat(service.reserve(request)).isFalse();
        assertThat(inventory.findAll().getFirst().getQuantity()).isEqualTo(10);
    }
}