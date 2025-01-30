package com.ecommerce.inventory_service.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderEvent(
        String id,
        double totalPrice,
        int totalItems,
        List<OrderItemDTO> items,
        String status,
        String statusDescription,
        String createdAt
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderItemDTO(
            String id,
            String productId,
            String name,
            double price,
            int quantity
    ) {
    }
}