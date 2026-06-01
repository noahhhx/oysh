# oysh core

The core module is the framework-agnostic building blocks for serving an interactive shell
over SSH from a Java program. This module leverages [Apache SSHD](https://github.com/apache/mina-sshd)
and [Jline](https://github.com/jline/jline3) in order to achieve this. Everything works in a plain
`main()`, or any Framework of choice.

## What it gives you

- **`SshShellServer`** — owns the Apache MINA `SshServer` lifecycle (`start()` / `stop()` /
  `isRunning()`).
- **`SshShellFactory`** — a MINA `ShellFactory` that creates one shell per channel.
- **`SshShellCommand`** — the per-channel bridge (internal). For each interactive channel it
  builds a pure-Java JLine terminal from the channel's streams, binds a session, and invokes
  your runner. You normally never touch this class directly.
- **`SshSession`** — the per-session handle: `principal()`, `roles()`, `terminal()`,
  `writer()`, `writeLine(...)`, `hasAnyRole(...)`, and the static `SshSession.current()`.
- **`ShellRunner`** *(you implement this)* — the read-eval-print loop for one session.
- **`RoleResolver`** *(you implement, or `RoleResolver.empty()`)* — maps an authenticated
  principal to a set of roles, for authorization.
- **`SshShellConfig`** — immutable transport config: host, port, host-key path, idle timeout.

## What it doesn't

- It does **not** parse commands or provide a command framework. That is the `ShellRunner`'s
  job — plug in picocli, raw JLine, Spring Shell, or a hand-rolled dispatcher.
- It does **not** do authentication. You supply a MINA SSHD `PasswordAuthenticator` and/or
  `PublickeyAuthenticator`.
- It does **not** keep your process alive. MINA's IO threads are daemons, so a bare `main()`
  must block (e.g. on a latch).