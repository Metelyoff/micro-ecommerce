package com.ecommerce.inventory.transformers;

import com.ecommerce.inventory.dtos.ItemDTO;
import com.ecommerce.inventory.entities.ItemEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.ecommerce.common.persistence.entities.BaseEntity.MONEY_ROUNDING_MODE;
import static com.ecommerce.common.persistence.entities.BaseEntity.MONEY_SCALE;

@Slf4j
@Service
public class ItemToEntityTransformer implements Transformer<ItemDTO, ItemEntity> {

    @Override
    public ItemEntity transform(ItemDTO dto) {
        log.debug("Transform ItemDTO to ItemEntity from: {}", dto);
        if (dto == null) throw new IllegalArgumentException("ItemDTO cannot be null");
        ItemEntity item = new ItemEntity();
        item.setName(dto.getName());
        item.setImage(dto.getImage());
        item.setPrice(BigDecimal.valueOf(dto.getPrice()).setScale(MONEY_SCALE, MONEY_ROUNDING_MODE));
        item.setQuantity(dto.getQuantity());
        return item;
    }

}
