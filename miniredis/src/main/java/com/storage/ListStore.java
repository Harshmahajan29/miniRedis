package com.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ListStore {
    private final ConcurrentHashMap<String, List<String>> map = new ConcurrentHashMap<>();

    public synchronized int lpush(String key, List<String> values) {
        List<String> list = map.computeIfAbsent(key, k -> new ArrayList<>());
        for (String val : values) {
            list.add(0, val); // Insert at front
        }
        return list.size();
    }

    public synchronized int rpush(String key, List<String> values) {
        List<String> list = map.computeIfAbsent(key, k -> new ArrayList<>());
        list.addAll(values); // Append to back
        return list.size();
    }

    public synchronized String lpop(String key) {
        List<String> list = map.get(key);
        if (list == null || list.isEmpty()) {
            return null;
        }
        String val = list.remove(0);
        if (list.isEmpty()) {
            map.remove(key);
        }
        return val;
    }

    public synchronized String rpop(String key) {
        List<String> list = map.get(key);
        if (list == null || list.isEmpty()) {
            return null;
        }
        String val = list.remove(list.size() - 1);
        if (list.isEmpty()) {
            map.remove(key);
        }
        return val;
    }

    public synchronized List<String> lrange(String key, int start, int stop) {
        List<String> list = map.get(key);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        int size = list.size();
        // Handle negative indices like standard Redis
        if (start < 0) start = Math.max(0, size + start);
        if (stop < 0) stop = size + stop;

        if (start > stop || start >= size) {
            return Collections.emptyList();
        }

        stop = Math.min(stop, size - 1);
        return new ArrayList<>(list.subList(start, stop + 1));
    }

    public void delInternal(String key) {
        map.remove(key);
    }

    public int size() {
        return map.size();
    }
}