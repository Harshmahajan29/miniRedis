package com.redis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.command.*;
import com.storage.*;



public class ClientConnection implements Runnable {
    private final Socket clientSocket;
    private final CommandExecutor commandExecutor;

    public ClientConnection(Socket socket, Storage storage, AofManager aofManager) {
        this.clientSocket = socket;
        this.commandExecutor = new CommandExecutor(storage, aofManager);
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
                Command command = RequestParser.parse(inputLine);
                String response = commandExecutor.execute(command);
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