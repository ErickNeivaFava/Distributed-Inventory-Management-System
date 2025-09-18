package com.mercadolibre.inventory.controller;


import com.mercadolibre.inventory.model.Store;
import com.mercadolibre.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final InventoryService inventoryService;

    public StoreController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<List<Store>> getAllStores() {
        return ResponseEntity.ok(inventoryService.getAllStores());
    }

    @GetMapping("/ids")
    public ResponseEntity<List<String>> getAllStoresIds() {
        return ResponseEntity.ok(inventoryService.getAllStoresIds());
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<Store> getStore(@PathVariable String storeId) {
        return ResponseEntity.ok(inventoryService.getStore(storeId));
    }

    @PostMapping
    public ResponseEntity<Store> createStore(@RequestBody Store store) {
        return ResponseEntity.ok(inventoryService.createStore(store));
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> deleteStore(@PathVariable String storeId) {
        inventoryService.deleteStore(storeId);
        return ResponseEntity.noContent().build();
    }
}
