package com.mercadolibre.sync.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConflictResolutionService {

    private static final Logger logger = LoggerFactory.getLogger(ConflictResolutionService.class);
    @Autowired
    private RestTemplate restTemplate;

    public void resolveConflictsForStore(String storeId) {
        logger.info("Resolving conflicts for store: {}", storeId);

        try {
            // 1. Buscar inventário da loja
            ResponseEntity<List<Map<String, Object>>> storeResponse = restTemplate.exchange(
                    "http://localhost:8081/api/inventory/" + storeId + "/products",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            List<Map<String, Object>> storeInventory = storeResponse.getBody();

            // 2. Buscar inventário da central
            ResponseEntity<List<Map<String, Object>>> centralResponse = restTemplate.exchange(
                    "http://localhost:8081/api/inventory/STORE-000/products",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            List<Map<String, Object>> centralInventory = centralResponse.getBody();

            // 3. Criar mapa da central para acesso rápido
            Map<String, Map<String, Object>> centralMap = new HashMap<>();
            for (Map<String, Object> item : centralInventory) {
                String productId = (String) item.get("productId");
                centralMap.put(productId, item);
            }

            // 4. Subtrair da central os produtos que existem na loja
            for (Map<String, Object> storeItem : storeInventory) {
                String productId = (String) storeItem.get("productId");
                Integer quantity = (Integer) storeItem.get("quantity");

                if (productId != null && quantity != null && quantity > 0 && centralMap.containsKey(productId)) {
                    String decrementUrl = String.format(
                            "http://localhost:8081/api/inventory/store-000/%s/decrement?quantity=%d",
                            productId, quantity
                    );

                    restTemplate.postForEntity(decrementUrl, null, Void.class);
                    logger.info("Decremented {} units of product {} from central inventory", quantity, productId);
                }
            }

        } catch (Exception e) {
            logger.error("Error resolving conflicts for store {}: {}", storeId, e.getMessage(), e);
        }
    }


    private void simulateConflictResolution(String storeId) {
        // Simulate different conflict scenarios and resolution
        logger.debug("Simulating conflict resolution for store: {}", storeId);

        // Example conflict resolution strategies:
        // 1. Last write wins (based on timestamp)
        // 2. Manual intervention required for certain thresholds
        // 3. Business rules based on product type

        try {
            // Simulate processing time
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Conflict resolution interrupted for store: {}", storeId);
        }

        logger.info("Conflict resolution completed for store: {}", storeId);
    }

    public String resolveInventoryConflict(String storeId, String productId,
                                           int storeQuantity, LocalDateTime storeTimestamp,
                                           int centralQuantity, LocalDateTime centralTimestamp) {

        logger.debug("Resolving inventory conflict for store: {}, product: {}", storeId, productId);

        // Last write wins strategy
        if (storeTimestamp.isAfter(centralTimestamp)) {
            logger.info("Using store data (newer timestamp) for store: {}, product: {}", storeId, productId);
            return "STORE_DATA_USED";
        } else if (centralTimestamp.isAfter(storeTimestamp)) {
            logger.info("Using central data (newer timestamp) for store: {}, product: {}", storeId, productId);
            return "CENTRAL_DATA_USED";
        } else {
            // Same timestamp, use higher quantity (or other business rule)
            if (storeQuantity > centralQuantity) {
                logger.info("Using store data (higher quantity) for store: {}, product: {}", storeId, productId);
                return "STORE_DATA_USED";
            } else {
                logger.info("Using central data for store: {}, product: {}", storeId, productId);
                return "CENTRAL_DATA_USED";
            }
        }
    }
}
