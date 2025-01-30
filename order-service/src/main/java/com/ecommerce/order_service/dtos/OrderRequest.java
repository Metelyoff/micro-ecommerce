package com.ecommerce.order_service.dtos;

import com.ecommerce.order_service.entities.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record OrderRequest(
        @NotEmpty(message = "Items cannot be empty")
        Set<@Valid OrderItemDTO> items,
        PaymentMethod paymentMethod
) {
}
