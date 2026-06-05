# oysh spring - Auth

---

## Authentication

The starter requires **at least one** authenticator. Define a `PasswordAuthenticator`
and/or `PublickeyAuthenticator` bean in your context — they'll be picked up and passed
to `SshShellServer` automatically.

```java
// Password authentication
@Bean
PasswordAuthenticator passwordAuthenticator() {
    return (user, pass, session) -> myUserStore.check(user, pass);
}

// Public-key authentication against an authorized_keys file
@Bean
PublickeyAuthenticator publickeyAuthenticator() {
    return new AuthorizedKeysAuthenticator(Path.of("authorized_keys"));
}
```

If you define neither, the auto-configuration registers a **default
`PublickeyAuthenticator`** backed by the file at `oysh.auth.authorized-keys-path`
(default: `authorized_keys`).

Both beans use `@ConditionalOnMissingBean`, so any authenticator you define replaces the
default, not augments it.

---

## Per-session user identity

Once authenticated, `SshSession.getPrincipal()` returns the user name. This is fed to your
`RoleResolver` to determine roles. Declare a `RoleResolver` bean to override the default
(which returns an empty set for everyone):

```java
@Bean
RoleResolver roleResolver() {
    return principal -> {
        return "admin".equals(principal)
            ? Set.of("ADMIN", "READ")
            : Set.of("READ");
    };
}
```

---

## Authorization

Roles don't enforce anything on their own. The starter uses them in two places:

1. **Command visibility** — `@SshCommand(roles = "ADMIN")` hides the command from
   sessions that lack the role. The command won't appear in help output and can't be
   invoked by unprivileged users.

2. **Inline checks** — call `SshSession.current().hasAnyRole("ADMIN")` inside a command's
   run logic for finer-grained decisions:

```java
@SshCommand
@Command(name = "config", description = "View or set configuration.")
public class ConfigCommand implements Runnable {

    @Option(names = "--set", description = "Value to set.")
    private String newValue;

    @Override
    public void run() {
        SshSession session = SshSession.current();
        if (newValue != null && !session.hasAnyRole("ADMIN")) {
            session.writeLine("permission denied (requires ADMIN)");
            return;
        }
        // ...
    }
}
```
