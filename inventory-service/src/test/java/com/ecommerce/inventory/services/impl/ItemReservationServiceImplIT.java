package com.ecommerce.inventory.services.impl;

import com.ecommerce.inventory.AbstractIntegrationTest;
import com.ecommerce.inventory.dtos.ReservedItemDTO;
import com.ecommerce.inventory.entities.ItemEntity;
import com.ecommerce.inventory.entities.ReservedItemEntity;
import com.ecommerce.inventory.exceptions.ItemBadRequestException;
import com.ecommerce.inventory.repositories.ItemRepository;
import com.ecommerce.inventory.repositories.ItemReservationRepository;
import com.ecommerce.inventory.services.ItemReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.ecommerce.common.persistence.entities.BaseEntity.MONEY_ROUNDING_MODE;
import static com.ecommerce.common.persistence.entities.BaseEntity.MONEY_SCALE;
import static com.ecommerce.inventory.entities.ReservedItemStatus.CANCELLED;
import static com.ecommerce.inventory.entities.ReservedItemStatus.RESERVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Sql(value = {
        "/sql/items.sql",
        "/sql/reserved_items.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
@SpringBootTest
class ItemReservationServiceImplIT extends AbstractIntegrationTest {

    @Autowired
    private ItemReservationService itemReservationService;

    @Autowired
    private ItemReservationRepository itemReservationRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Value("${spring.application.max-reserved-items}")
    private Integer maxReservedItems;

    @Test
    void reserve_shouldThrowException_whenItemsIsNull() {
        assertThatThrownBy(() -> itemReservationService.reserve(UUID.randomUUID()::toString, null))
                .isInstanceOf(ItemBadRequestException.class)
                .hasMessage("Reserved items cannot be null or empty");
    }

    @Test
    void reserve_shouldThrowException_whenTooManyItems() {
        var item = itemRepository.findById(UUID.fromString("a93383c5-565b-4f7e-b8e3-ad5511d8b261")).orElseThrow();
        var orderId = UUID.randomUUID();
        var tooMany = IntStream.range(1, maxReservedItems + 2)
                .mapToObj(i -> new ReservedItemDTO(
                        item.getId(),
                        orderId,
                        item.getPrice().doubleValue(),
                        1,
                        null,
                        null
                )).toList();

        assertThatThrownBy(() -> itemReservationService.reserve(orderId::toString, tooMany))
                .isInstanceOf(ItemBadRequestException.class)
                .hasMessage(String.format(
                        "Maximum number of reserved items exceeded. Allowed: [%d]",
                        maxReservedItems
                ));
    }

    @Test
    void reserve_shouldThrowException_whenItemQuantityTooLow() {
        var item = itemRepository.findById(UUID.fromString("a93383c5-565b-4f7e-b8e3-ad5511d8b261")).orElseThrow();
        var orderId = UUID.randomUUID();
        var dto = new ReservedItemDTO(
                item.getId(),
                orderId,
                item.getPrice().doubleValue(),
                item.getQuantity() + 1,
                null,
                null
        );

        assertThatThrownBy(() -> itemReservationService.reserve(orderId::toString, List.of(dto)))
                .isInstanceOf(ItemBadRequestException.class)
                .hasMessage(String.format(
                        "Not enough quantity for item %s. Available: %d, Requested: %d",
                        dto.getItemId(), item.getQuantity(), dto.getQuantity()
                ));
    }

    @Test
    void reserve_shouldSaveReservedItems_whenValidInput() {
        var item = itemRepository.findById(UUID.fromString("a93383c5-565b-4f7e-b8e3-ad5511d8b261")).orElseThrow();
        var orderId = UUID.randomUUID();
        var dto = new ReservedItemDTO(
                item.getId(),
                orderId,
                item.getPrice().doubleValue(),
                1,
                null,
                null
        );

        assertDoesNotThrow(() -> itemReservationService.reserve(orderId::toString, List.of(dto)));

        var results = itemReservationRepository.findAllByOrderId(orderId);
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);

        var result = results.stream().findFirst().orElseThrow();
        assertThat(result.getItem()).isEqualTo(item);
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getStatus()).isEqualTo(RESERVED);
    }

    @Test
    void reserve_shouldThrowException_whenPriceMismatch() {
        var item = itemRepository.findById(UUID.fromString("a93383c5-565b-4f7e-b8e3-ad5511d8b261")).orElseThrow();
        var orderId = UUID.randomUUID();
        var dto = new ReservedItemDTO(
                item.getId(),
                orderId,
                1.0,
                1,
                null,
                null
        );

        assertThatThrownBy(() -> itemReservationService.reserve(orderId::toString, List.of(dto)))
                .isInstanceOf(ItemBadRequestException.class)
                .hasMessage(String.format(
                        "Item price mismatch. Available: %s, Requested: %s",
                        item.getPrice(), BigDecimal.valueOf(dto.getPrice()).setScale(MONEY_SCALE, MONEY_ROUNDING_MODE)
                ));
    }

    @Test
    void cancelReservation_shouldThrowException_whenOrderIdNotFound() {
        var orderId = UUID.randomUUID();

        assertThatThrownBy(() -> itemReservationService.cancelReservation(orderId::toString))
                .isInstanceOf(ItemBadRequestException.class)
                .hasMessage(String.format("No reserved items found for order ID: %s", orderId));
    }

    @Transactional
    @Test
    void cancelReservation_shouldRevertQuantityAndUpdateStatus() {
        var orderId = UUID.fromString("06b58c2c-79ab-4b5a-9ff4-05dc368d8388");
        var reservedBefore = itemReservationRepository.findAllByOrderId(orderId);
        var quantityBefore = reservedBefore.stream()
                .map(ReservedItemEntity::getItem)
                .distinct()
                .map(ItemEntity::getQuantity)
                .reduce(Integer::sum)
                .orElseThrow();

        assertDoesNotThrow(() -> itemReservationService.cancelReservation(orderId::toString));

        var reservedAfter = itemReservationRepository.findAllByOrderId(orderId);
        assertThat(reservedAfter).isNotEmpty();
        assertThat(reservedAfter.size()).isEqualTo(3);
        assertThat(reservedAfter)
                .extracting(ReservedItemEntity::getStatus)
                .contains(CANCELLED);
        var quantityAfter = reservedAfter.stream()
                .map(ReservedItemEntity::getItem)
                .distinct()
                .map(ItemEntity::getQuantity)
                .reduce(Integer::sum)
                .orElseThrow();
        var reservedAfterQuantity = reservedAfter.stream()
                .map(ReservedItemEntity::getQuantity)
                .reduce(Integer::sum)
                .orElseThrow();
        assertThat(quantityAfter).isEqualTo(reservedAfterQuantity + quantityBefore);
    }

    @Transactional
    @Test
    void findReservedItemsByOrderId_shouldReturnItems_whenExist() {
        var orderId = UUID.fromString("02ec61cd-2c89-45aa-aa6d-eed6ffcbf1e5");

        var result = assertDoesNotThrow(() -> itemReservationService.findReservedItemsByOrderId(orderId::toString));

        assertThat(result).isNotEmpty();
        assertThat(result)
                .extracting(ReservedItemDTO::getOrderId)
                .contains(orderId);
    }

    @Test
    void findReservedItemsByOrderId_shouldReturnEmpty_whenNotFound() {
        var orderId = UUID.randomUUID();

        var result = assertDoesNotThrow(() -> itemReservationService.findReservedItemsByOrderId(orderId::toString));

        assertThat(result).isEmpty();
    }

}