package com.ecommerce.inventory.services.impl;

import com.ecommerce.inventory.dtos.ItemDTO;
import com.ecommerce.inventory.entities.ItemEntity;
import com.ecommerce.inventory.exceptions.ItemBadRequestException;
import com.ecommerce.inventory.exceptions.ItemNotFoundException;
import com.ecommerce.inventory.repositories.ItemRepository;
import com.ecommerce.inventory.transformers.ItemToDTOTransformer;
import com.ecommerce.inventory.transformers.ItemToEntityTransformer;
import com.ecommerce.inventory.transformers.Transformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static com.ecommerce.inventory.exceptions.ItemNotFoundException.NOT_FOUND_BY_ID_PATTERN;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceImplTest {

    private ItemRepository itemRepository;
    private Transformer<ItemEntity, ItemDTO> itemToDTOTransformer;
    private Transformer<ItemDTO, ItemEntity> itemToEntityTransformer;
    private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        itemRepository = mock(ItemRepository.class);
        itemToDTOTransformer = mock(ItemToDTOTransformer.class);
        itemToEntityTransformer = mock(ItemToEntityTransformer.class);
        itemService = new ItemServiceImpl(itemRepository, itemToDTOTransformer, itemToEntityTransformer);
    }

    @Test
    void findAll_shouldReturnAllItems() {
        var entity = new ItemEntity(
                "Test",
                5,
                "url",
                BigDecimal.TEN
        );
        var dto = new ItemDTO(
                entity.getName(),
                entity.getImage(),
                entity.getPrice().doubleValue(),
                entity.getQuantity()
        );

        when(itemRepository.findAll()).thenReturn(List.of(entity));
        when(itemToDTOTransformer.transform(entity)).thenReturn(dto);

        var result = itemService.findAll();

        assertThat(result).containsExactly(dto);

        verify(itemRepository, only()).findAll();
        verify(itemToDTOTransformer, only()).transform(entity);
    }

    @Test
    void findAllByIds_shouldReturnMatchingItems() {
        UUID id = randomUUID();
        var ids = Set.of(id);
        var entity = ItemEntity.builder()
                .id(id)
                .name("Test")
                .image("url")
                .quantity(5)
                .price(BigDecimal.TEN)
                .build();
        var dto = ItemDTO.builder()
                .id(id)
                .name(entity.getName())
                .image(entity.getImage())
                .price(entity.getPrice().doubleValue())
                .quantity(entity.getQuantity())
                .build();

        when(itemRepository.findAllByIdIn(ids)).thenReturn(List.of(entity));
        when(itemToDTOTransformer.transform(entity)).thenReturn(dto);

        var result = itemService.findAllByIds(ids);

        assertThat(result).containsExactly(dto);

        verify(itemRepository, only()).findAllByIdIn(ids);
        verify(itemToDTOTransformer, only()).transform(entity);
    }

    @Test
    void findById_shouldReturnItem_whenFound() {
        UUID id = randomUUID();
        var entity = ItemEntity.builder()
                .id(id)
                .name("Test")
                .image("url")
                .quantity(5)
                .price(BigDecimal.TEN)
                .build();
        var dto = ItemDTO.builder()
                .id(id)
                .name(entity.getName())
                .image(entity.getImage())
                .price(entity.getPrice().doubleValue())
                .quantity(entity.getQuantity())
                .build();

        when(itemRepository.findById(id)).thenReturn(Optional.of(entity));
        when(itemToDTOTransformer.transform(entity)).thenReturn(dto);

        var result = itemService.findById(id);

        assertThat(result).isEqualTo(dto);

        verify(itemRepository, only()).findById(id);
        verify(itemToDTOTransformer, only()).transform(entity);
    }

    @Test
    void findById_shouldThrowBadRequest_whenIdIsNull() {
        assertThatThrownBy(() -> itemService.findById(null))
                .isInstanceOf(ItemBadRequestException.class)
                .hasMessageContaining("Item id required");

        verify(itemRepository, never()).findById(any());
        verify(itemToDTOTransformer, never()).transform(any());
    }

    @Test
    void findById_shouldThrowNotFound_whenItemNotFound() {
        UUID id = randomUUID();

        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.findById(id))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining(String.format(NOT_FOUND_BY_ID_PATTERN, id));

        verify(itemRepository, only()).findById(id);
        verify(itemToDTOTransformer, never()).transform(any());
    }

    @Test
    void save_shouldReturnSavedItem() {
        var inputDto = new ItemDTO("Item", "url", BigDecimal.TEN.doubleValue(), 5);
        var entity = ItemEntity.builder()
                .id(UUID.randomUUID())
                .name(inputDto.getName())
                .image(inputDto.getImage())
                .quantity(inputDto.getQuantity())
                .price(BigDecimal.valueOf(inputDto.getPrice()))
                .build();
        var outputDto = ItemDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .image(entity.getImage())
                .price(entity.getPrice().doubleValue())
                .quantity(entity.getQuantity())
                .build();

        when(itemToEntityTransformer.transform(inputDto)).thenReturn(entity);
        when(itemRepository.save(entity)).thenReturn(entity);
        when(itemToDTOTransformer.transform(entity)).thenReturn(outputDto);

        var result = itemService.save(inputDto);

        assertThat(result).isEqualTo(outputDto);

        verify(itemRepository, only()).save(entity);
        verify(itemToDTOTransformer, only()).transform(entity);
        verify(itemToEntityTransformer, only()).transform(inputDto);
    }

    @Test
    void save_shouldThrowItemBadRequestException_whenInputIsNull() {
        assertThatThrownBy(() -> itemService.save(null))
                .isInstanceOf(ItemBadRequestException.class)
                .hasMessage("ItemDTO cannot be null");
    }

    @Test
    void init_shouldGenerateAndSave100RandomItems() {
        itemService.init();

        verify(itemRepository).saveAll(argThat(items -> {
            int count = 0;
            for (Object ignored : items) {
                count++;
            }
            return count == 100;
        }));
    }

}