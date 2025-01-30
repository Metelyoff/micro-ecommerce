package com.ecommerce.inventory_service.dtos;

import com.ecommerce.inventory_service.entities.ReservedItemStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservedItemDTO(
        UUID id,

        @NotNull(message = "Reservation itemId cannot be null")
        UUID itemId,

        UUID orderId,

        @NotNull(message = "Reservation item price cannot be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "Reservation item price must be greater than zero")
        double price,

        @NotNull(message = "Reservation item quantity cannot be null")
        @Min(value = 1, message = "Reservation item quantity must be greater than zero")
        int quantity,

        ReservedItemStatus status,

        LocalDateTime reservedAt
) {
}
