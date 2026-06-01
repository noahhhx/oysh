# oysh core - Auth

---

## Authentication

`SshShellServer` requires **at least one** authenticator. Pass MINA SSHD authenticator 
instances directly:

```java
// Public-key against an OpenSSH authorized_keys file (recommended):
PublickeyAuthenticator pk =
        new AuthorizedKeysAuthenticator(Path.of("authorized_keys"));
SshShellServer server = new SshShellServer(config, factory, pk, /* password */ null);

// Password:
PasswordAuthenticator pw = (user, pass, session) -> myUserStore.check(user, pass);
SshShellServer server = new SshShellServer(config, factory, /* pubkey */ null, pw);

// Both — either method may satisfy the client:
SshShellServer server = new SshShellServer(config, factory, pk, pw);
```

The authenticated user name becomes `SshSession.getPrincipal()`, which is then fed to your
`RoleResolver`.

---

## Authorization

`RoleResolver` maps a principal to roles; `SshSession.hasAnyRole(...)` checks them. The core
does not enforce anything itself — your `ShellRunner` decides what to do with roles. For
example, hide or refuse a command:

```java
case "top-secret" -> {
    if (session.hasAnyRole("ADMIN")) {
        session.writeLine("super secret config applied");
    } else {
        session.writeLine("permission denied (requires ADMIN)");
    }
}
```