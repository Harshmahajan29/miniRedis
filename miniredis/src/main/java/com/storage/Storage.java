package com.storage;

import com.Expiration.*;
import com.redis.PubSubManager;
import com.redis.EvictionManager;
import com.redis.MetricsManager;

public class Storage {
    private final StringStore stringStore;
    private final ListStore listStore;
    private final SetStore setStore;
    private final HashStore hashStore;
    private final ExpirationManager expirationManager;
    private final PubSubManager pubSubManager;
    private final EvictionManager evictionManager;
    private final MetricsManager metricsManager;

    public Storage(int maxKeys) {
        this.stringStore = new StringStore();
        this.listStore = new ListStore();
        this.setStore = new SetStore();
        this.hashStore = new HashStore();
        this.expirationManager = new ExpirationManager();
        this.pubSubManager = new PubSubManager();
        this.evictionManager = new EvictionManager(maxKeys);
        this.metricsManager = new MetricsManager();
    }

    public Storage() {
        this(1000);
    }

    public MetricsManager getMetricsManager() {
        return metricsManager;
    }

    // Existing getters...
    public StringStore getStringStore() { return stringStore; }
    public ListStore getListStore() { return listStore; }
    public SetStore getSetStore() { return setStore; }
    public HashStore getHashStore() { return hashStore; }
    public ExpirationManager getExpirationManager() { return expirationManager; }
    public PubSubManager getPubSubManager() { return pubSubManager; }
    public EvictionManager getEvictionManager() { return evictionManager; }

    public void touchKey(String key) {
        if (evictionManager != null) {
            evictionManager.touch(key);
        }
    }

    public void evictIfNecessary() {
        if (evictionManager == null) return;

        while (evictionManager.isEvictionRequired(getTotalKeys())) {
            String lruKey = evictionManager.selectLruKey();
            if (lruKey == null) break;

            System.out.println("[Eviction] Storage full. Eviction target selected: " + lruKey);

            stringStore.delInternal(lruKey);
            listStore.delInternal(lruKey);
            setStore.delInternal(lruKey);
            hashStore.delInternal(lruKey);
            expirationManager.remove(lruKey);
            evictionManager.remove(lruKey);
        }
    }
    
    public int getTotalKeys() {
        return stringStore.size() + listStore.size() + setStore.size() + hashStore.size();
    }
}