package com.storage;

import com.Expiration.*;
public class Storage {
    private final StringStore stringStore;
    private final ListStore listStore;
    private final SetStore setStore;
    private final HashStore hashStore;
    private final ExpirationManager expirationManager;

    public Storage() {
        this.stringStore = new StringStore();
        this.listStore = new ListStore();
        this.setStore = new SetStore();
        this.hashStore = new HashStore();
        this.expirationManager = new ExpirationManager();
    }

    public StringStore getStringStore() {
        return stringStore;
    }

    public ListStore getListStore() {
        return listStore;
    }

    public SetStore getSetStore() {
        return setStore;
    }

    public HashStore getHashStore() {
        return hashStore;
    }

    public ExpirationManager getExpirationManager() {
        return expirationManager;
    }
}