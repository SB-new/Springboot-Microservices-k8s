package com.inventory.service;

import com.inventory.dto.inventory.InventoryRequest;
import com.inventory.dto.inventory.InventoryResponse;
import com.inventory.entity.InventoryItem;
import com.inventory.entity.Product;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.InventoryItemRepository;
import com.inventory.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Product product;
    private InventoryItem item;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L).name("Widget").sku("WGT-001")
                .price(BigDecimal.valueOf(9.99)).build();

        item = InventoryItem.builder()
                .id(1L).product(product)
                .quantity(50).location("Aisle-3")
                .lastUpdated(LocalDateTime.now()).build();
    }

    @Test
    void findById_whenExists_returnsResponse() {
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(item));

        InventoryResponse response = inventoryService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.productName()).isEqualTo("Widget");
        assertThat(response.quantity()).isEqualTo(50);
        assertThat(response.location()).isEqualTo("Aisle-3");
    }

    @Test
    void findById_whenMissing_throwsNotFoundException() {
        when(inventoryItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_withValidRequest_savesAndReturnsResponse() {
        InventoryRequest request = new InventoryRequest(1L, 100, "Shelf-B");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryItemRepository.save(any())).thenReturn(
                InventoryItem.builder().id(2L).product(product)
                        .quantity(100).location("Shelf-B")
                        .lastUpdated(LocalDateTime.now()).build());

        InventoryResponse response = inventoryService.create(request);

        assertThat(response.quantity()).isEqualTo(100);
        assertThat(response.location()).isEqualTo("Shelf-B");
        verify(inventoryItemRepository).save(any(InventoryItem.class));
    }

    @Test
    void create_withMissingProduct_throwsNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.create(new InventoryRequest(99L, 10, "Shelf-A")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists_deletesItem() {
        when(inventoryItemRepository.existsById(1L)).thenReturn(true);

        inventoryService.delete(1L);

        verify(inventoryItemRepository).deleteById(1L);
    }

    @Test
    void delete_whenMissing_throwsNotFoundException() {
        when(inventoryItemRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> inventoryService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
