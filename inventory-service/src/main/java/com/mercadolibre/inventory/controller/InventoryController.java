package com.mercadolibre.inventory.controller;

import com.mercadolibre.common.exception.ApiException;
import com.mercadolibre.common.model.ApiResponse;
import com.mercadolibre.inventory.model.Inventory;
import com.mercadolibre.inventory.model.InventoryRequest;
import com.mercadolibre.inventory.model.InventoryResponse;
import com.mercadolibre.inventory.service.InventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory")
@Validated
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{storeId}/{productId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventory(
            @PathVariable @NotBlank String storeId,
            @PathVariable @NotBlank String productId) {
        
        logger.info("Getting inventory for store: {}, product: {}", storeId, productId);
        
        try {
            Inventory inventory = inventoryService.getInventory(storeId, productId);
            InventoryResponse response = new InventoryResponse(inventory);
            return ResponseEntity.ok(ApiResponse.success("Inventory retrieved successfully", response));
        } catch (Exception e) {
            logger.error("Error getting inventory for store: {}, product: {}", storeId, productId, e);
            throw ApiException.inventoryNotFound(storeId, productId);
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventoryAcrossStores(
            @PathVariable @NotBlank String productId) {
        
        logger.info("Getting inventory across stores for product: {}", productId);
        
        try {
            List<Inventory> inventories = inventoryService.getInventoryAcrossStores(productId);
            List<InventoryResponse> responses = inventories.stream()
                    .map(InventoryResponse::new)
                    .toList();
            return ResponseEntity.ok(ApiResponse.success("Inventory across stores retrieved successfully", responses));
        } catch (Exception e) {
            logger.error("Error getting inventory across stores for product: {}", productId, e);
            throw ApiException.notFound("No inventory found for product: " + productId);
        }
    }

    @PostMapping("/{storeId}/{productId}/decrement")
    public ResponseEntity<ApiResponse<InventoryResponse>> decrementInventory(
            @PathVariable @NotBlank String storeId,
            @PathVariable @NotBlank String productId,
            @RequestParam @Positive int quantity,
            @RequestParam(required = false, defaultValue = "true") boolean publishEvent) {
        
        logger.info("Decrementing inventory for store: {}, product: {}, quantity: {}", storeId, productId, quantity);
        
        try {
            Inventory inventory = inventoryService.decrementInventory(storeId, productId, quantity, publishEvent);
            InventoryResponse response = new InventoryResponse(inventory);
            return ResponseEntity.ok(ApiResponse.success("Inventory decremented successfully", response));
        } catch (Exception e) {
            logger.error("Error decrementing inventory for store: {}, product: {}, quantity: {}", 
                    storeId, productId, quantity, e);
            throw e; // Let the service layer handle specific exceptions
        }
    }

    @PostMapping("/{storeId}/{productId}/increment")
    public ResponseEntity<ApiResponse<InventoryResponse>> incrementInventory(
            @PathVariable @NotBlank String storeId,
            @PathVariable @NotBlank String productId,
            @RequestParam @Positive int quantity,
            @RequestParam(required = false, defaultValue = "true") boolean publishEvent) {
        
        logger.info("Incrementing inventory for store: {}, product: {}, quantity: {}", storeId, productId, quantity);
        
        try {
            Inventory inventory = inventoryService.incrementInventory(storeId, productId, quantity, publishEvent);
            InventoryResponse response = new InventoryResponse(inventory);
            return ResponseEntity.ok(ApiResponse.success("Inventory incremented successfully", response));
        } catch (Exception e) {
            logger.error("Error incrementing inventory for store: {}, product: {}, quantity: {}", 
                    storeId, productId, quantity, e);
            throw e; // Let the service layer handle specific exceptions
        }
    }

    @PutMapping("/{storeId}/{productId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> setInventory(
            @PathVariable @NotBlank String storeId,
            @PathVariable @NotBlank String productId,
            @Valid @RequestBody InventoryRequest request,
            @RequestParam(required = false, defaultValue = "true") boolean publishEvent) {
        
        logger.info("Setting inventory for store: {}, product: {}, quantity: {}", 
                storeId, productId, request.getQuantity());
        
        try {
            Inventory inventory = inventoryService.setInventory(storeId, productId, request.getQuantity(), publishEvent);
            InventoryResponse response = new InventoryResponse(inventory);
            return ResponseEntity.ok(ApiResponse.success("Inventory set successfully", response));
        } catch (Exception e) {
            logger.error("Error setting inventory for store: {}, product: {}, quantity: {}", 
                    storeId, productId, request.getQuantity(), e);
            throw e; // Let the service layer handle specific exceptions
        }
    }

    @GetMapping("/{storeId}/low-stock")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getLowStockItems(
            @PathVariable @NotBlank String storeId,
            @RequestParam(defaultValue = "10") @Min(1) int threshold) {
        
        logger.info("Getting low stock items for store: {}, threshold: {}", storeId, threshold);
        
        try {
            List<Inventory> lowStockItems = inventoryService.getLowStockItems(storeId, threshold);
            List<InventoryResponse> responses = lowStockItems.stream()
                    .map(InventoryResponse::new)
                    .toList();
            return ResponseEntity.ok(ApiResponse.success("Low stock items retrieved successfully", responses));
        } catch (Exception e) {
            logger.error("Error getting low stock items for store: {}, threshold: {}", storeId, threshold, e);
            throw ApiException.storeNotFound(storeId);
        }
    }

    @GetMapping("/{storeId}/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInventorySummary(
            @PathVariable @NotBlank String storeId) {
        
        logger.info("Getting inventory summary for store: {}", storeId);
        
        try {
            Map<String, Object> summary = inventoryService.getInventorySummary(storeId);
            return ResponseEntity.ok(ApiResponse.success("Inventory summary retrieved successfully", summary));
        } catch (Exception e) {
            logger.error("Error getting inventory summary for store: {}", storeId, e);
            throw ApiException.storeNotFound(storeId);
        }
    }

    @GetMapping("/{storeId}/products")
    public ResponseEntity<?> getStoreProducts(@PathVariable String storeId) {

        logger.info("Getting inventory list for store: {}", storeId);
        try {
            List<Inventory> inventoryList;

            inventoryList = inventoryService.getAvailableProductsByStore(storeId);

            return ResponseEntity.ok(inventoryList);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar produtos da loja: " + e.getMessage());
        }
    }
}
