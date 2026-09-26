package com.command;

public class CommandExecutor {

    public String execute(Command command) {
        if (command == null) {
            return "ERR empty command";
        }

        switch (command.getName()) {
            case "PING":
                if (!command.getArgs().isEmpty()) {
                    return command.getArgs().get(0);
                }
                return "PONG";

            case "ECHO":
                if (command.getArgs().isEmpty()) {
                    return "ERR wrong number of arguments for 'echo' command";
                }
                return String.join(" ", command.getArgs());

            case "SET":
                if (command.getArgs().size() < 2) {
                    return "ERR wrong number of arguments for 'set' command";
                }
                return "OK (Storage coming in Phase 3)";

            case "GET":
                if (command.getArgs().size() < 1) {
                    return "ERR wrong number of arguments for 'get' command";
                }
                return "nil (Storage coming in Phase 3)";

            default:
                return "ERR unknown command '" + command.getName() + "'";
        }
    }
}