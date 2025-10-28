package com.ecommerce.inventory.services.impl;

import com.ecommerce.inventory.configs.CacheConfig;
import com.ecommerce.inventory.dtos.ReservedItemDTO;
import com.ecommerce.inventory.entities.ItemEntity;
import com.ecommerce.inventory.entities.ReservedItemEntity;
import com.ecommerce.inventory.exceptions.ItemBadRequestException;
import com.ecommerce.inventory.exceptions.ItemNotFoundException;
import com.ecommerce.inventory.repositories.ItemRepository;
import com.ecommerce.inventory.repositories.ItemReservationRepository;
import com.ecommerce.inventory.services.ItemReservationService;
import com.ecommerce.inventory.transformers.Transformer;
import com.ecommerce.outbox.annotations.OutboxTransaction;
import com.ecommerce.outbox.core.OutboxContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ecommerce.common.persistence.entities.BaseEntity.MONEY_ROUNDING_MODE;
import static com.ecommerce.common.persistence.entities.BaseEntity.MONEY_SCALE;
import static com.ecommerce.inventory.entities.ReservedItemStatus.*;

@Slf4j
@Service
public class ItemReservationServiceImpl implements ItemReservationService {

    private final ItemReservationRepository itemReservationRepository;
    private final ItemRepository itemRepository;
    private final Transformer<ReservedItemEntity, ReservedItemDTO> itemToDTOTransformer;
    private final Integer maxReservedItems;

