package com.ecommerce.inventory_service.transformers;

import com.ecommerce.inventory_service.dtos.ReservedItemDTO;
import com.ecommerce.inventory_service.entities.ReservedItem;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;

@Service
public class ReservedItemToDTO implements Transformer<ReservedItem, ReservedItemDTO> {

    @Override
    public ReservedItemDTO transform(ReservedItem reservedItem) {
        return new ReservedItemDTO(
                reservedItem.getId(),
                reservedItem.getItem().getId(),
                reservedItem.getOrderId(),
                reservedItem.getItem().getPrice().setScale(2, RoundingMode.HALF_UP).doubleValue(),
                reservedItem.getQuantity(),
                reservedItem.getStatus(),
                reservedItem.getReservedAt()
        );
    }

}
