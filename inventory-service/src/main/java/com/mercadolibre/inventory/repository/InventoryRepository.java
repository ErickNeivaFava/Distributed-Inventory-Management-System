package com.mercadolibre.inventory.repository;

import com.mercadolibre.inventory.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByStoreIdAndProductId(String storeId, String productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.storeId = :storeId AND i.productId = :productId")
    Optional<Inventory> findByStoreIdAndProductIdWithLock(@Param("storeId") String storeId,
                                                          @Param("productId") String productId);

    List<Inventory> findByProductId(String productId);

    List<Inventory> findByStoreId(String storeId);

    List<Inventory> findByStoreIdAndQuantityLessThanEqual(String storeId, int threshold);
}