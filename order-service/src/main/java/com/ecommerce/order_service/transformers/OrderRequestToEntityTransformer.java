package com.ecommerce.order_service.transformers;

import com.ecommerce.order_service.dtos.OrderRequest;
import com.ecommerce.order_service.entities.OrderItem;
import com.ecommerce.order_service.entities.Order;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OrderRequestToEntityTransformer implements Transformer<OrderRequest, Order> {

    private final ItemToEntityTransformer itemToEntityTransformer;

    public OrderRequestToEntityTransformer(ItemToEntityTransformer itemToEntityTransformer) {
        this.itemToEntityTransformer = itemToEntityTransformer;
    }

    @Override
    public Order transform(OrderRequest orderDTO) {
        if (orderDTO == null) throw new IllegalArgumentException("OrderDTO cannot be null");
        Order order = new Order();

        Set<OrderItem> orderItems = Optional.ofNullable(orderDTO.items())
                .stream()
                .flatMap(Collection::stream)
                .map(itemToEntityTransformer::transform)
                .peek(orderItem -> orderItem.setOrder(order))
                .collect(Collectors.toSet());
        order.setItems(orderItems);

        order.setPaymentMethod(orderDTO.paymentMethod());

        return order;
    }

}
