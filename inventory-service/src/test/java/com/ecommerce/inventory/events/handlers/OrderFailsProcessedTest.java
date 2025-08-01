package com.ecommerce.inventory.events.handlers;

import com.ecommerce.inventory.dtos.ReservedItemDTO;
import com.ecommerce.inventory.services.ItemReservationService;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.events.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderFailsProcessedTest {

    private ItemReservationService itemReservationService;
    private OrderFailsProcessed handler;

    @BeforeEach
    void setUp() {
        itemReservationService = mock(ItemReservationService.class);
        handler = new OrderFailsProcessed(itemReservationService);
    }

    @Test
    void eventName_shouldBeCorrectName() {
        assertThat(handler.eventName()).isEqualTo("OrderService.fail");
    }

    @Test
    void status_shouldBeCorrectStatus() {
        assertThat(handler.status()).isEqualTo(OutboxEventStatus.PROCESSED);
    }

    @Test
    void handleEvent_shouldCancelReservation() {
        var reserverItem = new ReservedItemDTO();
        var orderId = UUID.randomUUID().toString();
        var event = mock(OutboxEvent.class);

        when(event.getContextId()).thenReturn(orderId);
        when(itemReservationService.findReservedItemsByOrderId(any())).thenReturn(List.of(reserverItem));

        assertDoesNotThrow(() -> handler.handleEvent(event));

        verify(itemReservationService).cancelReservation(argThat(supplier -> supplier.getContextId().equals(orderId)));
    }

    @Test
    void handleEvent_shouldDoNothingWhenNoItems() {
        var orderId = UUID.randomUUID().toString();
        var event = mock(OutboxEvent.class);

        when(event.getContextId()).thenReturn(orderId);
        when(itemReservationService.findReservedItemsByOrderId(any())).thenReturn(List.of());

        assertDoesNotThrow(() -> handler.handleEvent(event));

        verify(itemReservationService, never()).cancelReservation(any());
    }

    @Test
    void handleEvent_shouldThrowIllegalArgumentExceptionWhenContextNull() {
        var event = mock(OutboxEvent.class);

        when(event.getContextId()).thenReturn(null);

        assertThatThrownBy(() -> handler.handleEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("OutboxEvent aggregateId required to cancel reservation.");
    }

}