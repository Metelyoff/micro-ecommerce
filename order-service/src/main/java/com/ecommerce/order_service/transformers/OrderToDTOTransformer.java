package com.ecommerce.order_service.transformers;

import com.ecommerce.order_service.dtos.OrderDTO;
import com.ecommerce.order_service.entities.Order;
import com.ecommerce.order_service.entities.OrderItem;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OrderToDTOTransformer implements Transformer<Order, OrderDTO> {

    private final ItemToDTOTransformer itemToDTOTransformer;

    public OrderToDTOTransformer(
            ItemToDTOTransformer itemToDTOTransformer
    ) {
        this.itemToDTOTransformer = itemToDTOTransformer;
    }

    @Override
    public OrderDTO transform(Order order) {
        if (order == null) return null;
        Set<OrderItem> itemsCopy = new HashSet<>(order.getItems());
        return new OrderDTO(
                order.getId(),
                itemsCopy
                        .stream()
                        .map(itemToDTOTransformer::transform)
                        .collect(Collectors.toSet()),
                order.getTotalItems(),
                order.getTotalPrice().setScale(2, RoundingMode.HALF_UP).doubleValue(),
                order.getStatus(),
                order.getStatusDescription(),
                order.getPaymentMethod(),
                order.getPaymentId(),
                order.getCreatedAt()
        );
    }

}
