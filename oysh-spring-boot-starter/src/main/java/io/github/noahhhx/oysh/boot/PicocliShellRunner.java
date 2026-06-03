package io.github.noahhhx.oysh.boot;

import io.github.noahhhx.oysh.ShellRunner;
import io.github.noahhhx.oysh.SshSession;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.function.Supplier;
import org.jline.console.SystemRegistry;
import org.jline.console.impl.SystemRegistryImpl;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Parser;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.springframework.context.ApplicationEventPublisher;
import picocli.CommandLine;
import picocli.shell.jline3.PicocliCommands;

/**
 * A {@link ShellRunner} that drives an interactive Picocli REPL over the session's JLine terminal.
 * Builds a fresh command tree per session so that auth and any per-session state are isolated.
 */
public class PicocliShellRunner implements ShellRunner {

    private final SshCommandRegistry registry;
    private final CommandLine.IFactory factory;
    private final ApplicationEventPublisher eventPublisher;
    private final String prompt;

    public PicocliShellRunner(SshCommandRegistry registry, CommandLine.IFactory factory,
          ApplicationEventPublisher eventPublisher, String prompt) {
        this.registry = registry;
        this.factory = factory;
        this.eventPublisher = eventPublisher;
        this.prompt = prompt;
    }

    @Override
    public void run(SshSession session) throws Exception {
        Terminal terminal = session.getTerminal();

        CommandLine root = buildCommandTree(session);

        Parser parser = new DefaultParser();
        Supplier<Path> workDir = () -> Paths.get(System.getProperty("user.dir"));

        SystemRegistry systemRegistry = new SystemRegistryImpl(parser, terminal, workDir, null);
        PicocliCommands picocliCommands = new PicocliCommands(root);
        systemRegistry.setCommandRegistries(picocliCommands);

        LineReader reader = LineReaderBuilder.builder()
              .terminal(terminal)
              .completer(systemRegistry.completer())
              .parser(parser)
              .build();
        
        session.writeLine("Connected as '" + session.getPrincipal() + "'. Type 'help' for commands.");
        
        while (true) {
            try {
                systemRegistry.cleanUp();
                String line = reader.readLine(prompt);
                if (line == null) {
                    return;
                }
                systemRegistry.execute(line);
                if (!line.isBlank()) {
                    eventPublisher.publishEvent(new SshCommandExecutedEvent(
                          session.getPrincipal(), line.trim(), Instant.now()));
                }
            } catch (UserInterruptException e) {
                // Ctrl-C: abandon current line, keep session open.
            } catch (EndOfFileException e) {
                // Ctrl-D: exit session
                return;
            } catch (Exception e) {
                systemRegistry.trace(e);
            }
        }
    }

    private CommandLine buildCommandTree(SshSession session) {
        CommandLine root = new CommandLine(new RootCommand(), factory);
        for (SshCommandRegistry.Entry entry : registry.getEntries()) {
            if (session.hasAnyRole(entry.roles())) {
                root.addSubcommand(entry.bean());
            }
        }
        return root;
    }
}
