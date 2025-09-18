package com.mercadolibre.sync.model;

import java.time.LocalDateTime;

public class InventoryConflict {
    
    private String id;
    private String storeId;
    private String productId;
    private int storeQuantity;
    private int centralQuantity;
    private LocalDateTime storeTimestamp;
    private LocalDateTime centralTimestamp;
    private LocalDateTime conflictDetectedAt;
    private boolean resolved;
    private boolean requiresManualReview;
    private ConflictResolutionStrategy resolutionStrategy;
    private LocalDateTime resolvedAt;
    private String resolutionNotes;

    public InventoryConflict() {
        this.id = java.util.UUID.randomUUID().toString();
        this.conflictDetectedAt = LocalDateTime.now();
        this.resolved = false;
        this.requiresManualReview = false;
    }

    public InventoryConflict(String storeId, String productId, int storeQuantity, int centralQuantity,
                           LocalDateTime storeTimestamp, LocalDateTime centralTimestamp) {
        this();
        this.storeId = storeId;
        this.productId = productId;
        this.storeQuantity = storeQuantity;
        this.centralQuantity = centralQuantity;
        this.storeTimestamp = storeTimestamp;
        this.centralTimestamp = centralTimestamp;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getStoreQuantity() {
        return storeQuantity;
    }

    public void setStoreQuantity(int storeQuantity) {
        this.storeQuantity = storeQuantity;
    }

    public int getCentralQuantity() {
        return centralQuantity;
    }

    public void setCentralQuantity(int centralQuantity) {
        this.centralQuantity = centralQuantity;
    }

    public LocalDateTime getStoreTimestamp() {
        return storeTimestamp;
    }

    public void setStoreTimestamp(LocalDateTime storeTimestamp) {
        this.storeTimestamp = storeTimestamp;
    }

    public LocalDateTime getCentralTimestamp() {
        return centralTimestamp;
    }

    public void setCentralTimestamp(LocalDateTime centralTimestamp) {
        this.centralTimestamp = centralTimestamp;
    }

    public LocalDateTime getConflictDetectedAt() {
        return conflictDetectedAt;
    }

    public void setConflictDetectedAt(LocalDateTime conflictDetectedAt) {
        this.conflictDetectedAt = conflictDetectedAt;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public boolean isRequiresManualReview() {
        return requiresManualReview;
    }

    public void setRequiresManualReview(boolean requiresManualReview) {
        this.requiresManualReview = requiresManualReview;
    }

    public ConflictResolutionStrategy getResolutionStrategy() {
        return resolutionStrategy;
    }

    public void setResolutionStrategy(ConflictResolutionStrategy resolutionStrategy) {
        this.resolutionStrategy = resolutionStrategy;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    @Override
    public String toString() {
        return "InventoryConflict{" +
                "id='" + id + '\'' +
                ", storeId='" + storeId + '\'' +
                ", productId='" + productId + '\'' +
                ", storeQuantity=" + storeQuantity +
                ", centralQuantity=" + centralQuantity +
                ", storeTimestamp=" + storeTimestamp +
                ", centralTimestamp=" + centralTimestamp +
                ", conflictDetectedAt=" + conflictDetectedAt +
                ", resolved=" + resolved +
                ", requiresManualReview=" + requiresManualReview +
                ", resolutionStrategy=" + resolutionStrategy +
                ", resolvedAt=" + resolvedAt +
                '}';
    }
}
