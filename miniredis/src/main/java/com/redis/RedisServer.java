package com.redis;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisServer {
    private final int port;
    private final ExecutorService threadPool;
    private volatile boolean isRunning = true;

    public RedisServer(int port) {
        this.port = port;
        // Use a dynamic thread pool to handle concurrent client connections
        this.threadPool = Executors.newCachedThreadPool();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("MiniRedis server started on port " + port + "...");

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new ClientConnection(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Server exception encountered: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }

    public void stop() {
        this.isRunning = false;
    }
}