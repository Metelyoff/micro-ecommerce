package com.ecommerce.inventory.dtos;

import com.ecommerce.common.persistence.dtos.AuditDTO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ItemDTO extends AuditDTO {

    @NotBlank(message = "Item name cannot be blank or empty")
    private String name;

    private String image;

    @NotNull(message = "Item price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Item price must be greater than zero")
    private double price;

    @NotNull(message = "Item quantity cannot be null")
    @Min(value = 1, message = "Item quantity must be greater than zero")
    private int quantity;

}
