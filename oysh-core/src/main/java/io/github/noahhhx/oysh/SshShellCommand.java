package io.github.noahhhx.oysh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.jline.terminal.Terminal;
import org.jline.terminal.impl.ExternalTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SSHD instantiates one of these per channel and calls the stream setters followed by 
 * {@link #start}. Builds a {@link Terminal} from this channel's byte streams, binds a fresh
 * {@link SshSession} to the session thread and hand control to runner.
 */
public class SshShellCommand implements Command {
    
    private static final Logger log = LoggerFactory.getLogger(SshShellCommand.class);
    
    private final ShellRunner shellRunner;
    private final RoleResolver roleResolver;
    
    private InputStream is;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback exitCallback;
    private Thread thread;
    private volatile Terminal terminal;

    public SshShellCommand(ShellRunner shellRunner, RoleResolver roleResolver) {
        this.shellRunner = shellRunner;
        this.roleResolver = roleResolver;
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        this.exitCallback = callback;
    }

    @Override
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    @Override
    public void setInputStream(InputStream in) {
        this.is = in;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void start(ChannelSession channel, Environment env) throws IOException {
        String principal = channel.getServerSession().getUsername();
        thread = new Thread(() -> runSession(principal, env), "ssh-shell-" + principal);
        thread.setDaemon(true);
        thread.start();
    }
    
    private void runSession(String principal, Environment env) {
        int exitCode = 0;
        try {
            String termType = env.getEnv().getOrDefault("TERM", "xterm-256color");
            // Build a Java terminal directly from the SSH channel streams.
            // External Terminal needs no native provider.
            this.terminal = new ExternalTerminal(
                  "ssh", termType, is, out, StandardCharsets.UTF_8);

            Set<String> roles = roleResolver.rolesForPrincipal(principal);
            SshSession session = new SshSession(principal, roles, terminal);
            SshSession.bind(session);
            
            try {
                log.info("SSH shell session started for '{}'", principal);
                shellRunner.run(session);
            } finally {
                SshSession.unbind();
            }
        } catch (Exception e) {
            exitCode = 1;
            log.warn("SSH shell session for '{}' ended with error", principal, e);
        } finally {
            closeQuietly();
            if (exitCallback != null) {
                exitCallback.onExit(exitCode);
            }
            log.info("SSH shell session ended for '{}'", principal);
        }
    }

    @Override
    public void destroy(ChannelSession channel) throws Exception {
        if (thread != null) {
            thread.interrupt();
        }
        closeQuietly();
    }
    
    private void closeQuietly() {
        if (terminal != null) {
            try {
                terminal.close();
            } catch (Exception ignore) {
                // ignore
            }
        }
    }
}
