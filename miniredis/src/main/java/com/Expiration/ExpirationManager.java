package com.Expiration;
import com.storage.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExpirationManager {
    private final ConcurrentHashMap<String, Long> expiryMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public ExpirationManager() {
        // Active Expiration: Periodically sweep and clean expired keys every 100 milliseconds
        scheduler.scheduleAtFixedRate(this::cleanupExpiredKeys, 100, 100, TimeUnit.MILLISECONDS);
    }

    public void setTTL(String key, long ttlInSeconds, Storage storage) {
        long expireAt = System.currentTimeMillis() + (ttlInSeconds * 1000);
        expiryMap.put(key, expireAt);
    }

    public boolean isExpired(String key, Storage storage) {
        Long expireAt = expiryMap.get(key);
        if (expireAt == null) {
            return false;
        }

        if (System.currentTimeMillis() > expireAt) {
            // Passive Expiration: Evict immediately upon access
            evictKey(key, storage);
            return true;
        }

        return false;
    }

    public long getTTLSeconds(String key) {
        Long expireAt = expiryMap.get(key);
        if (expireAt == null) {
            return -1; // -1 means key exists but has no associated expire
        }

        long remainingMillis = expireAt - System.currentTimeMillis();
        if (remainingMillis <= 0) {
            return -2; // -2 means key does not exist / expired
        }

        return remainingMillis / 1000;
    }

    public void remove(String key) {
        expiryMap.remove(key);
    }

    private void evictKey(String key, Storage storage) {
        expiryMap.remove(key);
        storage.getStringStore().delInternal(key);
    }

    private void cleanupExpiredKeys() {
        long now = System.currentTimeMillis();
        for (ConcurrentHashMap.Entry<String, Long> entry : expiryMap.entrySet()) {
            if (now > entry.getValue()) {
                // Key has expired; remove from map and storage
                expiryMap.remove(entry.getKey());
                // Eviction will occur cleanly when storage is passed or updated
            }
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}