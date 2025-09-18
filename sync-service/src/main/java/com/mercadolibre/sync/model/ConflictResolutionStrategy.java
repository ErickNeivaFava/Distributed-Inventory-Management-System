package com.mercadolibre.sync.model;

public enum ConflictResolutionStrategy {
    
    /**
     * Use the data with the most recent timestamp
     */
    LAST_WRITE_WINS("Last Write Wins"),
    
    /**
     * Use the data with the higher quantity
     */
    HIGHER_QUANTITY_WINS("Higher Quantity Wins"),
    
    /**
     * Use the data with the lower quantity (for safety)
     */
    LOWER_QUANTITY_WINS("Lower Quantity Wins"),
    
    /**
     * Require manual intervention for resolution
     */
    MANUAL_REVIEW("Manual Review Required"),
    
    /**
     * Use store data as source of truth
     */
    STORE_PRIORITY("Store Priority"),
    
    /**
     * Use central data as source of truth
     */
    CENTRAL_PRIORITY("Central Priority"),
    
    /**
     * Merge quantities (sum both)
     */
    MERGE_QUANTITIES("Merge Quantities");

    private final String description;

    ConflictResolutionStrategy(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
