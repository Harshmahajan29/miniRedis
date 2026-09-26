package com.redis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.command.*;


public class RecoveryManager {

    public static void recover(String filePath, CommandExecutor executor) {
        File aofFile = new File(filePath);
        if (!aofFile.exists()) {
            System.out.println("No existing AOF file found. Starting with a fresh database.");
            return;
        }

        System.out.println("Recovering database state from " + filePath + "...");
        int commandCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(aofFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                Command cmd = RequestParser.parse(line);
                if (cmd != null) {
                    executor.executeInternal(cmd); // Replay without re-logging to AOF
                    commandCount++;
                }
            }
            System.out.println("Recovery complete. Replayed " + commandCount + " commands from AOF.");
        } catch (IOException e) {
            System.err.println("Error during AOF recovery: " + e.getMessage());
        }
    }
}