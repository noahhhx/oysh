package io.github.noahhhx.oysh;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;

/**
 * Complete and minimal standalone example, listens on port 2223 and serves a tiny
 * shell. The only framework used is JLine (transitvely) for line editing.
 */
public class StandaloneExample {

    public static void main(String[] args) throws InterruptedException {
        // Implement the REPL.
        ShellRunner runner = session -> {
            LineReader reader = LineReaderBuilder.builder().terminal(
                  session.getTerminal()
            ).build();
            session.writeLine("Hi " + session.getPrincipal() + " - type help," 
                  + " whoami, quit");
            while (true) {
                String line;
                try {
                    line = reader.readLine("demo> ");
                } catch (EndOfFileException | UserInterruptException e) {
                    return; // Ctrl-D or Ctrl-C session end.
                }
                if (line == null) {
                    return;
                }
                switch (line.trim()) {
                    case "" -> {}
                    case "help" -> session.writeLine("commands: help, whoami, quit");
                    case "whoami" -> session.writeLine(
                          session.getPrincipal() + " : " + session.getRoles());
                    case "quit", "exit" -> { return; }
                    default -> session.writeLine("unknown command: " + line.trim());
                }
                
            }
        };
        
        // 2. Map principals to roles
        RoleResolver roles = principal -> Set.of("ADMIN");
        
        // 3. One shell per channel;
        SshShellFactory factory = new SshShellFactory(runner, roles);
        
        // 4. Authentication. DEMO password only - use public keys!
        PasswordAuthenticator auth = (u, p, s) -> "admin".equals(u) && "admin".equals(p);
        
        // 5. Transport config + server.
        SshShellConfig config = new SshShellConfig(
              "127.0.0.1", 2223, Path.of("hostkey-standalone.ser"),
              Duration.ofMinutes(10));
        SshShellServer server = new SshShellServer(config, factory, null, auth);
        
        server.start();
        System.out.println("standalone ssh shell listening on 2223");
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        new CountDownLatch(1).await(); // keep JVM alive
        
    }
}