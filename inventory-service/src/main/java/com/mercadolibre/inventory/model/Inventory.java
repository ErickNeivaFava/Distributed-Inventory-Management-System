package com.mercadolibre.inventory.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

//TODO mover pra common-lib?
@Entity
@Table(name = "inventory", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"storeId", "productId"})
})
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String storeId;

    @Column(nullable = false)
    private String productId; // TODO (evolução): definir uma entidade para produto

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    // Constructors
    public Inventory() {
        this.lastUpdated = LocalDateTime.now();
    }

    public Inventory(String storeId, String productId, int quantity) {
        this.storeId = storeId;
        this.productId = productId;
        this.quantity = quantity;
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
