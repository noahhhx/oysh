package com.noah.oysh;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.time.Duration;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests for {@link SshShellServer} construction and start/stop lifecycle. */
class SshShellServerTest {

    private static SshShellConfig config(int port, Path dir) {
        return new SshShellConfig("127.0.0.1", port, dir.resolve("hostkey.ser"),
              Duration.ofMinutes(1));
    }

    private static SshShellFactory noopFactory() {
        return new SshShellFactory(session -> { }, RoleResolver.empty());
    }

    private static PasswordAuthenticator acceptAll() {
        return (user, pass, session) -> true;
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    @Test
    void constructorRejectsMissingAuthenticators(@TempDir Path dir) throws IOException {
        assertThrows(IllegalStateException.class, () ->
              new SshShellServer(config(freePort(), dir), noopFactory(), null, null));
    }

    @Test
    void startThenStopTogglesRunningState(@TempDir Path dir) throws IOException {
        SshShellServer server = new SshShellServer(
              config(freePort(), dir), noopFactory(), null, acceptAll());
        
        assertFalse(server.isRunning());

        server.start();
        try {
            assertTrue(server.isRunning());
        } finally {
            server.stop();
        }
        assertFalse(server.isRunning());
    }

    @Test
    void startIsIdempotentAndStopIsSafeToRepeat(@TempDir Path dir) throws IOException {
        SshShellServer server = new SshShellServer(
              config(freePort(), dir), noopFactory(), null, acceptAll());

        server.start();
        server.start(); // second start is a no-op, must not throw
        assertTrue(server.isRunning());

        server.stop();
        server.stop(); // second stop is a no-op, must not throw
        assertFalse(server.isRunning());
    }
}
