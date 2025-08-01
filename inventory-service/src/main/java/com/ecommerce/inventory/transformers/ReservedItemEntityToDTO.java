package com.ecommerce.inventory.transformers;

import com.ecommerce.inventory.dtos.ReservedItemDTO;
import com.ecommerce.inventory.entities.ReservedItemEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.ecommerce.common.persistence.entities.BaseEntity.MONEY_ROUNDING_MODE;
import static com.ecommerce.common.persistence.entities.BaseEntity.MONEY_SCALE;

@Slf4j
@Service
public class ReservedItemEntityToDTO implements Transformer<ReservedItemEntity, ReservedItemDTO> {

    @Override
    public ReservedItemDTO transform(ReservedItemEntity entity) {
        log.debug("Transform ReservedItemEntity to ReservedItemDTO from : {}", entity);
        return Optional.ofNullable(entity)
                .map(item -> ReservedItemDTO.builder()
                        .id(item.getId())
                        .itemId(item.getItem().getId())
                        .orderId(item.getOrderId())
                        .price(item.getItem().getPrice().setScale(MONEY_SCALE, MONEY_ROUNDING_MODE).doubleValue())
                        .quantity(item.getQuantity())
                        .status(item.getStatus())
                        .reservedAt(item.getReservedAt())
                        .created(item.getCreated())
                        .createdBy(item.getCreatedBy())
                        .updated(item.getUpdated())
                        .updatedBy(item.getUpdatedBy())
                        .build()
                )
                .orElse(null);
    }

}
