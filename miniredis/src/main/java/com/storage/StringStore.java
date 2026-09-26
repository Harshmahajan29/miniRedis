package com.storage;

import com.Expiration.*;



import java.util.concurrent.ConcurrentHashMap;

public class StringStore {
    private final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

    public String set(String key, String value) {
        map.put(key, value);
        return "OK";
    }

    public String get(String key, ExpirationManager expirationManager, Storage storage) {
        if (expirationManager.isExpired(key, storage)) {
            return null;
        }
        return map.get(key);
    }

    public boolean del(String key, ExpirationManager expirationManager) {
        expirationManager.remove(key);
        return map.remove(key) != null;
    }

    // Used by ExpirationManager for internal evictions
    public void delInternal(String key) {
        map.remove(key);
    }

    public boolean exists(String key, ExpirationManager expirationManager, Storage storage) {
        if (expirationManager.isExpired(key, storage)) {
            return false;
        }
        return map.containsKey(key);
    }

    public Long incr(String key, ExpirationManager expirationManager, Storage storage) throws NumberFormatException {
        if (expirationManager.isExpired(key, storage)) {
            map.remove(key);
        }
        return Long.parseLong(map.compute(key, (k, v) -> {
            if (v == null) {
                return "1";
            }
            long current = Long.parseLong(v);
            return String.valueOf(current + 1);
        }));
    }
}