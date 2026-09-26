package com.storage;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HashStore {
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, String>> map = new ConcurrentHashMap<>();

    public int hset(String key, String field, String value) {
        ConcurrentHashMap<String, String> innerMap = map.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
        String previous = innerMap.put(field, value);
        return previous == null ? 1 : 0; // 1 if new field added, 0 if field updated
    }

    public String hget(String key, String field) {
        ConcurrentHashMap<String, String> innerMap = map.get(key);
        return innerMap != null ? innerMap.get(field) : null;
    }

    public int hdel(String key, String field) {
        ConcurrentHashMap<String, String> innerMap = map.get(key);
        if (innerMap == null) {
            return 0;
        }
        String removed = innerMap.remove(field);
        if (innerMap.isEmpty()) {
            map.remove(key);
        }
        return removed != null ? 1 : 0;
    }

    public Map<String, String> hgetall(String key) {
        ConcurrentHashMap<String, String> innerMap = map.get(key);
        return innerMap != null ? innerMap : Collections.emptyMap();
    }

    public void delInternal(String key) {
        map.remove(key);
    }

    public int size() {
        return map.size();
    }
}