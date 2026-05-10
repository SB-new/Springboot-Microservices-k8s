package com.inventory.dto.inventory;

import jakarta.validation.constraints.*;

public record InventoryRequest(
        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity cannot be negative")
        Integer quantity,

        @Size(max = 100)
        String location
) {}
