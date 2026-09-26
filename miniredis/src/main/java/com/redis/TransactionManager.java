package com.redis;

import java.util.ArrayList;
import java.util.List;

import com.command.*;


public class TransactionManager {
    private boolean inTransaction = false;
    private final List<Command> commandQueue = new ArrayList<>();

    public boolean isInTransaction() {
        return inTransaction;
    }

    public String multi() {
        if (inTransaction) {
            return "ERR MULTI calls cannot be nested";
        }
        inTransaction = true;
        commandQueue.clear();
        return "OK";
    }

    public String enqueue(Command command) {
        if (!inTransaction) {
            return "ERR EXEC without MULTI";
        }
        commandQueue.add(command);
        return "QUEUED";
    }

    public List<Command> exec() throws IllegalStateException {
        if (!inTransaction) {
            throw new IllegalStateException("ERR EXEC without MULTI");
        }
        List<Command> queued = new ArrayList<>(commandQueue);
        commandQueue.clear();
        inTransaction = false;
        return queued;
    }

    public String discard() {
        if (!inTransaction) {
            return "ERR DISCARD without MULTI";
        }
        commandQueue.clear();
        inTransaction = false;
        return "OK";
    }
}