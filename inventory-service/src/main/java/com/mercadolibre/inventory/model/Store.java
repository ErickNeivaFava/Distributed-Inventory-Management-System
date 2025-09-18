package com.mercadolibre.inventory.model;

import com.mercadolibre.inventory.enums.StoreType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

//TODO mover pra common-lib?
@Entity
@Table(name = "stores")
public class Store {

    @Id
    private String id;

    private String name;
    private String location;
    private boolean active = true;
    private StoreType type; // Enum: STORE, CENTRAL_WAREHOUSE (unique)

    // Constructors
    public Store() {}

    public Store(String id, String name, String location, StoreType type) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.type = type;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public StoreType getType() { return type; }
    public void setType(StoreType type) { this.type = type; }
}
