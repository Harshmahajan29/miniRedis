package com.command;

import java.util.List;

public class Command {
    private final String name;
    private final List<String> args;

    public Command(String name, List<String> args) {
        this.name = name.toUpperCase(); // Redis command names are case-insensitive
        this.args = args;
    }

    public String getName() {
        return name;
    }

    public List<String> getArgs() {
        return args;
    }
}