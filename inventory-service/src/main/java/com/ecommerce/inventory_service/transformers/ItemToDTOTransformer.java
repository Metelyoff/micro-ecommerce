package com.ecommerce.inventory_service.transformers;

import com.ecommerce.inventory_service.dtos.ItemDTO;
import com.ecommerce.inventory_service.entities.Item;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ItemToDTOTransformer implements Transformer<Item, ItemDTO> {

    @Override
    public ItemDTO transform(Item item) {
        return Optional.ofNullable(item).map(i -> new ItemDTO(
                        i.getId(),
                        i.getName(),
                        i.getImage(),
                        i.getPrice().setScale(2, java.math.RoundingMode.HALF_UP).doubleValue(),
                        i.getQuantity()
                )
        ).orElse(null);
    }

}
