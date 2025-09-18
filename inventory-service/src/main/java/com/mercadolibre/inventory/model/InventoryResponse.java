package com.mercadolibre.inventory.model;

import java.time.LocalDateTime;

//TODO mover pra common-lib?
public class InventoryResponse {

    private String storeId;
    private String productId;
    private int quantity;
    private LocalDateTime lastUpdated;

    public InventoryResponse(Inventory inventory) {
        this.storeId = inventory.getStoreId();
        this.productId = inventory.getProductId();
        this.quantity = inventory.getQuantity();
        this.lastUpdated = inventory.getLastUpdated();
    }

    // Getters
    public String getStoreId() { return storeId; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
}
