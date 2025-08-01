package com.ecommerce.inventory.services.impl;

import com.ecommerce.inventory.dtos.ReservedItemDTO;
import com.ecommerce.inventory.entities.ItemEntity;
import com.ecommerce.inventory.entities.ReservedItemEntity;
import com.ecommerce.inventory.entities.ReservedItemStatus;
import com.ecommerce.inventory.exceptions.ItemBadRequestException;
import com.ecommerce.inventory.exceptions.ItemNotFoundException;
import com.ecommerce.inventory.repositories.ItemRepository;
import com.ecommerce.inventory.repositories.ItemReservationRepository;
import com.ecommerce.inventory.transformers.ReservedItemEntityToDTO;
import com.ecommerce.inventory.transformers.Transformer;
import com.ecommerce.outbox.core.OutboxContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class ItemReservationServiceImplTest {

    private ItemRepository itemRepository;
    private ItemReservationRepository reservationRepository;
    private Transformer<ReservedItemEntity, ReservedItemDTO> transformer;

    private ItemReservationServiceImpl service;

    @BeforeEach
    void setup() {
        itemRepository = mock(ItemRepository.class);
        reservationRepository = mock(ItemReservationRepository.class);
        transformer = mock(ReservedItemEntityToDTO.class);
        service = new ItemReservationServiceImpl(reservationRepository, itemRepository, transformer, 50);
    }

    @Test
    void reserve_shouldSaveReservedItemsSuccessfully() {
        UUID orderId = UUID.randomUUID();
        OutboxContext ctx = orderId::toString;

        ReservedItemDTO dto = new ReservedItemDTO(
                UUID.randomUUID(),
                orderId,
                BigDecimal.TEN.doubleValue(),
                2,
                ReservedItemStatus.RESERVED,
                Instant.now()
        );
        ItemEntity item = new ItemEntity("Item", 5, "img", BigDecimal.TEN);
        item.setId(dto.getItemId());

        Set<UUID> itemIds = Set.of(dto.getItemId());

        when(itemRepository.findAllById(itemIds)).thenReturn(List.of(item));

        assertDoesNotThrow(() -> service.reserve(ctx, List.of(dto)));

        assertThat(item.getQuantity()).isEqualTo(3);

        verify(itemRepository, times(1)).findAllById(itemIds);
        verify(itemRepository, times(1)).saveAll(anyCollection());
        verify(reservationRepository, only()).saveAll(anyCollection());
    }

    @Test
    void reserve_shouldThrowItemBadRequestException_whenOrderIdNull() {
        assertThatThrownBy(() -> service.reserve(null, List.of()))
                .isInstanceOf(ItemBadRequestException.class)
                .hasMessageContaining("Order id cannot be null");

        verify(itemRepository, never()).findAllById(any());
        verify(itemRepository, never()).saveAll(anyCollection());
        verify(reservationRepository, never()).saveAll(anyCollection());
    }

    @Test
    void reserve_shouldThrowThrowItemBadRequestException_whenItemsNullOrEmpty() {
        OutboxContext ctx = () -> UUID.randomUUID().toString();

        assertThatThrownBy(() -> service.reserve(ctx, null))
                .isInstanceOf(ItemBadRequestException.class)
                .hasMessageContaining("Reserved items cannot be null or empty");

        assertThatThrownBy(() -> service.reserve(ctx, List.of()))
                .isInstanceOf(ItemBadRequestException.class)
                .hasMessageContaining("Reserved items cannot be null or empty");

        verify(itemRepository, never()).findAllById(any());
        verify(itemRepository, never()).saveAll(anyCollection());
        verify(reservationRepository, never()).saveAll(anyCollection());
    }

    @Test
    void reserve_shouldThrowItemBadRequestException_whenItemsCountExceededLimit() {
        UUID orderId = UUID.randomUUID();
        OutboxContext ctx = orderId::toString;
        var reserverItems = IntStream.range(0, 51).mapToObj(i -> new ReservedItemDTO(
                UUID.randomUUID(),
                orderId,
                BigDecimal.TEN.doubleValue(),
                2,
                ReservedItemStatus.RESERVED,
                Instant.now()
        )).toList();

        assertThatThrownBy(() -> service.reserve(ctx, reserverItems))
                .isInstanceOf(ItemBadRequestException.class)
                .hasMessageContaining("Maximum number of reserved items exceeded");

        verify(itemRepository, never()).findAllById(anyCollection());
        verify(itemRepository, never()).saveAll(anyCollection());
        verify(reservationRepository, never()).saveAll(anyCollection());
    }

    @Test
    void reserve_shouldThrowItemNotFoundException_whenItemNotFound() {
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        ReservedItemDTO dto = new ReservedItemDTO(
                itemId,
                orderId,
                BigDecimal.TEN.doubleValue(),
                2,
                ReservedItemStatus.RESERVED,
                Instant.now()
        );
        OutboxContext ctx = orderId::toString;
        Set<UUID> itemIds = Set.of(itemId);

        when(itemRepository.findAllById(itemIds)).thenReturn(List.of());

        assertThatThrownBy(() -> service.reserve(ctx, List.of(dto)))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining(String.format("Item by id %s not found", itemId));

        verify(itemRepository, only()).findAllById(itemIds);
        verify(itemRepository, never()).saveAll(anyCollection());
        verify(reservationRepository, never()).saveAll(anyCollection());
    }

    @Test
    void reserve_shouldThrowItemBadRequestException_whenQuantityInsufficient() {
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        ReservedItemDTO dto = new ReservedItemDTO(
                itemId,
                orderId,
                BigDecimal.TEN.doubleValue(),
                10,
                ReservedItemStatus.RESERVED,
                Instant.now()
        );
        OutboxContext ctx = orderId::toString;
        ItemEntity item = new ItemEntity("Item", 5, "img", BigDecimal.TEN);
        item.setId(itemId);

        Set<UUID> itemIds = Set.of(itemId);

        when(itemRepository.findAllById(itemIds)).thenReturn(List.of(item));

        assertThatThrownBy(() -> service.reserve(ctx, List.of(dto)))
                .isInstanceOf(ItemBadRequestException.class)
                .hasMessageContaining(String.format(
                        "Not enough quantity for item %s. Available: %d, Requested: %d",
                        item.getId(), item.getQuantity(), dto.getQuantity()
                ));

        verify(itemRepository, only()).findAllById(itemIds);
        verify(itemRepository, never()).saveAll(anyCollection());
        verify(reservationRepository, never()).saveAll(anyCollection());
    }

    @Test
    void reserve_shouldThrowItemBadRequestException_whenPriceMismatch() {
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        ReservedItemDTO dto = new ReservedItemDTO(
                itemId,
                orderId,
                BigDecimal.TEN.doubleValue(),
                5,
                ReservedItemStatus.RESERVED,
                Instant.now()
        );
        OutboxContext ctx = orderId::toString;
        ItemEntity item = new ItemEntity("Item", 15, "img", BigDecimal.ONE);
        item.setId(itemId);

        Set<UUID> itemIds = Set.of(itemId);

        when(itemRepository.findAllById(itemIds)).thenReturn(List.of(item));

        assertThatThrownBy(() -> service.reserve(ctx, List.of(dto)))
                .isInstanceOf(ItemBadRequestException.class)
                .hasMessageContaining("Item price mismatch");

        verify(itemRepository, only()).findAllById(itemIds);
        verify(itemRepository, never()).saveAll(anyCollection());
        verify(reservationRepository, never()).saveAll(anyCollection());
    }

    @Test
    void cancelReservation_shouldCancelAllItemsAndUpdateQuantities() {
        UUID orderId = UUID.randomUUID();
        OutboxContext ctx = orderId::toString;

        ItemEntity item = new ItemEntity("Item", 5, "img", BigDecimal.TEN);
        ReservedItemEntity reserved = new ReservedItemEntity(
                orderId,
                item,
                2,
                ReservedItemStatus.RESERVED,
                Instant.now()
        );

        when(reservationRepository.findAllByOrderId(orderId)).thenReturn(List.of(reserved));

        assertDoesNotThrow(() -> service.cancelReservation(ctx));

        assertThat(reserved.getStatus()).isEqualTo(ReservedItemStatus.CANCELLED);
        assertThat(item.getQuantity()).isEqualTo(7);

        verify(reservationRepository, times(1)).findAllByOrderId(orderId);
        verify(itemRepository, times(1)).saveAll(Set.of(item));
        verify(reservationRepository, times(1)).saveAll(List.of(reserved));
    }

    @Test
    void cancelReservation_shouldThrowItemBadRequestException_whenNoOrderId() {
        assertThatThrownBy(() -> service.cancelReservation(null))
                .isInstanceOf(ItemBadRequestException.class)
                .hasMessageContaining("Order id cannot be null");

        verify(reservationRepository, never()).findAllByOrderId(any());
        verify(itemRepository, never()).saveAll(anyCollection());
        verify(reservationRepository, never()).saveAll(anyCollection());
    }

    @Test
    void cancelReservation_shouldThrowItemBadRequestException_whenNothingToCancel() {
        UUID orderId = UUID.randomUUID();
        OutboxContext ctx = orderId::toString;

        when(reservationRepository.findAllByOrderId(orderId)).thenReturn(List.of());

        assertThatThrownBy(() -> service.cancelReservation(ctx))
                .isInstanceOf(ItemBadRequestException.class)
                .hasMessageContaining(String.format("No reserved items found for order ID: %s", orderId));

        verify(reservationRepository, only()).findAllByOrderId(orderId);
        verify(itemRepository, never()).saveAll(anyCollection());
        verify(reservationRepository, never()).saveAll(anyCollection());
    }

    @Test
    void findReservedItemsByOrderId_shouldReturnDTOs() {
        UUID orderId = UUID.randomUUID();
        OutboxContext ctx = orderId::toString;

        ReservedItemEntity entity = new ReservedItemEntity();
        ReservedItemDTO dto = new ReservedItemDTO();

        when(reservationRepository.findAllByOrderId(orderId)).thenReturn(List.of(entity));
        when(transformer.transform(entity)).thenReturn(dto);

        var result = assertDoesNotThrow(() -> service.findReservedItemsByOrderId(ctx));

        assertThat(result).containsExactly(dto);

        verify(reservationRepository, only()).findAllByOrderId(orderId);
        verify(transformer, only()).transform(entity);
    }

    @Test
    void findReservedItemsByOrderId_shouldReturnEmpty_whenNullContext() {
        var result = assertDoesNotThrow(() -> service.findReservedItemsByOrderId(null));

        assertThat(result).isEmpty();

        verify(reservationRepository, never()).findAllByOrderId(any());
        verify(transformer, never()).transform(any());
    }

    @Test
    void findReservedItemsByOrderId_shouldReturnEmpty_whenNotFound() {
        UUID orderId = UUID.randomUUID();
        OutboxContext ctx = orderId::toString;

        when(reservationRepository.findAllByOrderId(orderId)).thenReturn(List.of());

        var result = assertDoesNotThrow(() -> service.findReservedItemsByOrderId(ctx));

        assertThat(result).isEmpty();

        verify(reservationRepository, only()).findAllByOrderId(orderId);
        verify(transformer, never()).transform(any());
    }

}