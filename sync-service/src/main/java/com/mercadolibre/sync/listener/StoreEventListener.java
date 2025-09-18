package com.mercadolibre.sync.listener;

import com.mercadolibre.sync.service.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StoreEventListener {

    private static final Logger logger = LoggerFactory.getLogger(StoreEventListener.class);
    private final SyncService syncService;

    public StoreEventListener(SyncService syncService) {
        this.syncService = syncService;
    }

    @KafkaListener(
            topics = "store-events",
            groupId = "sync-service-stores",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleStoreEvent(Map<String, Object> event) {
        logger.info("Received store event: {}", event);

        String eventType = (String) event.get("eventType");
        String storeId = (String) event.get("storeId");

        switch (eventType) {
            case "STORE_CREATED":
                handleStoreCreated(storeId, event);
                break;
            case "STORE_UPDATED":
                handleStoreUpdated(storeId, event);
                break;
            case "STORE_DELETED":
                handleStoreDeleted(storeId, event);
                break;
            default:
                logger.warn("Unknown store event type: {}", eventType);
        }
    }

    private void handleStoreCreated(String storeId, Map<String, Object> event) {
        logger.info("Handling store created event for store: {}", storeId);
        // Initialize sync for new store
        // a loja inicia com produtos zerados
        //syncService.syncStore(storeId);
    }

    private void handleStoreUpdated(String storeId, Map<String, Object> event) {
        logger.info("Handling store updated event for store: {}", storeId);
        // Trigger sync for updated store
        // o update iria atualizar apenas os dados da loja e não produtos
        //syncService.syncStore(storeId);
    }

    private void handleStoreDeleted(String storeId, Map<String, Object> event) {
        logger.info("Handling store deleted event for store: {}", storeId);
        // Clean up sync data for deleted store
        // This would typically involve removing store from sync schedules
        // and archiving sync history
        syncService.syncStore(storeId);
        //apagar todos os inventarios com o storeid
    }
}
