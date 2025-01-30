package com.ecommerce.inventory_service.services;

import com.ecommerce.inventory_service.dtos.ItemDTO;
import com.ecommerce.inventory_service.dtos.ReservedItemDTO;
import com.ecommerce.inventory_service.entities.Item;
import com.ecommerce.inventory_service.entities.ReservedItem;
import com.ecommerce.inventory_service.repositories.ItemRepository;
import com.ecommerce.inventory_service.repositories.ItemReservationRepository;
import com.ecommerce.inventory_service.transformers.Transformer;
import com.ecommerce.outbox.annotations.OutboxTransaction;
import com.ecommerce.outbox.core.OutboxContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ecommerce.inventory_service.entities.ReservedItemStatus.*;
import static java.math.RoundingMode.HALF_UP;

@Service
public class ItemReservationService implements ItemReservationServiceSpec {

    private final ItemReservationRepository itemReservationRepository;
    private final ItemRepository itemRepository;
    private final Transformer<ReservedItem, ReservedItemDTO> itemToDTOTransformer;

    public ItemReservationService(
            ItemReservationRepository itemReservationRepository,
            ItemRepository itemRepository,
            Transformer<ReservedItem, ReservedItemDTO> itemToDTOTransformer
    ) {
        this.itemReservationRepository = itemReservationRepository;
        this.itemRepository = itemRepository;
        this.itemToDTOTransformer = itemToDTOTransformer;
    }

    private static final int MAX_RESERVED_ITEMS = 50;

    @OutboxTransaction
    @Override
    public void reserve(OutboxContext orderId, Collection<ReservedItemDTO> reservedItems) {
        UUID orderUUID = UUID.fromString(orderId.getContextId());
        validateInput(orderUUID, reservedItems);
        Collection<UUID> reservedItemIds = extractItemIds(reservedItems);
        Collection<Item> availableItems = itemRepository.findAllById(reservedItemIds);
        Collection<ReservedItem> validatedReservedItems = prepareAndSaveReservedItems(orderUUID, reservedItems, availableItems);

        itemRepository.saveAll(availableItems);
        itemReservationRepository.saveAll(validatedReservedItems);
    }

    private void validateInput(UUID orderId, Collection<ReservedItemDTO> reservedItems) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order id cannot be null");
        }
        if (reservedItems == null || reservedItems.isEmpty()) {
            throw new IllegalArgumentException("Reserved items cannot be null or empty");
        }
        if (reservedItems.size() > MAX_RESERVED_ITEMS) {
            throw new IllegalArgumentException("Maximum number of reserved items exceeded");
        }
    }

    private Collection<UUID> extractItemIds(Collection<ReservedItemDTO> reservedItems) {
        return reservedItems.stream()
                .map(ReservedItemDTO::itemId)
                .collect(Collectors.toSet());
    }

    private Collection<ReservedItem> prepareAndSaveReservedItems(
            UUID orderId,
            Collection<ReservedItemDTO> reservedItems,
            Collection<Item> availableItems
    ) {
        return reservedItems.stream()
                .map(reservedItem -> validateAndPrepareReservedItem(orderId, reservedItem, availableItems))
                .collect(Collectors.toSet());
    }

    private ReservedItem validateAndPrepareReservedItem(
            UUID orderId,
            ReservedItemDTO reservedItem,
            Collection<Item> availableItems
    ) {
        Item matchingItem = findMatchingAvailableItem(reservedItem, availableItems);
        validateItemQuantity(matchingItem, reservedItem);
        validateItemPrice(matchingItem, reservedItem);

        matchingItem.setQuantity(matchingItem.getQuantity() - reservedItem.quantity());
        return new ReservedItem(null, orderId, matchingItem, reservedItem.quantity(), RESERVED, LocalDateTime.now());
    }

    private Item findMatchingAvailableItem(ReservedItemDTO reservedItem, Collection<Item> availableItems) {
        return availableItems.stream()
                .filter(availableItem -> availableItem.getId().equals(reservedItem.itemId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Item by id \"%s\" not found", reservedItem.itemId())));
    }

    private void validateItemQuantity(Item availableItem, ReservedItemDTO reservedItem) {
        if (reservedItem.quantity() > availableItem.getQuantity()) {
            throw new IllegalArgumentException(String.format(
                    "Not enough quantity for item %s. Available: %d, Requested: %d",
                    reservedItem.itemId(), availableItem.getQuantity(), reservedItem.quantity()
            ));
        }
    }

    private void validateItemPrice(Item availableItem, ReservedItemDTO reservedItem) {
        BigDecimal reservedItemPrice = BigDecimal.valueOf(reservedItem.price()).setScale(2, HALF_UP);
        if (availableItem.getPrice().compareTo(reservedItemPrice) != 0) {
            throw new IllegalArgumentException(String.format(
                    "Item price mismatch. Available: %s, Requested: %s",
                    availableItem.getPrice(), reservedItemPrice
            ));
        }
    }

    @OutboxTransaction
    @Override
    public void cancelReservation(OutboxContext orderId) {
        UUID id = Optional.ofNullable(orderId)
                .map(OutboxContext::getContextId)
                .map(UUID::fromString)
                .orElseThrow(() -> new IllegalArgumentException("Order id cannot be null"));

        Collection<ReservedItem> reservedItems = itemReservationRepository.findAllByOrderId(id);
        if (reservedItems.isEmpty()) {
            throw new IllegalArgumentException(String.format("No reserved items found for order ID: %s", orderId));
        }

        reservedItems.forEach(reservedItem -> {
            reservedItem.setStatus(CANCELLED);
            Item item = reservedItem.getItem();
            item.setQuantity(item.getQuantity() + reservedItem.getQuantity());
        });

        itemRepository.saveAll(reservedItems.stream()
                .map(ReservedItem::getItem)
                .collect(Collectors.toSet()));

        itemReservationRepository.saveAll(reservedItems);
    }

    @Override
    public Collection<ReservedItemDTO> findReservedItemsByOrderId(OutboxContext orderId) {
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
