package com.redis;

import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import com.storage.*;

public class MetricsManager {
    private final long startTimeMillis;
    private final AtomicLong totalCommandsProcessed = new AtomicLong(0);
    private final AtomicLong keyspaceHits = new AtomicLong(0);
    private final AtomicLong keyspaceMisses = new AtomicLong(0);
    private final Set<PrintWriter> monitorClients = ConcurrentHashMap.newKeySet();

    public MetricsManager() {
        this.startTimeMillis = System.currentTimeMillis();
    }

    public void incrementCommands() {
        totalCommandsProcessed.incrementAndGet();
    }

    public void recordHit() {
        keyspaceHits.incrementAndGet();
    }

    public void recordMiss() {
        keyspaceMisses.incrementAndGet();
    }

    public void registerMonitor(PrintWriter writer) {
        monitorClients.add(writer);
    }

    public void unregisterMonitor(PrintWriter writer) {
        monitorClients.remove(writer);
    }

    public void publishToMonitors(String commandText, String clientAddress) {
        if (monitorClients.isEmpty()) {
            return;
        }

        double timestamp = System.currentTimeMillis() / 1000.0;
        String logLine = String.format("%.6f [0 %s] \"%s\"", timestamp, clientAddress, commandText);

        for (PrintWriter writer : monitorClients) {
            try {
                writer.println(logLine);
            } catch (Exception e) {
                monitorClients.remove(writer);
            }
        }
    }

    public String getInfo(Storage storage, int activeClientCount) {
        long uptimeSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000;
        long hits = keyspaceHits.get();
        long misses = keyspaceMisses.get();
        long totalLookups = hits + misses;
        double hitRate = totalLookups > 0 ? ((double) hits / totalLookups) * 100.0 : 0.0;

        StringBuilder sb = new StringBuilder();
        sb.append("# Server\n");
        sb.append("miniredis_version:1.0.0\n");
        sb.append("uptime_in_seconds:").append(uptimeSeconds).append("\n\n");

        sb.append("# Clients\n");
        sb.append("connected_clients:").append(activeClientCount).append("\n\n");

        sb.append("# Stats\n");
        sb.append("total_commands_processed:").append(totalCommandsProcessed.get()).append("\n");
        sb.append("keyspace_hits:").append(hits).append("\n");
        sb.append("keyspace_misses:").append(misses).append("\n");
        sb.append("cache_hit_rate:").append(String.format("%.2f", hitRate)).append("%\n\n");

        sb.append("# Keyspace\n");
        sb.append("total_keys:").append(storage.getTotalKeys()).append("\n");

        return sb.toString().trim();
    }
}