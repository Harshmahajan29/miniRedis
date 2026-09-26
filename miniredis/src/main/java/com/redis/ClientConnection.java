package com.redis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientConnection implements Runnable {
    private final Socket clientSocket;

    public ClientConnection(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        String clientAddress = clientSocket.getRemoteSocketAddress().toString();
        System.out.println("Client connected: " + clientAddress);

        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                System.out.println("Received from " + clientAddress + ": " + inputLine);
                // Phase 1 verification: Echo response back to client
                writer.println("PONG: " + inputLine);
            }
        } catch (IOException e) {
            System.err.println("Connection error with client " + clientAddress + ": " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Client disconnected: " + clientAddress);
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
}