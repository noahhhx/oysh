package io.github.noahhhx.oysh.boot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.noahhhx.oysh.SshShellServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import picocli.CommandLine.Command;

class SshCommandRegistryTest {

    // ---- Config & command classes for each nested test ----

    @Configuration
    @Import(SshCommandRegistryTest.SingleCommand.class)
    static class SingleCommandConfig {
    }

    @Configuration
    @Import({CommandAdminA.class, CommandAdminB.class})
    static class ManyCommandsConfig {
    }

    @Configuration
    @Import({OpenCommand.class, AdminCmd.class})
    static class MixedConfig {
    }

    @Configuration
    static class EmptyConfig {
    }

    @SshCommand
    @Command(name = "my-command", description = "")
    static class SingleCommand implements Runnable {
        @Override
        public void run() {
        }
    }

    @SshCommand(roles = {"ADMIN"})
    @Command(name = "my-command", description = "")
    static class CommandAdminA implements Runnable {
        @Override
        public void run() {
        }
    }

    @SshCommand(roles = {"ADMIN"})
    @Command(name = "my-command1", description = "")
    static class CommandAdminB implements Runnable {
        @Override
        public void run() {
        }
    }

    @SshCommand
    @Command(name = "open-cmd", description = "")
    static class OpenCommand implements Runnable {
        @Override
        public void run() {
        }
    }

    @SshCommand(roles = {"ADMIN"})
    @Command(name = "admin-cmd", description = "")
    static class AdminCmd implements Runnable {
        @Override
        public void run() {
        }
    }

    // ---- Nested tests ----

    @Nested
    @SpringBootTest(
          classes = {OyshAutoConfiguration.class, SingleCommandConfig.class},
          properties = "oysh.enabled=true")
    class SingleCommandNoRoles {

        @Autowired
        private SshCommandRegistry registry;

        @Autowired
        private SshShellServer server;

        @AfterEach
        void tearDown() {
            server.stop();
        }

        @Test
        void singleCommandNoRoles() {
            assertEquals(1, registry.getEntries().size());
            SshCommandRegistry.Entry entry = registry.getEntries().get(0);
            assertEquals(0, entry.roles().length);
        }
    }

    @Nested
    @SpringBootTest(
          classes = {OyshAutoConfiguration.class, ManyCommandsConfig.class},
          properties = "oysh.enabled=true")
    class ManyCommandWithRoles {

        @Autowired
        private SshCommandRegistry registry;

        @Autowired
        private SshShellServer server;

        @AfterEach
        void tearDown() {
            server.stop();
        }

        @Test
        void multipleCommandsWithRoles() {
            assertEquals(2, registry.getEntries().size());
            registry.getEntries().forEach(entry -> {
                assertTrue(entry.roles().length > 0);
                assertEquals("ADMIN", entry.roles()[0]);
            });
        }
    }

    @Nested
    @SpringBootTest(
          classes = {OyshAutoConfiguration.class, MixedConfig.class},
          properties = "oysh.enabled=true")
    class MixedRoleAndRoleless {

        @Autowired
        private SshCommandRegistry registry;

        @Autowired
        private SshShellServer server;

        @AfterEach
        void tearDown() {
            server.stop();
        }

        @Test
        void rolelessAndRoleGatedCommandsCoexist() {
            assertEquals(2, registry.getEntries().size());

            boolean foundRoleless = false;
            boolean foundRoleGated = false;
            for (SshCommandRegistry.Entry entry : registry.getEntries()) {
                if (entry.roles().length == 0) {
                    foundRoleless = true;
                } else if (entry.roles()[0].equals("ADMIN")) {
                    foundRoleGated = true;
                }
            }
            assertTrue(foundRoleless, "should have a role-less command");
            assertTrue(foundRoleGated, "should have a role-gated command");
        }
    }

    @Nested
    @SpringBootTest(
          classes = {OyshAutoConfiguration.class, EmptyConfig.class},
          properties = "oysh.enabled=true")
    class EmptyRegistry {

        @Autowired
        private SshCommandRegistry registry;

        @Autowired
        private SshShellServer server;

        @AfterEach
        void tearDown() {
            server.stop();
        }

        @Test
        void noAnnotatedBeansReturnsEmptyEntries() {
            assertTrue(registry.getEntries().isEmpty());
        }
    }
}
