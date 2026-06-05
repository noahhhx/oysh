package io.github.noahhhx.oysh.boot;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.shell.jline3.PicocliCommands.ClearScreen;

class RootCommandTest {

    @Test
    void hasCorrectCommandAnnotation() {
        Command cmd = RootCommand.class.getAnnotation(Command.class);
        assertEquals("", cmd.name());
        assertTrue(cmd.description().length > 0);

        assertEquals(2, cmd.subcommands().length);
        boolean hasHelp = false;
        boolean hasClear = false;
        for (Class<?> sc : cmd.subcommands()) {
            if (sc == HelpCommand.class) hasHelp = true;
            if (sc == ClearScreen.class) hasClear = true;
        }
        assertTrue(hasHelp, "should have HelpCommand as subcommand");
        assertTrue(hasClear, "should have ClearScreen as subcommand");
    }

    @Test
    void runDoesNothing() {
        RootCommand cmd = new RootCommand();
        assertDoesNotThrow(cmd::run);
    }
}
