package com.mercadolibre.sync.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class SyncStatus {

    private String id;
    private String storeId;
    private String type;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int itemsProcessed;
    private int successCount;
    private int failureCount;
    private String errorMessage;

    public SyncStatus() {
        this.id = UUID.randomUUID().toString();
        this.status = "PENDING";
        this.startTime = LocalDateTime.now();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public int getItemsProcessed() { return itemsProcessed; }
    public void setItemsProcessed(int itemsProcessed) { this.itemsProcessed = itemsProcessed; }
    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    public int getFailureCount() { return failureCount; }
    public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public void incrementSuccessCount() {
        this.successCount++;
        this.itemsProcessed++;
    }

    public void incrementFailureCount() {
        this.failureCount++;
        this.itemsProcessed++;
    }

    public long getDuration() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMillis();
        }
        return 0;
    }

    @Override
    public String toString() {
        return "SyncStatus{" +
                "id='" + id + '\'' +
                ", storeId='" + storeId + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", duration=" + getDuration() + "ms" +
                ", successCount=" + successCount +
                ", failureCount=" + failureCount +
                '}';
    }
}
