package com.ecommerce.inventory.events.handlers;

import com.ecommerce.inventory.services.ItemReservationService;
import com.ecommerce.outbox.core.OutboxContext;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.events.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderCanceledHandlerTest {

    private ItemReservationService itemReservationService;
    private OrderCanceledHandler handler;

    @BeforeEach
    void setUp() {
        itemReservationService = mock(ItemReservationService.class);
        handler = new OrderCanceledHandler(itemReservationService);
    }

    @Test
    void eventName_shouldBeCorrectName() {
        assertThat(handler.eventName()).isEqualTo("OrderService.cancelByReason");
    }

    @Test
    void status_shouldBeCorrectStatus() {
        assertThat(handler.status()).isEqualTo(OutboxEventStatus.PROCESSED);
    }

    @Test
    void handleEvent_validContextId_shouldCallCancelReservation() {
        var contextId = UUID.randomUUID().toString();
        var event = mock(OutboxEvent.class);
        when(event.getContextId()).thenReturn(contextId);

        assertDoesNotThrow(() -> handler.handleEvent(event));

        var captor = ArgumentCaptor.forClass(OutboxContext.class);
        verify(itemReservationService, times(1)).cancelReservation(captor.capture());

        assertThat(captor.getValue().getContextId()).isEqualTo(contextId);
    }

    @Test
    void handleEvent_nullContextId_shouldThrowException() {
        var event = mock(OutboxEvent.class);
        when(event.getContextId()).thenReturn(null);

        assertThatThrownBy(() -> handler.handleEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("OutboxEvent aggregateId required to cancel reservation.");

        verifyNoInteractions(itemReservationService);
    }

}