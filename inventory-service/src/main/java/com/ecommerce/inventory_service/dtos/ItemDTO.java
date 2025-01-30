package com.ecommerce.inventory_service.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ItemDTO(
        UUID id,

        @NotNull(message = "Item name cannot be null")
        @NotBlank(message = "Item name cannot be blank")
        String name,

        String image,

        @NotNull(message = "Item price cannot be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "Item price must be greater than zero")
        double price,

        @NotNull(message = "Item quantity cannot be null")
        @Min(value = 1, message = "Item quantity must be greater than zero")
        int quantity
) {
        public ItemDTO(
                UUID id,
                String name,
                double price,
                int quantity
        ) {
                this(id, name, "https://picsum.photos/300/300", price, quantity);
        }
}
