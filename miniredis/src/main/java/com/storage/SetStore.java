package com.storage;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.*;

public class SetStore {
    private final ConcurrentHashMap<String, Set<String>> map = new ConcurrentHashMap<>();

    public int sadd(String key, List<String> members) {
        Set<String> set = map.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet());
        int addedCount = 0;
        for (String member : members) {
            if (set.add(member)) {
                addedCount++;
            }
        }
        return addedCount;
    }

    public int srem(String key, List<String> members) {
        Set<String> set = map.get(key);
        if (set == null || set.isEmpty()) {
            return 0;
        }

        int removedCount = 0;
        for (String member : members) {
            if (set.remove(member)) {
                removedCount++;
            }
        }

        if (set.isEmpty()) {
            map.remove(key);
        }
        return removedCount;
    }

    public boolean sismember(String key, String member) {
        Set<String> set = map.get(key);
        return set != null && set.contains(member);
    }

    public Set<String> smembers(String key) {
        Set<String> set = map.get(key);
        return set != null ? set : Collections.emptySet();
    }

    public void delInternal(String key) {
        map.remove(key);
    }
}