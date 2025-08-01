package com.ecommerce.inventory.transformers;

import com.ecommerce.inventory.dtos.ItemDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class ItemToEntityTransformerTest {

    private final ItemToEntityTransformer transformer = new ItemToEntityTransformer();

    @Test
    void transform_shouldThrowIllegalArgumentException_whenDTOIsNull() {
        assertThatThrownBy(() -> transformer.transform(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ItemDTO cannot be null");
    }

    @Test
    void transform_shouldReturnEntity_whenDTOIsNotNull() {
        var dto = ItemDTO.builder()
                .name("name")
                .image("image")
                .price(10.0)
                .quantity(5)
                .build();

        var result = assertDoesNotThrow(() -> transformer.transform(dto));

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getImage(), result.getImage());
        assertEquals(dto.getPrice(), result.getPrice().doubleValue());
        assertEquals(dto.getQuantity(), result.getQuantity());
        assertNull(result.getCreated());
        assertNull(result.getCreatedBy());
        assertNull(result.getUpdated());
        assertNull(result.getUpdatedBy());
    }

}