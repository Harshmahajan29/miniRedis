package com.redis;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.command.*;
import com.storage.*;

public class RedisServer {
    private static final String AOF_FILE = "appendonly.aof";
    private final int port;
    private final ExecutorService threadPool;
    private final Storage storage;
    private final AofManager aofManager;
    private volatile boolean isRunning = true;

    public RedisServer(int port) {
        this.port = port;
        this.threadPool = Executors.newCachedThreadPool();
        this.storage = new Storage();
        this.aofManager = new AofManager(AOF_FILE);

        // Run recovery replay before opening server to clients
        CommandExecutor recoveryExecutor = new CommandExecutor(storage, null);
        RecoveryManager.recover(AOF_FILE, recoveryExecutor);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("MiniRedis server running on port " + port + "...");

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new ClientConnection(clientSocket, storage, aofManager));
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
        } finally {
            aofManager.close();
            threadPool.shutdown();
        }
    }

    public void stop() {
        this.isRunning = false;
    }
}