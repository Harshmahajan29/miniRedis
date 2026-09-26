package com.redis;

import com.storage.Storage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class ServerIntegrationTest {
    private ServerSocket serverSocket;
    private Storage storage;
    private Thread serverThread;
    private static final int TEST_PORT = 6380;

    @BeforeEach
    void startServer() throws Exception {
        storage = new Storage(100);
        serverSocket = new ServerSocket(TEST_PORT);

        serverThread = new Thread(() -> {
            try {
                while (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();
                    new Thread(new ClientConnection(socket, storage, null)).start();
                }
            } catch (Exception ignored) {}
        });
        serverThread.start();
    }

    @AfterEach
    void stopServer() throws Exception {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    @Test
    void testFullSocketExecutionAndInfoCommand() throws Exception {
        try (Socket client = new Socket("localhost", TEST_PORT);
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {

            out.println("SET name MiniRedis");
            assertEquals("OK", in.readLine());

            out.println("GET name");
            assertEquals("MiniRedis", in.readLine());

            out.println("INFO");
            String line = in.readLine();
            assertTrue(line.contains("# Server") || line.contains("miniredis_version"));
        }
    }

    @Test
    void testTransactionFlow() throws Exception {
        try (Socket client = new Socket("localhost", TEST_PORT);
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {

            out.println("MULTI");
            assertEquals("OK", in.readLine());

            out.println("SET txKey txVal");
            assertEquals("QUEUED", in.readLine());

            out.println("EXEC");
            String response = in.readLine();
            assertTrue(response.contains("OK") || response.contains("1) OK"));
        }
    }
}