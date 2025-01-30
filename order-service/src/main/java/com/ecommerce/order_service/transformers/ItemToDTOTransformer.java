package com.ecommerce.order_service.transformers;

import com.ecommerce.order_service.dtos.OrderItemDTO;
import com.ecommerce.order_service.entities.OrderItem;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.Optional;

@Component
public class ItemToDTOTransformer implements Transformer<OrderItem, OrderItemDTO> {

    @Override
    public OrderItemDTO transform(OrderItem orderItem) {
        return Optional.ofNullable(orderItem).map(i -> new OrderItemDTO(
                        i.getId(),
                        i.getProductId(),
                        i.getName(),
                        i.getPrice().setScale(2, RoundingMode.HALF_UP).doubleValue(),
                        i.getQuantity()
                )
        ).orElse(null);
    }

}