    public ItemReservationServiceImpl(
            final ItemReservationRepository itemReservationRepository,
            final ItemRepository itemRepository,
            final Transformer<ReservedItemEntity, ReservedItemDTO> itemToDTOTransformer,
            @Value("${spring.application.max-reserved-items}") final Integer maxReservedItems
    ) {
        this.itemReservationRepository = itemReservationRepository;
        this.itemRepository = itemRepository;
        this.itemToDTOTransformer = itemToDTOTransformer;
        this.maxReservedItems = maxReservedItems;
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.ITEMS_ALL, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.ITEM_BY_ID, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.RESERVED_ITEMS_BY_ORDER, key = "#orderId?.contextId", beforeInvocation = true)
    })
    @OutboxTransaction(successEvent = "ItemReservationService.reserve")
    @Override
    public void reserve(OutboxContext orderId, Collection<ReservedItemDTO> reservedItems) {
        log.debug("Reserve Items from Order Id: {}. Items to reserve: [{}]", orderId, reservedItems);

        var orderUUID = Optional.ofNullable(orderId)
                .map(OutboxContext::getContextId)
                .map(UUID::fromString)
                .orElseThrow(() -> new ItemBadRequestException("Order id cannot be null"));

        validateItems(reservedItems);

        var reservedItemIds = extractItemIds(reservedItems);
        var availableItems = itemRepository.findAllById(reservedItemIds);
        var validatedReservedItems = prepareReservedItems(orderUUID, reservedItems, availableItems);

        itemRepository.saveAll(availableItems);
        itemReservationRepository.saveAll(validatedReservedItems);
    }

    private void validateItems(Collection<ReservedItemDTO> reservedItems) {
        if (reservedItems == null || reservedItems.isEmpty()) {
            throw new ItemBadRequestException("Reserved items cannot be null or empty");
        }
        if (reservedItems.size() > maxReservedItems) {
            throw new ItemBadRequestException(String.format(
                    "Maximum number of reserved items exceeded. Allowed: [%d]",
                    maxReservedItems
            ));
        }
    }

    private Collection<UUID> extractItemIds(Collection<ReservedItemDTO> reservedItems) {
        return reservedItems.stream()
                .map(ReservedItemDTO::getItemId)
                .collect(Collectors.toSet());
    }

    private Collection<ReservedItemEntity> prepareReservedItems(
            UUID orderId,
            Collection<ReservedItemDTO> reservedItems,
            Collection<ItemEntity> availableItems
    ) {
        return reservedItems.stream()
                .map(item -> validateAndPrepareReservedItem(orderId, item, availableItems))
                .collect(Collectors.toSet());
    }

    private ReservedItemEntity validateAndPrepareReservedItem(
            UUID orderId,
            ReservedItemDTO reservedItem,
            Collection<ItemEntity> availableItems
    ) {
        var matchingItem = findMatchingAvailableItem(reservedItem, availableItems);

        validateItemQuantity(matchingItem, reservedItem);
        validateItemPrice(matchingItem, reservedItem);

        matchingItem.setQuantity(matchingItem.getQuantity() - reservedItem.getQuantity());
        return new ReservedItemEntity(orderId, matchingItem, reservedItem.getQuantity(), RESERVED, Instant.now());
    }

    private ItemEntity findMatchingAvailableItem(ReservedItemDTO reservedItem, Collection<ItemEntity> availableItems) {
        return availableItems.stream()
                .filter(availableItem -> availableItem.getId().equals(reservedItem.getItemId()))
                .findFirst()
                .orElseThrow(() -> new ItemNotFoundException(String.format("Item by id %s not found", reservedItem.getItemId())));
    }

    private void validateItemQuantity(ItemEntity availableItem, ReservedItemDTO reservedItem) {
        if (reservedItem.getQuantity() > availableItem.getQuantity()) {
            throw new ItemBadRequestException(String.format(
                    "Not enough quantity for item %s. Available: %d, Requested: %d",
                    reservedItem.getItemId(), availableItem.getQuantity(), reservedItem.getQuantity()
            ));
        }
    }

    private void validateItemPrice(ItemEntity availableItem, ReservedItemDTO reservedItem) {
        var reservedItemPrice = BigDecimal.valueOf(reservedItem.getPrice()).setScale(MONEY_SCALE, MONEY_ROUNDING_MODE);
        if (availableItem.getPrice().compareTo(reservedItemPrice) != 0) {
            throw new ItemBadRequestException(String.format(
                    "Item price mismatch. Available: %s, Requested: %s",
                    availableItem.getPrice(), reservedItemPrice
            ));
        }
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.ITEMS_ALL, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.ITEM_BY_ID, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.RESERVED_ITEMS_BY_ORDER, key = "#orderId?.contextId", beforeInvocation = true)
    })
    @OutboxTransaction(successEvent = "ItemReservationService.cancelReservation")
    @Override
    public void cancelReservation(OutboxContext orderId) {
        log.debug("Cancel Reservation for Order Id: {}", orderId);

        var id = Optional.ofNullable(orderId)
                .map(OutboxContext::getContextId)
                .map(UUID::fromString)
                .orElseThrow(() -> new ItemBadRequestException("Order id cannot be null"));

        var reservedItems = itemReservationRepository.findAllByOrderId(id);
        if (reservedItems.isEmpty()) {
            throw new ItemBadRequestException(String.format("No reserved items found for order ID: %s", id));
        }

        reservedItems.forEach(reservedItem -> {
            reservedItem.setStatus(CANCELLED);
            ItemEntity item = reservedItem.getItem();
            item.setQuantity(item.getQuantity() + reservedItem.getQuantity());
        });

        itemRepository.saveAll(reservedItems.stream()
                .map(ReservedItemEntity::getItem)
                .collect(Collectors.toSet()));

        itemReservationRepository.saveAll(reservedItems);
    }

    @Override
    @Cacheable(cacheNames = CacheConfig.RESERVED_ITEMS_BY_ORDER, key = "#orderId?.contextId")
    public Collection<ReservedItemDTO> findReservedItemsByOrderId(OutboxContext orderId) {
        log.debug("Find Reserved Items for Order Id: {}", orderId);
        return Optional.ofNullable(orderId)
                .map(OutboxContext::getContextId)
                .map(UUID::fromString)
                .map(itemReservationRepository::findAllByOrderId)
                .stream()
                .flatMap(Collection::stream)
                .map(itemToDTOTransformer::transform)
                .collect(Collectors.toSet());
    }

}
