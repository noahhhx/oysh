package io.github.noahhhx.oysh.boot;

import io.github.noahhhx.oysh.SshSession;
import org.springframework.boot.actuate.health.HealthEndpoint;
import picocli.CommandLine.Command;

/**
 * Demo command with no role restriction - available to all users.
 */
@SshCommand
@Command(name = "status", description = "Show application status.")
public class StatusCommand implements Runnable {
    
    private final HealthEndpoint health;

    public StatusCommand(HealthEndpoint health) {
        this.health = health;
    }

    @Override
    public void run() {
        SshSession session = SshSession.current();
        session.writeLine("user           : " + session.getPrincipal());
        session.writeLine("roles          : " + session.getRoles());
        session.writeLine("health status  : " + health.health().getStatus());
    }
}
