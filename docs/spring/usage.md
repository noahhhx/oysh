# oysh spring - Usage

---

## Dependency

```xml
<dependency>
    <groupId>io.github.noahhhx</groupId>
    <artifactId>oysh-spring-boot-starter</artifactId>
    <version>${oysh.core.version}</version>
</dependency>
```

This transitively brings in `oysh-core`, `spring-boot-autoconfigure`, `picocli`, and
`picocli-shell-jline3`.

---

## Quick start

1. Enable the shell on your `@SpringBootApplication` class:

```java
@SpringBootApplication
@EnableOyshShell
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

2. Turn it on and set a host key path in `application.yml`:

```yaml
oysh:
  enabled: true
  host-key-path: hostkey.ser
```

No `hostkey.ser` file yet? The server generates one on first start.

3. Start the app. The shell listens on `127.0.0.1:2222` by default with public-key
   authentication against `authorized_keys`.

```shell
ssh -p 2222 oysh@127.0.0.1   # public-key
```

---

## The mental model

The starter's auto-configuration builds the same object graph described in the core
docs — `SshShellServer`, `SshShellFactory`, `ShellRunner`, `RoleResolver` — but wires
them as Spring beans. The main additions:

- The `ShellRunner` is a `PicocliShellRunner` that discovers every `@SshCommand` bean
  and builds a Picocli command tree per session.
- A `SmartLifecycle` bean starts the server late in the context lifecycle (phase
  `Integer.MAX_VALUE - 1000`) and stops it on shutdown.
- `SshCommandExecutedEvent` is published after each command, so you can listen to it
  with a plain `@EventListener`.

As with core, "per-session state == per-thread state". Call `SshSession.current()` from
inside a command's `run()` or `call()` method to access the principal, terminal, and roles.

---

## Minimal example

A Spring Boot app with one public command and one admin-gated command:

```java
@SpringBootApplication
@EnableOyshShell
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

```java
@Component
public class MyAuth {
    @Bean
    PasswordAuthenticator passwordAuthenticator() {
        return (user, pass, session) -> "admin".equals(user) && "admin".equals(pass);
    }

    @Bean
    RoleResolver roleResolver() {
        return principal -> "admin".equals(principal) ? Set.of("ADMIN") : Set.of();
    }
}
```

```java
@SshCommand
@Command(name = "whoami", description = "Show your identity.")
public class WhoamiCommand implements Runnable {
    @Override
    public void run() {
        SshSession session = SshSession.current();
        session.writeLine(session.getPrincipal() + " : " + session.getRoles());
    }
}
```

```java
@SshCommand(roles = "ADMIN")
@Command(name = "shutdown", description = "Shut down the app.")
public class ShutdownCommand implements Runnable {

    private final ApplicationContext ctx;

    public ShutdownCommand(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run() {
        SshSession.current().writeLine("Shutting down...");
        SpringApplication.exit(ctx, () -> 0);
    }
}
```

Connect and test:

```shell
ssh -p 2222 admin@127.0.0.1   # password: admin
oysh> whoami
admin : [ADMIN]
oysh> shutdown
Shutting down...
```

A non-admin user won't even see `shutdown` in the help output.

The above can also be found within the `example` module in the code base, with build
instructions for building and running.
[See example project here](https://github.com/noahhhx/oysh/tree/main/oysh-example/oysh-spring-boot-example).

---

## Configuration reference

All properties under the `oysh` prefix:

| Property | Type | Default | Description |
|---|---|---|---|
| `oysh.enabled` | `boolean` | `false` | Master switch |
| `oysh.host` | `String` | `127.0.0.1` | Bind address |
| `oysh.port` | `int` | `2222` | Listen port |
| `oysh.host-key-path` | `String` | `hostkey.ser` | Persistent host key file |
| `oysh.idle-timeout` | `Duration` | `10m` | Disconnect idle sessions |
| `oysh.prompt` | `String` | `oysh> ` | Interactive prompt string |
| `oysh.auth.authorized-keys-path` | `String` | `authorized_keys` | Path to OpenSSH authorized_keys |
