package com.wynh.microservices.inventory;
import com.wynh.microservices.common.ApiException;
import com.wynh.microservices.inventory.dto.ReservationRequest;
import com.wynh.microservices.inventory.model.*;
import com.wynh.microservices.inventory.repository.*;
import com.wynh.microservices.inventory.service.InventoryService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceApplicationTests {
    @Mock InventoryRepository inventory;
    @Mock ReservationRepository reservations;
    @InjectMocks InventoryService service;
    private final String number = "f9246419-8be6-40a8-aabe-c675a3c4dd52";

    @Test void reservesAndDeductsStock() {
        var stock = new Inventory(1L, "keyboard", 5);
        when(inventory.lockBySku("keyboard")).thenReturn(Optional.of(stock));
        when(reservations.findById(number)).thenReturn(Optional.empty());
        assertThat(service.reserve(new ReservationRequest(number, "keyboard", 2))).isTrue();
        assertThat(stock.getQuantity()).isEqualTo(3);
        verify(reservations).saveAndFlush(argThat(value -> value.isAccepted() && value.getQuantity() == 2));
    }
    @Test void retryDoesNotDeductTwice() {
        var stock = new Inventory(1L, "keyboard", 3);
        when(inventory.lockBySku("keyboard")).thenReturn(Optional.of(stock));
        when(reservations.findById(number)).thenReturn(Optional.of(new Reservation(number, "keyboard", 2, true)));
        assertThat(service.reserve(new ReservationRequest(number, "keyboard", 2))).isTrue();
        assertThat(stock.getQuantity()).isEqualTo(3);
        verify(reservations, never()).saveAndFlush(any());
    }
    @Test void rejectsInsufficientStockWithoutDeduction() {
        var stock = new Inventory(1L, "keyboard", 1);
        when(inventory.lockBySku("keyboard")).thenReturn(Optional.of(stock));
        when(reservations.findById(number)).thenReturn(Optional.empty());
        assertThat(service.reserve(new ReservationRequest(number, "keyboard", 2))).isFalse();
        assertThat(stock.getQuantity()).isEqualTo(1);
        verify(reservations).saveAndFlush(argThat(value -> !value.isAccepted()));
    }
    @Test void rejectsReuseForDifferentQuantity() {
        when(inventory.lockBySku("keyboard")).thenReturn(Optional.of(new Inventory(1L, "keyboard", 3)));
        when(reservations.findById(number)).thenReturn(Optional.of(new Reservation(number, "keyboard", 2, true)));
        assertThatThrownBy(() -> service.reserve(new ReservationRequest(number, "keyboard", 3))).isInstanceOf(ApiException.class);
    }
}