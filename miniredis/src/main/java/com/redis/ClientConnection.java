package com.redis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.storage.*;
import com.command.*;


public class ClientConnection implements Runnable {
    private static final AtomicInteger activeClients = new AtomicInteger(0);

    private final Socket clientSocket;
    private final Storage storage;
    private final CommandExecutor commandExecutor;
    private final TransactionManager transactionManager;

    public ClientConnection(Socket socket, Storage storage, AofManager aofManager) {
        this.clientSocket = socket;
        this.storage = storage;
        this.commandExecutor = new CommandExecutor(storage, aofManager);
        this.transactionManager = new TransactionManager();
    }

    @Override
    public void run() {
        activeClients.incrementAndGet();
        String clientAddress = clientSocket.getRemoteSocketAddress().toString();

        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                Command command = RequestParser.parse(inputLine);
                if (command == null) continue;

                String cmdName = command.getName();

                // Broadcast command line to registered MONITOR clients
                if (!"MONITOR".equals(cmdName)) {
                    storage.getMetricsManager().publishToMonitors(command.getRawInput(), clientAddress);
                }

                // Handle INFO command
                if ("INFO".equals(cmdName)) {
                    writer.println(storage.getMetricsManager().getInfo(storage, activeClients.get()));
                    continue;
                }

                // Handle MONITOR command
                if ("MONITOR".equals(cmdName)) {
                    storage.getMetricsManager().registerMonitor(writer);
                    writer.println("OK");
                    continue;
                }

                // Existing Pub/Sub, Transaction, and Command routing...
                writer.println(commandExecutor.execute(command));
            }
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        } finally {
            activeClients.decrementAndGet();
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}