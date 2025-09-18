package com.mercadolibre.common.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class InventoryEvent {

    private String eventId;
    private String storeId;
    private String productId;
    private int quantity;
    private String eventType;
    private LocalDateTime timestamp;
    private LocalDateTime processedAt;

    public InventoryEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }

    public InventoryEvent(String storeId, String productId, int quantity, String eventType) {
        this();
        this.storeId = storeId;
        this.productId = productId;
        this.quantity = quantity;
        this.eventType = eventType;
    }

    // Getters and setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    @Override
    public String toString() {
        return "InventoryEvent{" +
                "eventId='" + eventId + '\'' +
                ", storeId='" + storeId + '\'' +
                ", productId='" + productId + '\'' +
                ", quantity=" + quantity +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
