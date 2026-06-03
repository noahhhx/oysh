package io.github.noahhhx.oysh.boot;

import io.github.noahhhx.oysh.SshSession;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Demo of a command that is secured by {@code RoleResolver}.
 */
@SshCommand(roles = "ADMIN")
@Command(name = "secure", description = "It's too secure to tell you")
public class SecureCommand implements Runnable {

    @Option(names = {"-f", "--force"}, description = "Forcefully execute this command.")
    private boolean force;

    @Override
    public void run() {
        SshSession session = SshSession.current();
        final String start = "You have passed the security check";
        final String end = ", now....";
        if (force) {
            session.writeLine(
                  start + " with force" + end
            );
        } else {
            session.writeLine(
                  start + end
            );
        }
    }
}
