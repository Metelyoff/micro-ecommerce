package com.ecommerce.inventory.repositories;

import com.ecommerce.inventory.AbstractIntegrationTest;
import com.ecommerce.inventory.entities.ItemEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql(value = "/sql/items.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class ItemRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void findAllByIdIn_shouldReturnMatchingItems() {
        var ids = Set.of(
                "a93383c5-565b-4f7e-b8e3-ad5511d8b261",
                "c1daddc1-23fb-40f8-b908-34b490b0e7b9",
                "6cd2e2f7-0043-4055-9387-1c1b5155cbfd"
        ).stream().map(UUID::fromString).collect(Collectors.toSet());

        var foundItems = itemRepository.findAllByIdIn(ids);

        assertThat(foundItems)
                .hasSize(3)
                .extracting(ItemEntity::getName)
                .containsExactlyInAnyOrder("Test 1", "Test 2", "Test 3");
    }

    @Test
    void findAllByIdIn_shouldReturnEmpty_whenNoMatch() {
        var foundItems = itemRepository.findAllByIdIn(List.of(UUID.randomUUID()));

        assertThat(foundItems).isEmpty();
    }

}