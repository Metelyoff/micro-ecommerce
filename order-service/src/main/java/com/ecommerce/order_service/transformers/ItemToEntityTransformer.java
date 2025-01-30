package com.ecommerce.order_service.transformers;

import com.ecommerce.order_service.dtos.OrderItemDTO;
import com.ecommerce.order_service.entities.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ItemToEntityTransformer implements Transformer<OrderItemDTO, OrderItem> {

    @Override
    public OrderItem transform(OrderItemDTO orderItemDTO) {
        if (orderItemDTO == null) throw new IllegalArgumentException("ItemDTO cannot be null");
        OrderItem orderItem = new OrderItem();
        orderItem.setId(orderItemDTO.id());
        orderItem.setProductId(orderItemDTO.productId());
        orderItem.setName(orderItemDTO.name());
        orderItem.setPrice(BigDecimal.valueOf(orderItemDTO.price()).setScale(2, RoundingMode.HALF_UP));
        orderItem.setQuantity(orderItemDTO.quantity());
        return orderItem;
    }

}
