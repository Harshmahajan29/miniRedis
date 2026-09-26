package com.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RequestParser {

    public static Command parse(String inputLine) {
        if (inputLine == null || inputLine.trim().isEmpty()) {
            return null;
        }

        String trimmed = inputLine.trim();
        String[] parts = trimmed.split("\\s+");
        String commandName = parts[0];

        List<String> args = new ArrayList<>();
        if (parts.length > 1) {
            args.addAll(Arrays.asList(parts).subList(1, parts.length));
        }

        return new Command(commandName, args, trimmed);
    }
}