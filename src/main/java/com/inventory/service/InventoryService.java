package com.inventory.service;

import com.inventory.dto.inventory.InventoryRequest;
import com.inventory.dto.inventory.InventoryResponse;
import com.inventory.entity.InventoryItem;
import com.inventory.entity.Product;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.InventoryItemRepository;
import com.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<InventoryResponse> findAll(Pageable pageable) {
        return inventoryItemRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public InventoryResponse findById(Long id) {
        return toResponse(findItemOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> findByProductId(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", productId);
        }
        return inventoryItemRepository.findByProductId(productId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> findLowStock(int threshold) {
        return inventoryItemRepository.findLowStock(threshold).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public InventoryResponse create(InventoryRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.productId()));

        InventoryItem item = InventoryItem.builder()
                .product(product)
                .quantity(request.quantity())
                .location(request.location())
                .build();

        return toResponse(inventoryItemRepository.save(item));
    }

    @Transactional
    public InventoryResponse update(Long id, InventoryRequest request) {
        InventoryItem item = findItemOrThrow(id);

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.productId()));

        item.setProduct(product);
        item.setQuantity(request.quantity());
        item.setLocation(request.location());

        return toResponse(inventoryItemRepository.save(item));
    }

    @Transactional
    public void delete(Long id) {
        if (!inventoryItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("InventoryItem", id);
        }
        inventoryItemRepository.deleteById(id);
    }

    private InventoryItem findItemOrThrow(Long id) {
        return inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", id));
    }

    private InventoryResponse toResponse(InventoryItem item) {
        return new InventoryResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getSku(),
                item.getQuantity(),
                item.getLocation(),
                item.getLastUpdated()
        );
    }
}
