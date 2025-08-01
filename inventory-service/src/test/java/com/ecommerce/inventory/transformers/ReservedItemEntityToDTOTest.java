package com.ecommerce.inventory.transformers;

import com.ecommerce.inventory.entities.ItemEntity;
import com.ecommerce.inventory.entities.ReservedItemEntity;
import com.ecommerce.inventory.entities.ReservedItemStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReservedItemEntityToDTOTest {

    private final ReservedItemEntityToDTO transformer = new ReservedItemEntityToDTO();

    @Test
    void transform_shouldReturnNull_whenEntityIsNull() {
        var result = assertDoesNotThrow(() -> transformer.transform(null));
        assertNull(result);
    }

    @Test
    void transform_shouldReturnDTO_whenEntityIsNotNull() {
        var itemEntity = ItemEntity.builder()
                .id(UUID.randomUUID())
                .name("item")
                .quantity(1)
                .price(BigDecimal.ONE)
                .image("image")
                .created(Instant.now())
                .createdBy("SYSTEM")
                .updated(Instant.now())
                .updatedBy("SYSTEM")
                .build();
        var entity = ReservedItemEntity.builder()
                .id(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .item(itemEntity)
                .status(ReservedItemStatus.RESERVED)
                .quantity(1)
                .reservedAt(Instant.now())
                .created(Instant.now())
                .createdBy("SYSTEM")
                .updated(Instant.now())
                .updatedBy("SYSTEM")
                .build();

        var result = assertDoesNotThrow(() -> transformer.transform(entity));

        assertNotNull(result);
        assertEquals(entity.getId(), result.getId());
        assertEquals(entity.getOrderId(), result.getOrderId());
        assertEquals(entity.getItem().getId(), result.getItemId());
        assertEquals(entity.getStatus(), result.getStatus());
        assertEquals(entity.getQuantity(), result.getQuantity());
        assertEquals(entity.getReservedAt(), result.getReservedAt());
        assertEquals(entity.getCreated(), result.getCreated());
        assertEquals(entity.getCreatedBy(), result.getCreatedBy());
        assertEquals(entity.getUpdated(), result.getUpdated());
        assertEquals(entity.getUpdatedBy(), result.getUpdatedBy());
    }

}