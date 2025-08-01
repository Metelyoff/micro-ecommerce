package com.ecommerce.inventory.transformers;

import com.ecommerce.inventory.dtos.ItemDTO;
import com.ecommerce.inventory.entities.ItemEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.ecommerce.common.persistence.entities.BaseEntity.MONEY_ROUNDING_MODE;
import static com.ecommerce.common.persistence.entities.BaseEntity.MONEY_SCALE;

@Slf4j
@Service
public class ItemToDTOTransformer implements Transformer<ItemEntity, ItemDTO> {

    @Override
    public ItemDTO transform(ItemEntity entity) {
        log.debug("Transform ItemEntity to ItemDTO from: {}", entity);
        return Optional.ofNullable(entity)
                .map(item -> ItemDTO.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .image(item.getImage())
                        .price(item.getPrice().setScale(MONEY_SCALE, MONEY_ROUNDING_MODE).doubleValue())
                        .quantity(item.getQuantity())
                        .created(item.getCreated())
                        .createdBy(item.getCreatedBy())
                        .updated(item.getUpdated())
                        .updatedBy(item.getUpdatedBy())
                        .build()
                )
                .orElse(null);
    }

}
