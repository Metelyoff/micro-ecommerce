package com.ecommerce.inventory_service.transformers;

import com.ecommerce.inventory_service.dtos.ItemDTO;
import com.ecommerce.inventory_service.entities.Item;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_UP;

@Service
public class ItemToEntityTransformer implements Transformer<ItemDTO, Item> {

    @Override
    public Item transform(ItemDTO itemDTO) {
        if (itemDTO == null) throw new IllegalArgumentException("ItemDTO cannot be null");
        Item item = new Item();
        item.setName(itemDTO.name());
        item.setPrice(BigDecimal.valueOf(itemDTO.price()).setScale(2, HALF_UP));
        item.setQuantity(itemDTO.quantity());
        return item;
    }

}
