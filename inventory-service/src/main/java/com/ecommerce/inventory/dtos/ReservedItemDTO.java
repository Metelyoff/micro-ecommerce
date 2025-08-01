package com.ecommerce.inventory.dtos;

import com.ecommerce.common.persistence.dtos.AuditDTO;
import com.ecommerce.inventory.entities.ReservedItemStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ReservedItemDTO extends AuditDTO {

    @NotNull(message = "Reservation itemId cannot be null")
    private UUID itemId;

    private UUID orderId;

    @NotNull(message = "Reservation item price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Reservation item price must be greater than zero")
    private double price;

    @NotNull(message = "Reservation item quantity cannot be null")
    @Min(value = 1, message = "Reservation item quantity must be greater than zero")
    private int quantity;

    private ReservedItemStatus status;

    private Instant reservedAt;

}
