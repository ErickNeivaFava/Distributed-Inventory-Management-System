package com.mercadolibre.inventory.service;

import com.mercadolibre.inventory.model.Inventory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    private final RedisTemplate<String, Inventory> redisTemplate;

    public CacheService(RedisTemplate<String, Inventory> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void updateInventoryCache(String storeId, String productId, Inventory inventory) {
        String key = String.format("inventory:%s:%s", storeId, productId);
        redisTemplate.opsForValue().set(key, inventory);
    }

    public Inventory getInventoryFromCache(String storeId, String productId) {
        String key = String.format("inventory:%s:%s", storeId, productId);
        return redisTemplate.opsForValue().get(key);
    }

    public void evictInventoryCache(String storeId, String productId) {
        String key = String.format("inventory:%s:%s", storeId, productId);
        redisTemplate.delete(key);
    }
}
