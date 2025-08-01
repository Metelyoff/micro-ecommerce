package com.ecommerce.inventory.events.handlers;

import com.ecommerce.inventory.dtos.ReservedItemDTO;
import com.ecommerce.inventory.events.OrderEvent;
import com.ecommerce.inventory.services.ItemReservationService;
import com.ecommerce.outbox.core.OutboxContext;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.events.OutboxEvent;
import com.ecommerce.outbox.transformers.OutboxPayloadToTypeTransformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ItemReservationAfterOrderCreatedHandlerTest {

    private ItemReservationService itemReservationService;
    private OutboxPayloadToTypeTransformer<OrderEvent> transformer;
    private ItemReservationAfterOrderCreatedHandler handler;

    @BeforeEach
    void setUp() {
        itemReservationService = mock(ItemReservationService.class);
        transformer = mock(OutboxPayloadToTypeTransformer.class);
        handler = new ItemReservationAfterOrderCreatedHandler(itemReservationService, transformer);
    }

    @Test
    void eventName_shouldBeCorrectName() {
        assertThat(handler.eventName()).isEqualTo("OrderService.create");
    }

    @Test
    void status_shouldBeCorrectStatus() {
        assertThat(handler.status()).isEqualTo(OutboxEventStatus.PROCESSED);
    }

    @Test
    void handleEvent_nullOutboxEvent_throwsException() {
        assertThatThrownBy(() -> handler.handleEvent(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order event is null");
    }

    @Test
    void handleEvent_nullPayload_throwsException() {
        OutboxEvent outboxEvent = mock(OutboxEvent.class);
        when(outboxEvent.payload()).thenReturn(null);
        when(transformer.transform(null)).thenReturn(null);

        assertThatThrownBy(() -> handler.handleEvent(outboxEvent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order event is null");
    }

    @Test
    void handleEvent_nullContextId_throwsException() {
        var orderEvent = new OrderEvent(
                UUID.randomUUID().toString(),
                31.5,
                3,
                List.of(),
                "TEST",
                "Test status description",
                Instant.now().toString()
        );

        var outboxEvent = mock(OutboxEvent.class);
        when(outboxEvent.payload()).thenReturn("payload");
        when(transformer.transform("payload")).thenReturn(orderEvent);
        when(outboxEvent.getContextId()).thenReturn(null);

        assertThatThrownBy(() -> handler.handleEvent(outboxEvent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Context id is null");
    }

    @Test
    void handleEvent_nullItems_shouldNotThrowException() {
        var orderId = UUID.randomUUID();
        var outboxEvent = mock(OutboxEvent.class);
        var orderEvent = new OrderEvent(
                UUID.randomUUID().toString(),
                31.5,
                3,
                List.of(),
                "TEST",
                "Test status description",
                Instant.now().toString()
        );

        when(outboxEvent.payload()).thenReturn("payload");
        when(transformer.transform("payload")).thenReturn(orderEvent);
        when(outboxEvent.getContextId()).thenReturn(orderId.toString());

        assertDoesNotThrow(() -> handler.handleEvent(outboxEvent));

        verify(transformer, times(1)).transform(any());

        ArgumentCaptor<Set<ReservedItemDTO>> itemsCaptor = ArgumentCaptor.forClass(Set.class);
        ArgumentCaptor<OutboxContext> contextCaptor = ArgumentCaptor.forClass(OutboxContext.class);

        verify(itemReservationService, times(1)).reserve(contextCaptor.capture(), itemsCaptor.capture());

        assertThat(contextCaptor.getValue()).isNotNull();
        assertThat(contextCaptor.getValue().getContextId()).isNotNull();
        assertThat(contextCaptor.getValue().getContextId()).isEqualTo(orderId.toString());

        var reservedItems = itemsCaptor.getValue();
        assertThat(reservedItems).isEmpty();
    }

    @Test
    void handleEvent_successfulReservation() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        OutboxEvent outboxEvent = mock(OutboxEvent.class);
        OrderEvent.OrderItemDTO item = new OrderEvent.OrderItemDTO(
                UUID.randomUUID().toString(),
                productId.toString(),
                "Test name",
                10.5,
                3
        );
        OrderEvent orderEvent = new OrderEvent(
                UUID.randomUUID().toString(),
                31.5,
                3,
                List.of(item),
                "TEST",
                "Test status description",
                Instant.now().toString()
        );

        when(outboxEvent.payload()).thenReturn("payload");
        when(transformer.transform(any())).thenReturn(orderEvent);
        when(outboxEvent.getContextId()).thenReturn(orderId.toString());

        assertDoesNotThrow(() -> handler.handleEvent(outboxEvent));

        verify(transformer, times(1)).transform(any());
        ArgumentCaptor<Set<ReservedItemDTO>> itemsCaptor = ArgumentCaptor.forClass(Set.class);
        verify(itemReservationService, times(1)).reserve(any(), itemsCaptor.capture());

        Set<ReservedItemDTO> reservedItems = itemsCaptor.getValue();
        assertThat(reservedItems).hasSize(1);

        ReservedItemDTO reservedItem = reservedItems.iterator().next();
        assertThat(reservedItem.getOrderId()).isEqualTo(orderId);
        assertThat(reservedItem.getItemId()).isEqualTo(productId);
        assertThat(reservedItem.getQuantity()).isEqualTo(3);
        assertThat(reservedItem.getPrice()).isEqualTo(10.5);
        assertThat(reservedItem.getStatus()).isNull();
        assertThat(reservedItem.getReservedAt()).isNull();
    }

}