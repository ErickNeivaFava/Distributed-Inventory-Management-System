package com.mercadolibre.inventory.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

//TODO mover pra common-lib?
public class InventoryRequest {

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    // Constructors
    public InventoryRequest() {}

    public InventoryRequest(Integer quantity) {
        this.quantity = quantity;
    }

    // Getters and setters
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
