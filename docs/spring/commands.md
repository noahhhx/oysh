# oysh spring - Commands

---

## Writing a command

An SSH command is a Picocli `@Command` class annotated with `@SshCommand`. The annotation
is meta-annotated with `@Component`, so the class is picked up by component scanning and
auto-registered as a shell subcommand.

```java
@SshCommand
@Command(name = "hello", description = "Says hello.")
public class HelloCommand implements Runnable {
    @Override
    public void run() {
        SshSession.current().writeLine("Hello, world!");
    }
}
```

Every command has access to `SshSession.current()` for writing output, checking roles, and
reading the principal name.

---

## Role-gated commands

Pass roles to `@SshCommand` to restrict visibility:

```java
@SshCommand(roles = "ADMIN")
@Command(name = "top-secret", description = "Admin-only operation.")
public class TopSecretCommand implements Runnable {
    @Override
    public void run() {
        SshSession.current().writeLine("Super secret data.");
    }
}
```

Sessions that don't hold the required roles won't see the command in `help` output and
can't invoke it.

---

## Picocli features

The starter uses Picocli with the JLine3 shell integration. You get the full Picocli
feature set:

```java
@SshCommand
@Command(name = "user", description = "Manage users.",
         mixinStandardHelpOptions = true,
         subcommands = {HelpCommand.class})
public class UserCommand implements Runnable {

    @Option(names = {"-n", "--name"}, description = "User name.", required = true)
    private String name;

    @Option(names = "--admin", description = "Grant admin role.")
    private boolean admin;

    @Override
    public void run() {
        SshSession session = SshSession.current();
        session.writeLine("User: " + name + ", admin: " + admin);
    }
}
```

Autocompletion, `--help` flags, subcommands, positional parameters, type conversion —
all work out of the box.

---

## Dependency injection

Commands are Spring beans. Inject any bean from the context via constructor injection:

```java
@SshCommand
@Command(name = "health", description = "Show application health.")
public class HealthCommand implements Runnable {

    private final HealthEndpoint healthEndpoint;

    public HealthCommand(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    @Override
    public void run() {
        var health = healthEndpoint.health();
        SshSession.current().writeLine(health.getStatus().toString());
    }
}
```

The `SpringIFactory` resolves command classes from the Spring context and falls back to
Picocli's default factory with autowiring for non-bean types — so nested command classes
and mixins also work.

---

## Built-in commands

The command tree always includes:

- **`help`** — lists all visible commands (respects role gates).
- **`cls`** — clears the terminal screen.

These come from Picocli's `HelpCommand` and `ClearScreen` classes, wired into the
`RootCommand`.
