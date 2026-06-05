# oysh spring - Events

---

## SshCommandExecutedEvent

After every non-blank command line is processed, the `PicocliShellRunner` publishes a
`SshCommandExecutedEvent` on the Spring `ApplicationEventPublisher`:

```java
public record SshCommandExecutedEvent(
      String principal,
      String commandLine,
      Instant at) {
}
```

Listen for it with a plain `@EventListener` to build an audit trail without coupling to
shell internals:

```java
@Component
public class AuditConfig {

    private static final Logger log = LoggerFactory.getLogger(AuditConfig.class);

    @EventListener
    public void onCommand(SshCommandExecutedEvent event) {
        log.info("SSH audit | user={} | cmd='{}' | at={}",
              event.principal(), event.commandLine(), event.at());
    }
}
```

The event is published on the session thread, so your listener runs synchronously in the
session's call stack. If your listener does I/O or takes time, consider making it
`@Async`.
