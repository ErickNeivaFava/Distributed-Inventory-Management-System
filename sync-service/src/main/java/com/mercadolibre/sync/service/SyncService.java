package com.mercadolibre.sync.service;

import com.mercadolibre.common.model.ApiResponse;
import com.mercadolibre.common.model.InventoryEvent;
import com.mercadolibre.sync.model.InventoryResponse;
import com.mercadolibre.sync.model.SyncStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.SyncFailedException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class SyncService {

    private static final Logger logger = LoggerFactory.getLogger(SyncService.class);
    private final ConflictResolutionService conflictResolutionService;
    @Autowired
    private RestTemplate restTemplate;


    public SyncService(ConflictResolutionService conflictResolutionService) {
        this.conflictResolutionService = conflictResolutionService;
    }

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void scheduledSync() {
        logger.info("Starting scheduled synchronization");
        syncAllStores();
    }

    @Async
    public CompletableFuture<SyncStatus> syncAllStores() {
        SyncStatus status = new SyncStatus();
        status.setStartTime(LocalDateTime.now());
        status.setType("FULL_SYNC");

        try {
            // Buscar IDs das lojas do endpoint REST
            ResponseEntity<String[]> response = restTemplate.getForEntity(
                    "http://inventory-service:8081/api/stores/ids",
                    String[].class
            );

            List<String> storeIds = Arrays.asList(response.getBody());

            if (storeIds.isEmpty()) {
                logger.warn("No stores found to synchronize");
            }

            logger.info("Found {} stores to sync: {}", storeIds.size(), storeIds);

            // assume q a loja 000 tem todos os produtos cadastrados
            storeIds.forEach(storeId -> {
                try {
                    syncStore(storeId);
                    status.incrementSuccessCount();
                } catch (Exception e) {
                    logger.error("Failed to sync store {}: {}", storeId, e.getMessage());
                    status.incrementFailureCount();
                }
            });

            status.setEndTime(LocalDateTime.now());
            status.setStatus("COMPLETED");
            logger.info("Synchronization completed: {}", status);

        } catch (Exception e) {
            status.setEndTime(LocalDateTime.now());
            status.setStatus("FAILED");
            status.setErrorMessage(e.getMessage());
            logger.error("Synchronization failed: {}", e.getMessage());
        }

        return CompletableFuture.completedFuture(status);
    }

    @Async
    public CompletableFuture<SyncStatus> syncStore(String storeId) {
        SyncStatus status = new SyncStatus();
        status.setStartTime(LocalDateTime.now());
        status.setStoreId(storeId);
        status.setType("STORE_SYNC");

        try {
            logger.info("Syncing store: {}", storeId);

            // Simulate sync process with conflict resolution
            conflictResolutionService.resolveConflictsForStore(storeId);



            status.setEndTime(LocalDateTime.now());
            status.setStatus("COMPLETED");
            status.incrementSuccessCount();

            logger.info("Store {} synchronized successfully", storeId);

        } catch (Exception e) {
            status.setEndTime(LocalDateTime.now());
            status.setStatus("FAILED");
            status.setErrorMessage(e.getMessage());
            status.incrementFailureCount();

            logger.error("Failed to sync store {}: {}", storeId, e.getMessage());
        }

        return CompletableFuture.completedFuture(status);
    }

    public SyncStatus getSyncStatus(String syncId) {
        // In a real implementation, this would query a database
        SyncStatus status = new SyncStatus();
        status.setId(syncId);
        status.setStatus("COMPLETED");
        status.setStartTime(LocalDateTime.now().minusMinutes(5));
        status.setEndTime(LocalDateTime.now());
        return status;
    }

    public void syncInventoryEvent(InventoryEvent event) {
        logger.debug("Syncing inventory event for store: {}, product: {}, quantity: {}",
                event.getStoreId(), event.getProductId(), event.getQuantity());


        String centralStoreBaseUrl = "http://inventory-service:8081/api/inventory/store-000";
        try {
            String url;

            if (event.getQuantity() > 0) {
                // Quantity positiva - increment
                url = String.format("%s/%s/increment?quantity=%d&publishEvent=false",
                        centralStoreBaseUrl,
                        event.getProductId(),
                        event.getQuantity());
            } else {
                // Quantity negativa - decrement (usando valor absoluto)
                int absoluteQuantity = Math.abs(event.getQuantity());
                url = String.format("%s/%s/decrement?quantity=%d&publishEvent=false",
                        centralStoreBaseUrl,
                        event.getProductId(),
                        absoluteQuantity);
            }

            logger.debug("Calling central store API: {}", url);

            ResponseEntity<ApiResponse<InventoryResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    null,
                    new ParameterizedTypeReference<ApiResponse<InventoryResponse>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Successfully synced central inventory for product: {}, event: {}",
                        event.getProductId(), event.getEventId());
            } else {
                logger.warn("Failed to sync central inventory. Status: {}, Event: {}",
                        response.getStatusCode(), event.getEventId());
                // TODO kafka retry mechanism
                //throw new SyncFailedException("Failed to sync inventory event: " + event.getEventId());
            }

        } catch (Exception e) {
            logger.error("Error syncing inventory event: {}", event.getEventId(), e);
            //throw new SyncFailedException("Failed to sync inventory event: " + event.getEventId());
        }
    }

}
