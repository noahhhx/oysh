# oysh spring

The Spring Boot starter wraps the core module into a familiar Spring-style experience:
properties-driven config, auto-wired beans, and a Picocli-powered REPL with role-based
command visibility. Add the dependency, annotate a config class, and write `@SshCommand`
beans — the rest is wired for you.

## What it gives you

- **`@EnableOyshShell`** — imports the auto-configuration. Opt-in by design; opening a
  network port should never be a side-effect of a classpath dependency.
- **`OyshProperties`** — `application.yml` control over host, port, host-key path, idle
  timeout, and prompt, all under the `oysh.*` prefix.
- **`@SshCommand`** — a meta-annotation (`@Component` + discovery) that marks a Picocli
  `@Command` class as a shell subcommand, with optional role gates.
- **`PicocliShellRunner`** — a pre-built `ShellRunner` that drives a Picocli REPL over
  JLine, building a fresh command tree per session (so per-session auth state is
  isolated).
- **`SshCommandRegistry`** — discovers every `@SshCommand` bean at startup.
- **`SpringIFactory`** — a Picocli `IFactory` that resolves command dependencies from the
  Spring context with autowiring fallback.
- **`SshCommandExecutedEvent`** — published after every non-blank command, for audit
  trails.
- **Lifecycle management** — the SSH server starts/stops with the application context
  via `SmartLifecycle`.
- **Defaults with escape hatches** — every bean is `@ConditionalOnMissingBean`, so you
  override any piece: `ShellRunner`, `RoleResolver`, `PublickeyAuthenticator`,
  `PasswordAuthenticator`, `SshShellFactory`, `SshShellServer`, `CommandLine.IFactory`.

## What it doesn't

- It does **not** auto-configure by classpath alone. You must place `@EnableOyshShell` on
  a configuration class and set `oysh.enabled=true`.
- It does **not** provide password authentication out of the box — only a default
  `PublickeyAuthenticator` backed by an `authorized_keys` file.
- It does **not** ship Actuator health indicators or endpoints. The example module
  demonstrates wiring those into a custom `@SshCommand`.
- It does **not** enforce authorization. Role checks are done by `SshSession.hasAnyRole()`
  — your `ShellRunner` or commands decide what to do with them.
