package io.github.noahhhx.oysh.boot;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.noahhhx.oysh.RoleResolver;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import picocli.CommandLine.Command;

@SpringBootTest(
      classes = {IntegrationCustomBeansConfig.class, IntegrationMainConfig.class},
      webEnvironment = WebEnvironment.NONE)
class OyshSpringBootIntegrationTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static final int PORT = allocatePort();

    private SshClient client;

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("oysh.port", () -> PORT);
        registry.add("oysh.enabled", () -> "true");
    }

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.stop();
        }
        IntegrationEventCollector.receivedEvents.clear();
    }

    @Test
    void clientConnectsAndSeesWelcomeBanner() throws Exception {
        String output = connectAndReadOutput("test", "secret");
        assertTrue(output.contains("Connected as 'test'"));
    }

    @Test
    void customCommandIsAvailableAndPublishesEvent() throws Exception {
        String output = connectAndSendCommand("test", "secret", "greet\n");

        assertTrue(output.contains("Hello, test!"),
              "Expected greeting but got: " + output);

        assertFalse(IntegrationEventCollector.receivedEvents.isEmpty(),
              "should have published at least one event");
        assertTrue(IntegrationEventCollector.receivedEvents.stream()
              .anyMatch(e -> e.commandLine().equals("greet")),
              "should have published event for 'greet' command");
    }

    @Test
    void roleGatedCommandHiddenFromRegularUser() throws Exception {
        String output = connectAndSendCommand("guest", "secret", "secret-cmd\n");
        assertFalse(output.contains("Top secret"),
              "role-gated command should not execute for guest, but got: " + output);
    }

    @Test
    void roleGatedCommandVisibleToAdmin() throws Exception {
        String output = connectAndSendCommand("admin", "secret", "secret-cmd\n");
        assertTrue(output.contains("Top secret"),
              "role-gated command should execute for admin, but got: " + output);
    }

    @Test
    void helpCommandListsAvailableCommands() throws Exception {
        String output = connectAndSendCommand("test", "secret", "help\n");
        assertTrue(output.contains("greet"),
              "help should list 'greet' command, but got: " + output);
    }

    private String connectAndReadOutput(String user, String password) throws IOException {
        return connectAndSendCommand(user, password, null);
    }

    private String connectAndSendCommand(String user, String password, String command)
          throws IOException {
        client = SshClient.setUpDefaultClient();
        client.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);
        client.start();

        try (ClientSession session = client.connect(user, "127.0.0.1", PORT)
              .verify(TIMEOUT).getSession()) {
            session.addPasswordIdentity(password);
            session.auth().verify(TIMEOUT);

            try (ChannelShell channel = session.createShellChannel()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                channel.setOut(out);
                channel.setErr(new ByteArrayOutputStream());

                byte[] input = (command != null)
                      ? command.getBytes(StandardCharsets.UTF_8)
                      : new byte[0];
                channel.setIn(new ByteArrayInputStream(input));
                channel.open().verify(TIMEOUT);
                channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), TIMEOUT);

                return out.toString(StandardCharsets.UTF_8);
            }
        }
    }

    private static int allocatePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

@Configuration
class IntegrationCustomBeansConfig {

    @Bean
    PasswordAuthenticator testPasswordAuthenticator() {
        return (username, password, session) ->
              "secret".equals(password) &&
              ("test".equals(username) || "admin".equals(username) || "guest".equals(username));
    }

    @Bean
    RoleResolver testRoleResolver() {
        return principal -> {
            if ("admin".equals(principal)) {
                return Set.of("ADMIN");
            }
            return Set.of();
        };
    }
}

@Configuration
@EnableOyshShell
@Import({IntegrationGreetCommand.class, IntegrationSecretCommand.class,
      IntegrationEventCollector.class})
class IntegrationMainConfig {
}

@Component
class IntegrationEventCollector {

    static final List<SshCommandExecutedEvent> receivedEvents =
          Collections.synchronizedList(new ArrayList<>());

    @EventListener
    void onExecuted(SshCommandExecutedEvent event) {
        receivedEvents.add(event);
    }
}

@SshCommand
@Command(name = "greet", description = "Greet the user")
class IntegrationGreetCommand implements Runnable {
    @Override
    public void run() {
        io.github.noahhhx.oysh.SshSession.current().writeLine(
              "Hello, " + io.github.noahhhx.oysh.SshSession.current().getPrincipal() + "!");
    }
}

@SshCommand(roles = {"ADMIN"})
@Command(name = "secret-cmd", description = "Admin-only secret")
class IntegrationSecretCommand implements Runnable {
    @Override
    public void run() {
        io.github.noahhhx.oysh.SshSession.current().writeLine("Top secret");
    }
}
