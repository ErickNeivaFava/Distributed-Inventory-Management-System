package com.mercadolibre.inventory.repository;

import com.mercadolibre.inventory.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, String> {

    @Query("SELECT s.id FROM Store s WHERE s.active = true")
    List<String> findActiveStoreIds();
}
