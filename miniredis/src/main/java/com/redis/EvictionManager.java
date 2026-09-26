package com.redis;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EvictionManager {
    private final int maxKeys;
    private final ConcurrentHashMap<String, Long> accessTracker = new ConcurrentHashMap<>();

    public EvictionManager(int maxKeys) {
        this.maxKeys = maxKeys;
    }

    public void touch(String key) {
        accessTracker.put(key, System.currentTimeMillis());
    }

    public void remove(String key) {
        accessTracker.remove(key);
    }

    public boolean isEvictionRequired(int currentKeyCount) {
        return maxKeys > 0 && currentKeyCount >= maxKeys;
    }

    public String selectLruKey() {
        if (accessTracker.isEmpty()) {
            return null;
        }

        // Find key with the smallest timestamp (least recently used)
        return accessTracker.entrySet()
                .stream()
                .min(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public int getMaxKeys() {
        return maxKeys;
    }
}