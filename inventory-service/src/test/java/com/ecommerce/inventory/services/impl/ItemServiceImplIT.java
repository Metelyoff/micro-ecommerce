package com.ecommerce.inventory.services.impl;

import com.ecommerce.inventory.AbstractIntegrationTest;
import com.ecommerce.inventory.dtos.ItemDTO;
import com.ecommerce.inventory.exceptions.ItemBadRequestException;
import com.ecommerce.inventory.exceptions.ItemNotFoundException;
import com.ecommerce.inventory.services.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Sql(value = "/sql/items.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
@SpringBootTest
class ItemServiceImplIT extends AbstractIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Test
    void findAll_shouldReturnItems() {
        var items = assertDoesNotThrow(() -> itemService.findAll());
        assertThat(items).hasSize(110);
    }

    @Test
    void findAllByIds_shouldReturnMatchingItems() {
        var ids = Stream.of(
                "a93383c5-565b-4f7e-b8e3-ad5511d8b261",
                "c1daddc1-23fb-40f8-b908-34b490b0e7b9",
                "6cd2e2f7-0043-4055-9387-1c1b5155cbfd",
                "a7aa26d3-861a-4f2d-b653-08143862c653",
                "14f6a8b9-c3b0-466b-9098-a27ee16bb131"
        ).map(UUID::fromString).toList();

        var result = assertDoesNotThrow(() -> itemService.findAllByIds(ids));

        assertThat(result).hasSize(5);
        assertThat(result).extracting(ItemDTO::getId).containsAll(ids);
    }

    @Test
    void findById_shouldReturnItem() {
        var id = UUID.fromString("3bc6b8c9-9d69-4530-816e-6a85526439fd");

        var item = assertDoesNotThrow(() -> itemService.findById(id));

        assertThat(item).isNotNull();
        assertThat(item.getId()).isEqualTo(id);
    }

    @Test
    void findById_shouldThrowWhenNullId() {
        assertThatThrownBy(() -> itemService.findById(null))
                .isInstanceOf(ItemBadRequestException.class)
                .hasMessageContaining("Item id required");
    }

    @Test
    void findById_shouldThrowWhenItemNotFound() {
        var unknownId = UUID.randomUUID();

        assertThatThrownBy(() -> itemService.findById(unknownId))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining(unknownId.toString());
    }

    @Test
    void save_shouldPersistNewItem() {
        var newItem = new ItemDTO("New item", "https://image.jpg", 99.99, 5);
        var saved = assertDoesNotThrow(() -> itemService.save(newItem));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("New item");
        assertThat(saved.getPrice()).isEqualTo(99.99);
        assertThat(saved.getQuantity()).isEqualTo(5);
    }

    @Test
    void save_shouldThrowWhenNullItem() {
        assertThatThrownBy(() -> itemService.save(null))
                .isInstanceOf(ItemBadRequestException.class)
                .hasMessageContaining("ItemDTO cannot be null");
    }

}