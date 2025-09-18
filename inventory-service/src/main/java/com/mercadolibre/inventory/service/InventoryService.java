package com.mercadolibre.inventory.service;

import com.mercadolibre.common.exception.ApiException;
import com.mercadolibre.inventory.model.Inventory;
import com.mercadolibre.inventory.model.Store;
import com.mercadolibre.inventory.repository.InventoryRepository;
import com.mercadolibre.inventory.repository.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    private final InventoryRepository inventoryRepository;
    private final StoreRepository storeRepository;
    private final CacheService cacheService;
    private final EventPublisherService eventPublisherService;

    public InventoryService(InventoryRepository inventoryRepository,
                            StoreRepository storeRepository,
                            CacheService cacheService,
                            EventPublisherService eventPublisherService) {
        this.inventoryRepository = inventoryRepository;
        this.storeRepository = storeRepository;
        this.cacheService = cacheService;
        this.eventPublisherService = eventPublisherService;
    }

    @Cacheable(value = "inventory", key = "{#storeId, #productId}")
    public Inventory getInventory(String storeId, String productId) {
        logger.info("Fetching inventory from database for store: {}, product: {}", storeId, productId);
        return inventoryRepository.findByStoreIdAndProductId(storeId, productId)
                .orElseThrow(() -> ApiException.inventoryNotFound(storeId, productId));
    }

    public List<Inventory> getInventoryAcrossStores(String productId) {
        return inventoryRepository.findByProductId(productId);
    }

    @Transactional
    @CacheEvict(value = "inventory", key = "{#storeId, #productId}")
    public Inventory decrementInventory(String storeId, String productId, int quantity, boolean publishEvent) {
        logger.info("Decrementing inventory for store: {}, product: {}, quantity: {}", storeId, productId, quantity);
        
        Inventory inventory = inventoryRepository.findByStoreIdAndProductIdWithLock(storeId, productId)
                .orElseThrow(() -> ApiException.inventoryNotFound(storeId, productId));

        if (inventory.getQuantity() < quantity) {
            throw ApiException.insufficientInventory(storeId, productId, quantity, inventory.getQuantity());
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventory.setLastUpdated(LocalDateTime.now());
        Inventory updatedInventory = inventoryRepository.save(inventory);

        // Update cache manually
        cacheService.updateInventoryCache(storeId, productId, updatedInventory);

        // Publish event for synchronization
        //eventPublisherService.publishInventoryUpdate(storeId, productId, updatedInventory.getQuantity());
        if(publishEvent){
            eventPublisherService.publishInventoryUpdate(storeId, productId, -quantity);
        }

        logger.info("Successfully decremented inventory for store: {}, product: {}, new quantity: {}", 
                storeId, productId, updatedInventory.getQuantity());
        
        return updatedInventory;
    }

    @Transactional
    @CacheEvict(value = "inventory", key = "{#storeId, #productId}")
    public Inventory incrementInventory(String storeId, String productId, int quantity, boolean publishEvent) {
        Inventory inventory = inventoryRepository.findByStoreIdAndProductIdWithLock(storeId, productId)
                .orElseGet(() -> {
                    Inventory newInventory = new Inventory();
                    newInventory.setStoreId(storeId);
                    newInventory.setProductId(productId);
                    newInventory.setQuantity(0);
                    return newInventory;
                });

        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventory.setLastUpdated(LocalDateTime.now());
        Inventory updatedInventory = inventoryRepository.save(inventory);

        cacheService.updateInventoryCache(storeId, productId, updatedInventory);
        if(publishEvent) {
            eventPublisherService.publishInventoryUpdate(storeId, productId, quantity);
        }

        return updatedInventory;
    }

    @Transactional
    @CacheEvict(value = "inventory", key = "{#storeId, #productId}")
    public Inventory setInventory(String storeId, String productId, int quantity, boolean publishEvent) {

        Optional<Inventory> existingInventoryOpt = inventoryRepository.findByStoreIdAndProductIdWithLock(storeId, productId);

        int oldQuantity = 0;
        Inventory inventory;

        if (existingInventoryOpt.isPresent()) {
            inventory = existingInventoryOpt.get();
            oldQuantity = inventory.getQuantity();
        } else {
            inventory = new Inventory();
            inventory.setStoreId(storeId);
            inventory.setProductId(productId);
        }

        inventory.setQuantity(quantity);
        inventory.setLastUpdated(LocalDateTime.now());
        Inventory updatedInventory = inventoryRepository.save(inventory);

        cacheService.updateInventoryCache(storeId, productId, updatedInventory);

        int quantityDifference = quantity - oldQuantity;
        if(publishEvent){
            eventPublisherService.publishInventoryUpdate(storeId, productId, quantityDifference);
        }

        return updatedInventory;
    }

    public List<Inventory> getAvailableProductsByStore(String storeId) {
        return inventoryRepository.findByStoreId(storeId);
    }

    public List<Inventory> getLowStockItems(String storeId, int threshold) {
        return inventoryRepository.findByStoreIdAndQuantityLessThanEqual(storeId, threshold);
    }

    public Map<String, Object> getInventorySummary(String storeId) {
        logger.info("Getting inventory summary for store: {}", storeId);

        // Verify store exists
        if (!storeRepository.existsById(storeId)) {
            throw ApiException.storeNotFound(storeId);
        }

        List<Inventory> inventories = inventoryRepository.findByStoreId(storeId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("storeId", storeId);
        summary.put("totalProducts", inventories.size());
        summary.put("totalQuantity", inventories.stream().mapToInt(Inventory::getQuantity).sum());
        summary.put("lowStockCount", inventories.stream().mapToInt(i -> i.getQuantity() <= 10 ? 1 : 0).sum());
        summary.put("outOfStockCount", inventories.stream().mapToInt(i -> i.getQuantity() == 0 ? 1 : 0).sum());
        summary.put("lastUpdated", LocalDateTime.now());

        logger.info("Generated inventory summary for store: {} - {} products, {} total quantity",
                storeId, inventories.size(), summary.get("totalQuantity"));

        return summary;
    }

    //TODO analisar -> separar store para outro service?

    @Transactional(readOnly = true)
    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<String> getAllStoresIds() {
        //List<String> ids = ;
        return storeRepository.findActiveStoreIds();
    }

    public Store getStore(String storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));
    }

    public Store createStore(Store store) {
        return storeRepository.save(store);
    }

    public void deleteStore(String storeId) {
        storeRepository.deleteById(storeId);
    }


}