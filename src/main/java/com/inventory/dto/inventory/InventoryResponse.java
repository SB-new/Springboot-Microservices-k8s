package com.inventory.dto.inventory;

import java.time.LocalDateTime;

public record InventoryResponse(
        Long id,
        Long productId,
        String productName,
        String productSku,
        Integer quantity,
        String location,
        LocalDateTime lastUpdated
) {}
