package com.redis;

public class MiniRedis {
    private static final int DEFAULT_PORT = 6379;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port provided. Falling back to default port " + DEFAULT_PORT);
            }
        }

        RedisServer server = new RedisServer(port);
        server.start();
    }
}