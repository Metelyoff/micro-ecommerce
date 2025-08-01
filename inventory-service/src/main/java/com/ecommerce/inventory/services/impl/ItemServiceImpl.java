package com.ecommerce.inventory.services.impl;

import com.ecommerce.inventory.dtos.ItemDTO;
import com.ecommerce.inventory.entities.ItemEntity;
import com.ecommerce.inventory.exceptions.ItemBadRequestException;
import com.ecommerce.inventory.exceptions.ItemNotFoundException;
import com.ecommerce.inventory.repositories.ItemRepository;
import com.ecommerce.inventory.services.ItemService;
import com.ecommerce.inventory.transformers.Transformer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.ecommerce.common.persistence.entities.BaseEntity.MONEY_ROUNDING_MODE;
import static com.ecommerce.common.persistence.entities.BaseEntity.MONEY_SCALE;

@Slf4j
@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final Transformer<ItemEntity, ItemDTO> itemToDTOTransformer;
    private final Transformer<ItemDTO, ItemEntity> itemToEntityTransformer;

    @PostConstruct
    public void init() {
        log.debug("Init items");
        if (itemRepository.count() == 0) {
            log.debug("Creating items");
            var random = new Random();
            var randomItems = IntStream.range(1, 101).mapToObj(i -> new ItemEntity(
                    "Item " + i,
                    random.nextInt(50) + 1,
                    String.format("https://picsum.photos/id/%s/400/300", i),
                    BigDecimal.valueOf(random.nextDouble() * 100.0).setScale(MONEY_SCALE, MONEY_ROUNDING_MODE)
            )).toList();
            itemRepository.saveAll(randomItems);
        }
    }

    @Override
    public Collection<ItemDTO> findAll() {
        log.debug("Find all items");
        return itemRepository.findAll().stream().map(itemToDTOTransformer::transform).toList();
    }

    @Override
    public Collection<ItemDTO> findAllByIds(Collection<UUID> ids) {
        log.debug("Find all items by ids: [{}]", ids);
        return itemRepository.findAllByIdIn(ids)
                .stream()
                .map(itemToDTOTransformer::transform)
                .toList();
    }

    @Override
    public ItemDTO findById(UUID id) {
        log.debug("Find item by id: [{}]", id);
        return Optional.ofNullable(id)
                .map(itemRepository::findById)
                .orElseThrow(() -> new ItemBadRequestException("Item id required"))
                .map(itemToDTOTransformer::transform)
                .orElseThrow(() -> new ItemNotFoundException(id));
    }

    @Override
    public ItemDTO save(ItemDTO itemDTO) {
        log.debug("Saving item: {}", itemDTO);
        return Optional.ofNullable(itemDTO)
                .map(itemToEntityTransformer::transform)
                .map(itemRepository::save)
                .map(itemToDTOTransformer::transform)
                .orElseThrow(() -> new ItemBadRequestException("ItemDTO cannot be null"));
    }

}
