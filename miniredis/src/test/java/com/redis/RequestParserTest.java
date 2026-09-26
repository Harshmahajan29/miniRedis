package com.redis;

import com.command.Command;
import com.command.RequestParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RequestParserTest {

    @Test
    void testInlineCommandParsing() {
        Command cmd = RequestParser.parse("SET user:1 Harsh");
        assertNotNull(cmd);
        assertEquals("SET", cmd.getName());
        assertEquals(2, cmd.getArgs().size());
        assertEquals("user:1", cmd.getArgs().get(0));
        assertEquals("Harsh", cmd.getArgs().get(1));
    }

    @Test
    void testCaseInsensitivity() {
        Command cmd = RequestParser.parse("get user:1");
        assertNotNull(cmd);
        assertEquals("GET", cmd.getName());
    }

    @Test
    void testEmptyInputHandling() {
        Command cmd = RequestParser.parse("   ");
        assertNull(cmd);
    }
}