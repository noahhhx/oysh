package io.github.noahhhx.oysh;

import java.io.IOException;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.shell.ShellFactory;

/**
 * Hands SSHD a fresh {@link SshShellCommand} for every interactive channel. This is the
 * per-session boundary: one factory call == one new shell == one new thread.
 */
public class SshShellFactory implements ShellFactory {

    private final ShellRunner shellRunner;
    private final RoleResolver roleResolver;

    public SshShellFactory(ShellRunner shellRunner, RoleResolver roleResolver) {
        this.shellRunner = shellRunner;
        this.roleResolver = roleResolver;
    }

    @Override
    public Command createShell(ChannelSession channel) throws IOException {
        return new SshShellCommand(shellRunner, roleResolver);
    }
}
