package com.ecommerce.order_service.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderItemDTO(
        UUID id,

        @NotNull(message = "Item productId cannot be null")
        UUID productId,

        @NotNull(message = "Item name cannot be null")
        @NotEmpty(message = "Item name cannot be empty")
        String name,

        @NotNull(message = "Total amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
        double price,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be greater than zero")
        int quantity
) {
}
