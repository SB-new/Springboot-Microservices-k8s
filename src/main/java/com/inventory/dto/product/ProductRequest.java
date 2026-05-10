package com.inventory.dto.product;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Product name is required")
        @Size(max = 100)
        String name,

        @NotBlank(message = "SKU is required")
        @Size(max = 50)
        String sku,

        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.00", inclusive = false, message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2)
        BigDecimal price
) {}
