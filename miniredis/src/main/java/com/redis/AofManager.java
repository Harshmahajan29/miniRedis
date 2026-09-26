package com.redis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class AofManager {
    private final String filePath;
    private PrintWriter writer;
    private boolean enabled = true;

    public AofManager(String filePath) {
        this.filePath = filePath;
        try {
            // Open file in append mode (true)
            this.writer = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)), true);
        } catch (IOException e) {
            System.err.println("Failed to initialize AOF writer: " + e.getMessage());
            this.enabled = false;
        }
    }

    public synchronized void append(String rawCommand) {
        if (!enabled || writer == null) {
            return;
        }
        writer.println(rawCommand);
    }

    public synchronized void close() {
        if (writer != null) {
            writer.close();
        }
    }
}