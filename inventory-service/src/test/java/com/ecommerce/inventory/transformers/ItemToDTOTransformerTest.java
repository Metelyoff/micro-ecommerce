package com.ecommerce.inventory.transformers;

import com.ecommerce.inventory.entities.ItemEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ItemToDTOTransformerTest {

    private final ItemToDTOTransformer transformer = new ItemToDTOTransformer();

    @Test
    void transform_shouldReturnNull_whenEntityIsNull() {
        var result = assertDoesNotThrow(() -> transformer.transform(null));
        assertNull(result);
    }

    @Test
    void transform_shouldReturnDTO_whenEntityIsNotNull() {
        var entity = ItemEntity.builder()
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

        var result = assertDoesNotThrow(() -> transformer.transform(entity));

        assertNotNull(result);
        assertEquals(entity.getId(), result.getId());
        assertEquals(entity.getName(), result.getName());
        assertEquals(entity.getQuantity(), result.getQuantity());
        assertEquals(entity.getPrice().doubleValue(), result.getPrice());
        assertEquals(entity.getImage(), result.getImage());
        assertEquals(entity.getCreated(), result.getCreated());
        assertEquals(entity.getCreatedBy(), result.getCreatedBy());
        assertEquals(entity.getUpdated(), result.getUpdated());
        assertEquals(entity.getUpdatedBy(), result.getUpdatedBy());
    }

}