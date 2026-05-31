package com.noah.oysh;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.apache.sshd.core.CoreModuleProperties;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Owns the Apache {@link SshServer} lifecycle.
 * 
 * At least one authenticator (public-key or password) must be supplied.
 */
public class SshShellServer {

    private static final Logger log = LoggerFactory.getLogger(SshShellServer.class);
    
    private final SshShellConfig config;
    private final SshShellFactory shellFactory;
    @Nullable
    private final PublickeyAuthenticator publickeyAuthenticator;
    @Nullable
    private final PasswordAuthenticator passwordAuthenticator;
    
    private SshServer server;


    public SshShellServer(SshShellConfig config, SshShellFactory shellFactory,
          @Nullable PublickeyAuthenticator publickeyAuthenticator,
          @Nullable PasswordAuthenticator passwordAuthenticator) {
        if (publickeyAuthenticator == null && passwordAuthenticator == null) {
            throw new IllegalStateException(
                  "No authenticator configured - define a PublickeyAuthenticator or " +
                        "PasswordAuthenticator."
            );
        }
        this.config = config;
        this.shellFactory = shellFactory;
        this.publickeyAuthenticator = publickeyAuthenticator;
        this.passwordAuthenticator = passwordAuthenticator;
    }
    
    public synchronized void start() {
        if (server != null) {
            return;
        }
        SshServer s = SshServer.setUpDefaultServer();
        s.setHost(config.host());
        s.setPort(config.port());
        s.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(config.hostKeyPath()));
        s.setShellFactory(shellFactory);
        
        if (publickeyAuthenticator != null) {
            s.setPublickeyAuthenticator(publickeyAuthenticator);
        }
        if (passwordAuthenticator != null) {
            s.setPasswordAuthenticator(passwordAuthenticator);
        }

        CoreModuleProperties.IDLE_TIMEOUT.set(s, config.idleTimeout());
        
        try {
            s.start();
            server = s;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to start SSH shell server", e);
        }
    }
    
    public synchronized void stop() {
        if (server == null) {
            return;
        }
        try {
            server.stop(true);
        } catch (IOException e) {
            log.warn("Error while stopping SSH shell server", e);
        } finally {
            server = null;
            log.info("SSH shell server stopped");
        }
    }
    
    public synchronized boolean isRunning() {
        return server != null;
    }
}
