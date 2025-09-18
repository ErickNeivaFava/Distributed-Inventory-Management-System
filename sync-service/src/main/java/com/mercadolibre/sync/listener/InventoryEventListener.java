package com.mercadolibre.sync.listener;


import com.mercadolibre.common.model.InventoryEvent;
import com.mercadolibre.sync.service.ConflictResolutionService;
import com.mercadolibre.sync.service.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventListener {

    private static final Logger logger = LoggerFactory.getLogger(InventoryEventListener.class);
    private final ConflictResolutionService conflictResolutionService;
    private final SyncService syncService;

    public InventoryEventListener(ConflictResolutionService conflictResolutionService,
                                  SyncService syncService) {
        this.conflictResolutionService = conflictResolutionService;
        this.syncService = syncService;
    }

    @KafkaListener(
            topics = "inventory-events",
            groupId = "sync-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleInventoryEvent(@Payload InventoryEvent event) {
        logger.info("Received inventory event: {}", event);

        try {
            processInventoryEvent(event);
            logger.debug("Successfully processed inventory event: {}", event.getEventId());
        } catch (Exception e) {
            logger.error("Failed to process inventory event: {}", event.getEventId(), e);
            // TODO (evolução): implementar retry/DLQ
        }
    }

    @KafkaListener(
            topics = "inventory-alerts",
            groupId = "sync-service-alerts",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleInventoryAlert(@Payload InventoryEvent event) {
        logger.warn("Received inventory alert: {}", event);

        // Handle low stock alerts or other critical events
        if ("LOW_STOCK_ALERT".equals(event.getEventType())) {
            handleLowStockAlert(event);
        }
    }

    private void processInventoryEvent(InventoryEvent event) {

        logger.debug("Processing inventory event for store: {}, product: {}",
                event.getStoreId(), event.getProductId());

        try {
            syncService.syncInventoryEvent(event);
        } catch (Exception e) {
            //TODO adicionar exception correta pro kafka retry -> RuntimeException?
            Thread.currentThread().interrupt();
            logger.error("Unexpected error processing inventory event: {}", event.getEventId(), e);
        }

        logger.info("Completed processing inventory event: {}", event.getEventId());
    }

    private void handleLowStockAlert(InventoryEvent event) {
        logger.warn("Handling low stock alert for store: {}, product: {}, quantity: {}",
                event.getStoreId(), event.getProductId(), event.getQuantity());

        // In a real implementation, this might:
        // 1. Send notifications
        // 2. Trigger replenishment processes
        // 3. Update dashboard alerts
    }
}