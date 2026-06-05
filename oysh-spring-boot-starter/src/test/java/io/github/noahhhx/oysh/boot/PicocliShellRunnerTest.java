package io.github.noahhhx.oysh.boot;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.noahhhx.oysh.ShellRunner;
import io.github.noahhhx.oysh.SshSession;
import io.github.noahhhx.oysh.SshShellServer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.jline.terminal.Terminal;
import org.jline.terminal.impl.ExternalTerminal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import picocli.CommandLine.Command;

@TestPropertySource(properties = "oysh.enabled=true")
class PicocliShellRunnerTest {

    @Nested
    @SpringBootTest(classes = RunnerApp.class)
    class WelcomeAndEof {

        @Autowired
        private ShellRunner shellRunner;

        @Autowired
        private SshShellServer server;

        @AfterEach
        void tearDown() {
            server.stop();
        }

        @Test
        void writesWelcomeBannerOnStart() throws Exception {
            ByteArrayOutputStream captured = new ByteArrayOutputStream();
            Terminal terminal = new ExternalTerminal("test", "xterm-256color",
                  new ByteArrayInputStream(new byte[0]),
                  captured,
                  StandardCharsets.UTF_8);

            SshSession session = new SshSession("testuser", Set.of(), terminal);
            shellRunner.run(session);

            String output = captured.toString(StandardCharsets.UTF_8);
            assertTrue(output.contains("Connected as 'testuser'"));
        }

        @Test
        void eofReturnsGracefully() throws Exception {
            ByteArrayOutputStream captured = new ByteArrayOutputStream();
            Terminal terminal = new ExternalTerminal("test", "xterm-256color",
                  new ByteArrayInputStream(new byte[0]),
                  captured,
                  StandardCharsets.UTF_8);

            SshSession session = new SshSession("eofuser", Set.of(), terminal);
            assertDoesNotThrow(() -> shellRunner.run(session));
        }

        @Test
        void blankInputHandledWithoutException() throws Exception {
            ByteArrayOutputStream captured = new ByteArrayOutputStream();
            Terminal terminal = new ExternalTerminal("test", "xterm-256color",
                  new ByteArrayInputStream("   \n".getBytes(StandardCharsets.UTF_8)),
                  captured,
                  StandardCharsets.UTF_8);
            SshSession session = new SshSession("blankuser", Set.of(), terminal);

            shellRunner.run(session);

            String output = captured.toString(StandardCharsets.UTF_8);
            assertTrue(output.contains("Connected as 'blankuser'"));
        }

        @Test
        void builtinClearScreenExecutesWithoutError() throws Exception {
            // Redirect System.err to capture picocli's handleUnhandled() output,
            // which currently prints the InitializationException stack trace there.
            PrintStream originalErr = System.err;
            ByteArrayOutputStream errCapture = new ByteArrayOutputStream();
            System.setErr(new PrintStream(errCapture));

            try {
                ByteArrayOutputStream captured = new ByteArrayOutputStream();
                Terminal terminal = new ExternalTerminal("test", "xterm-256color",
                      new ByteArrayInputStream("cls\n".getBytes(StandardCharsets.UTF_8)),
                      captured,
                      StandardCharsets.UTF_8);
                SshSession session = new SshSession("clsuser", Set.of(), terminal);

                shellRunner.run(session);

                String errOutput = errCapture.toString(StandardCharsets.UTF_8);
                assertFalse(errOutput.contains("Cannot instantiate"),
                      "ClearScreen built-in command should not fail with "
                            + "InitializationException. System.err contained: " + errOutput);
            } finally {
                System.setErr(originalErr);
            }
        }
    }

    @Configuration
    @EnableOyshShell
    @Import(HelloCommand.class)
    static class RunnerApp {
    }

    @SshCommand
    @Command(name = "hello", description = "Says hello")
    static class HelloCommand implements Runnable {
        @Override
        public void run() {
            SshSession.current().writeLine("Hello from test");
        }
    }
}
