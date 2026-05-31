package com.noah.oysh;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class SshShellServerIntegrationTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private SshShellServer server;
    private SshClient client;

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.stop();
        }
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void clientCanConnectAndSeeSessionOutput(@TempDir Path dir) throws Exception {
        int port = freePort();

        AtomicReference<String> seenPrincipal = new AtomicReference<>();
        AtomicReference<Set<String>> seenRoles = new AtomicReference<>();
        CountDownLatch runnerInvoked = new CountDownLatch(1);

        // The runner under test: capture what the session reports, write a line, then return
        // (which ends the SSH session).
        ShellRunner runner = session -> {
            seenPrincipal.set(session.getPrincipal());
            seenRoles.set(session.getRoles());
            session.writeLine("OUTPUT principal=" + session.getPrincipal()
                  + " roles=" + session.getRoles());
            runnerInvoked.countDown();
        };

        RoleResolver roles = principal ->
              "test".equals(principal) ? Set.of("ADMIN") : Set.of();
        PasswordAuthenticator auth = (user, pass, s) ->
              "test".equals(user) && "secret".equals(pass);

        SshShellConfig config = new SshShellConfig(
              "127.0.0.1", port, dir.resolve("hostkey.ser"), Duration.ofMinutes(1));
        server = new SshShellServer(config, new SshShellFactory(runner, roles), null, auth);
        server.start();

        String output = runShellCommand(port, "test", "secret");

        assertTrue(runnerInvoked.await(10, TimeUnit.SECONDS),
              "ShellRunner should have been invoked");
        assertEquals("test", seenPrincipal.get());
        assertEquals(Set.of("ADMIN"), seenRoles.get());
        assertTrue(output.contains("OUTPUT principal=test roles=[ADMIN]"));
    }

    @Test
    void authenticationFailureIsRejected(@TempDir Path dir) throws Exception {
        int port = freePort();

        PasswordAuthenticator auth = (user, pass, s) ->
              "test".equals(user) && "secret".equals(pass);
        SshShellConfig config = new SshShellConfig(
              "127.0.0.1", port, dir.resolve("hostkey.ser"), Duration.ofMinutes(1));
        server = new SshShellServer(config,
              new SshShellFactory(s -> { }, RoleResolver.empty()), null, auth);
        server.start();

        client = SshClient.setUpDefaultClient();
        client.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);
        client.start();

        try (ClientSession session = client.connect("test", "127.0.0.1", port)
              .verify(TIMEOUT).getSession()) {
            session.addPasswordIdentity("wrong-password");
            // throws once no authentication method succeeds.
            assertThrows(IOException.class,
                  () -> session.auth().verify(TIMEOUT),
                  "auth with wrong password must fail");
        }
    }
    
    private String runShellCommand(int port, String user, String password) throws IOException {
        client = SshClient.setUpDefaultClient();
        client.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);
        client.start();

        try (ClientSession session = client.connect(user, "127.0.0.1", port)
              .verify(TIMEOUT).getSession()) {
            session.addPasswordIdentity(password);
            session.auth().verify(TIMEOUT);

            try (ChannelShell channel = session.createShellChannel()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                channel.setOut(out);
                channel.setErr(new ByteArrayOutputStream());
                channel.setIn(new ByteArrayInputStream(new byte[0])); // immediate EOF on stdin
                channel.open().verify(TIMEOUT);
                channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), TIMEOUT);
                return out.toString(StandardCharsets.UTF_8);
            }
        }
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
