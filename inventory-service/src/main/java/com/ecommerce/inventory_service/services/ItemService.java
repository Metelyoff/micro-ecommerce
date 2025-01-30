package com.ecommerce.inventory_service.services;

import com.ecommerce.inventory_service.dtos.ItemDTO;
import com.ecommerce.inventory_service.entities.Item;
import com.ecommerce.inventory_service.repositories.ItemRepository;
import com.ecommerce.inventory_service.transformers.Transformer;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class ItemService implements ItemServiceSpec {

    private final ItemRepository itemRepository;
    private final Transformer<Item, ItemDTO> itemToDTOTransformer;
    private final Transformer<ItemDTO, Item> itemToEntityTransformer;

    public ItemService(
            ItemRepository itemRepository,
            Transformer<Item, ItemDTO> itemToDTOTransformer,
            Transformer<ItemDTO, Item> itemToEntityTransformer
    ) {
        this.itemRepository = itemRepository;
        this.itemToDTOTransformer = itemToDTOTransformer;
        this.itemToEntityTransformer = itemToEntityTransformer;
    }

    @PostConstruct
    public void init() {
        Random random = new Random();
        var randomItems = IntStream.range(0, 100).mapToObj(i -> new Item(
                null,
                "Item-" + i,
                random.nextInt(50) + 1,
                String.format("https://picsum.photos/id/%s/400/300", i),
                BigDecimal.valueOf(random.nextDouble() * 100.0)
                        .setScale(2, RoundingMode.HALF_UP)
        )).toList();
        itemRepository.saveAll(randomItems);
    }

    @Override
    public Collection<ItemDTO> findAll() {
        return itemRepository.findAll().stream().map(itemToDTOTransformer::transform).toList();
    }

    @Override
    public Collection<ItemDTO> findAllByIds(Collection<String> ids) {
        return itemRepository.findAllByIdIn(ids.stream().map(UUID::fromString).toList())
                .stream()
                .map(itemToDTOTransformer::transform)
                .toList();
    }

    @Override
    public ItemDTO findById(String id) {
        return Optional.ofNullable(id)
                .map(UUID::fromString)
                .flatMap(itemRepository::findById)
                .map(itemToDTOTransformer::transform)
                .orElseThrow(() -> new IllegalArgumentException("Item with id " + id + " not found"));
    }

    @Override
    public ItemDTO save(ItemDTO itemDTO) {
        return Optional.ofNullable(itemDTO)
                .map(itemToEntityTransformer::transform)
                .map(itemRepository::save)
                .map(itemToDTOTransformer::transform)
                .orElseThrow(() -> new IllegalArgumentException("ItemDTO cannot be null"));
    }

}
