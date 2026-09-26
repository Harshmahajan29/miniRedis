package com.command;

import java.util.List;

public class Command {
    private final String name;
    private final List<String> args;
    private final String rawInput;

    public Command(String name, List<String> args, String rawInput) {
        this.name = name.toUpperCase();
        this.args = args;
        this.rawInput = rawInput;
    }

    public String getName() {
        return name;
    }

    public List<String> getArgs() {
        return args;
    }

    public String getRawInput() {
        return rawInput;
    }
}