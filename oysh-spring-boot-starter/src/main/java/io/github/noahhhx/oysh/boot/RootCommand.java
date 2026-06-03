package io.github.noahhhx.oysh.boot;

import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.shell.jline3.PicocliCommands.ClearScreen;

/**
 * The top-level command for the interactive REPL. Registered 
 */
@Command(
      name = "",
      description = "Oysh shell. Type 'help' to list commands, Ctrl-D to exit.",
      subcommands = {HelpCommand.class, ClearScreen.class}
)
public class RootCommand implements Runnable {

    @Override
    public void run() {
        // No-op: the REPL only dispatches subcommands.
    }
}
