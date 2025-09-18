package com.mercadolibre.sync.model;

import java.time.LocalDateTime;

//TODO mover pra common-lib?
public class InventoryResponse {

    private String storeId;
    private String productId;
    private int quantity;
    private LocalDateTime lastUpdated;

    public InventoryResponse(String storeId,
        String productId,
        int quantity,
        LocalDateTime lastUpdated) {
        this.storeId = storeId;
        this.productId = productId;
        this.quantity = quantity;
        this.lastUpdated = lastUpdated;
    }

    // Getters
    public String getStoreId() { return storeId; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
}
