package com.inventory.repository;

import com.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    List<InventoryItem> findByProductId(Long productId);

    List<InventoryItem> findByLocationContainingIgnoreCase(String location);

    @Query("SELECT i FROM InventoryItem i WHERE i.quantity <= :threshold")
    List<InventoryItem> findLowStock(int threshold);

    Optional<InventoryItem> findByProductIdAndLocation(Long productId, String location);
}
