# oysh core - Usage

---

## The mental model

Apache MINA SSHD calls `ShellFactory.createShell(...)` **once per interactive channel**, gives
that shell its own input/output streams, and runs it on its **own thread**. This module turns
that into:

1. A pure-Java JLine `Terminal` built from the channel streams (no native PTY needed).
2. A fresh `SshSession` (principal, roles, terminal) **bound to that thread** via a
   `ThreadLocal`.
3. A call to your `ShellRunner.run(session)`. Returning from it (e.g. on Ctrl-D) ends the SSH
   session.

So "per-session state" == "per-thread state". Anywhere on the session thread you can call
`SshSession.current()` to find out who is connected and which terminal to write to.

---

## Dependency

```xml
<dependency>
    <groupId>io.github.noahhhx</groupId>
    <artifactId>oysh-core</artifactId>
    <version>${oysh.core.version}</version>
</dependency>
```

This transitively brings `org.apache.sshd:sshd-core` and `org.jline:jline`. Add an SLF4J
binding of your choice if you want to see the module's logs.

--- 

## Minimal standalone example

Complete and minimal standalone example, listens on port 2223 and serves a tiny
shell. The only framework used is JLine (transitive) for line editing.

The following can also be found within the `example` module in the code base, with build 
instructions for building and running.
[See example project here](https://github.com/noahhhx/oysh/tree/main/oysh-example/oysh-standalone-example).

```java
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
```

To test it out:

```shell
ssh -p 2223 admin@127.0.0.1   # demo password: admin
demo> whoami
admin : ADMIN
demo> quit
```