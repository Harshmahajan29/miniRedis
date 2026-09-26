package com.redis;

import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PubSubManager {
    // Channel Name -> Set of subscriber PrintWriters
    private final ConcurrentHashMap<String, Set<PrintWriter>> channelSubscribers = new ConcurrentHashMap<>();

    public void subscribe(String channel, PrintWriter writer) {
        channelSubscribers.computeIfAbsent(channel, k -> ConcurrentHashMap.newKeySet()).add(writer);
    }

    public void unsubscribe(String channel, PrintWriter writer) {
        Set<PrintWriter> subscribers = channelSubscribers.get(channel);
        if (subscribers != null) {
            subscribers.remove(writer);
            if (subscribers.isEmpty()) {
                channelSubscribers.remove(channel);
            }
        }
    }

    public void unsubscribeAll(PrintWriter writer) {
        for (String channel : channelSubscribers.keySet()) {
            unsubscribe(channel, writer);
        }
    }

    public int publish(String channel, String message) {
        Set<PrintWriter> subscribers = channelSubscribers.get(channel);
        if (subscribers == null || subscribers.isEmpty()) {
            return 0;
        }

        int deliveredCount = 0;
        String formattedMessage = "1) \"message\"\n2) \"" + channel + "\"\n3) \"" + message + "\"";

        for (PrintWriter writer : subscribers) {
            try {
                writer.println(formattedMessage);
                deliveredCount++;
            } catch (Exception e) {
                // If writing fails, prune broken client connection
                subscribers.remove(writer);
            }
        }
        return deliveredCount;
    }
}