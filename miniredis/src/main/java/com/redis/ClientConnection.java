package com.redis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import com.command.*;

public class ClientConnection implements Runnable {
    private final Socket clientSocket;
    private final CommandExecutor commandExecutor;

    public ClientConnection(Socket socket) {
        this.clientSocket = socket;
        this.commandExecutor = new CommandExecutor();
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
                // 1. Parse raw text into a Command
                Command command = RequestParser.parse(inputLine);
                
                // 2. Execute command and obtain result string
                String response = commandExecutor.execute(command);
                
                // 3. Send response back to client
                writer.println(response);
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