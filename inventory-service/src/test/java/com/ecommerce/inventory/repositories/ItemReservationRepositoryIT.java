package com.ecommerce.inventory.repositories;

import com.ecommerce.inventory.AbstractIntegrationTest;
import com.ecommerce.inventory.entities.ItemEntity;
import com.ecommerce.inventory.entities.ReservedItemEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DataJpaTest
@Sql(value = {
        "/sql/items.sql",
        "/sql/reserved_items.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class ItemReservationRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private ItemReservationRepository itemReservationRepository;

    @Test
    void findAllByOrderId_shouldReturnMatchingReservedItems() {
        var orderId = UUID.fromString("06b58c2c-79ab-4b5a-9ff4-05dc368d8388");

        var foundReservedItems = assertDoesNotThrow(() -> itemReservationRepository.findAllByOrderId(orderId));

        assertThat(foundReservedItems)
                .hasSize(3)
                .extracting(ReservedItemEntity::getItem)
                .extracting(ItemEntity::getName)
                .containsExactlyInAnyOrder("Test 1", "Test 1", "Test 2");
    }

    @Test
    void findAllByOrderId_shouldReturnEmpty_whenNoMatch() {
        var orderId = UUID.randomUUID();

        var foundReservedItems = assertDoesNotThrow(() -> itemReservationRepository.findAllByOrderId(orderId));

        assertThat(foundReservedItems).isEmpty();
    }

}