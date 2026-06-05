package io.github.noahhhx.oysh.boot;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.noahhhx.oysh.RoleResolver;
import io.github.noahhhx.oysh.ShellRunner;
import io.github.noahhhx.oysh.SshShellConfig;
import io.github.noahhhx.oysh.SshShellFactory;
import io.github.noahhhx.oysh.SshShellServer;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.SmartLifecycle;
import picocli.CommandLine;

class OyshAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(OyshAutoConfiguration.class))
          .withPropertyValues("oysh.enabled=true");

    @Nested
    class Baseline {

        @Test
        void disabledByDefault() {
            new ApplicationContextRunner()
                  .withConfiguration(AutoConfigurations.of(OyshAutoConfiguration.class))
                  .run(context -> {
                      assertThat(context).doesNotHaveBean(SshShellServer.class);
                  });
        }

        @Test
        void enabledCreatesAllDefaultBeans() {
            runner.run(context -> {
                assertThat(context).hasSingleBean(RoleResolver.class);
                assertThat(context).hasSingleBean(SshCommandRegistry.class);
                assertThat(context).hasSingleBean(CommandLine.IFactory.class);
                assertThat(context).hasSingleBean(ShellRunner.class);
                assertThat(context).hasSingleBean(SshShellFactory.class);
                assertThat(context).hasSingleBean(PublickeyAuthenticator.class);
                assertThat(context).hasSingleBean(SshShellServer.class);
                assertThat(context).hasSingleBean(SmartLifecycle.class);
            });
        }

        @Test
        void ssdhAbsentSkipsAutoconfig() {
            runner.withClassLoader(new FilteredClassLoader(SshServer.class))
                  .run(context -> {
                      assertThat(context).doesNotHaveBean(SshShellServer.class);
                      assertThat(context).doesNotHaveBean(ShellRunner.class);
                  });
        }
    }

    @Nested
    class BeanOverrides {

        @Test
        void customRoleResolverReplacesDefault() {
            RoleResolver custom = principal -> Set.of("CUSTOM");
            runner.withBean(RoleResolver.class, () -> custom)
                  .run(context -> {
                      assertThat(context).hasSingleBean(RoleResolver.class);
                      RoleResolver bean = context.getBean(RoleResolver.class);
                      assertThat(bean.rolesForPrincipal("any")).containsExactly("CUSTOM");
                  });
        }

        @Test
        void customShellRunnerReplacesDefault() {
            ShellRunner custom = session -> {};
            runner.withBean(ShellRunner.class, () -> custom)
                  .run(context -> {
                      assertThat(context).hasSingleBean(ShellRunner.class);
                      ShellRunner bean = context.getBean(ShellRunner.class);
                      assertThat(bean).isSameAs(custom);
                  });
        }

        @Test
        void customPublickeyAuthenticatorSuppressesDefault() {
            PublickeyAuthenticator custom = (username, key, session) -> true;
            runner.withBean(PublickeyAuthenticator.class, () -> custom)
                  .run(context -> {
                      assertThat(context).hasSingleBean(PublickeyAuthenticator.class);
                      assertThat(context.getBean(PublickeyAuthenticator.class))
                            .isSameAs(custom);
                  });
        }

        @Test
        void customPasswordAuthenticatorSuppressesDefaultPublickey() {
            PasswordAuthenticator custom = (username, password, session) -> true;
            runner.withBean(PasswordAuthenticator.class, () -> custom)
                  .run(context -> {
                      assertThat(context).hasSingleBean(PasswordAuthenticator.class);
                      assertThat(context).doesNotHaveBean(PublickeyAuthenticator.class);
                  });
        }

        @Test
        void customSshShellFactorySuppressesDefault() {
            SshShellFactory custom = new SshShellFactory(
                  session -> {}, RoleResolver.empty());
            runner.withBean(SshShellFactory.class, () -> custom)
                  .run(context -> {
                      assertThat(context).hasSingleBean(SshShellFactory.class);
                      assertThat(context.getBean(SshShellFactory.class)).isSameAs(custom);
                  });
        }

        @Test
        void customSshShellServerSuppressesDefault(@TempDir Path dir) throws IOException {
            int port = freePort();
            SshShellConfig config = new SshShellConfig(
                  "127.0.0.1", port, dir.resolve("hostkey.ser"), Duration.ofMinutes(1));
            PasswordAuthenticator auth = (u, p, s) -> true;
            SshShellFactory factory = new SshShellFactory(s -> {}, RoleResolver.empty());
            SshShellServer custom = new SshShellServer(config, factory, null, auth);

            runner.withBean(SshShellServer.class, () -> custom)
                  .run(context -> {
                      assertThat(context).hasSingleBean(SshShellServer.class);
                      assertThat(context.getBean(SshShellServer.class)).isSameAs(custom);
                  });
        }

        @Test
        void customCommandLineFactorySuppressesDefault() {
            CommandLine.IFactory custom = CommandLine.defaultFactory();
            runner.withBean(CommandLine.IFactory.class, () -> custom)
                  .run(context -> {
                      assertThat(context).hasSingleBean(CommandLine.IFactory.class);
                      assertThat(context.getBean(CommandLine.IFactory.class)).isSameAs(custom);
                  });
        }
    }

    @Nested
    class ShellServerLifecycle {

        @Test
        void startDelegates(@TempDir Path dir) throws IOException {
            SshShellServer server = createServer(dir);
            OyshAutoConfiguration.SshShellServerLifecycle lifecycle =
                  new OyshAutoConfiguration.SshShellServerLifecycle(server);

            lifecycle.start();
            try {
                assertThat(server.isRunning()).isTrue();
            } finally {
                server.stop();
            }
        }

        @Test
        void stopDelegates(@TempDir Path dir) throws IOException {
            SshShellServer server = createServer(dir);
            server.start();
            OyshAutoConfiguration.SshShellServerLifecycle lifecycle =
                  new OyshAutoConfiguration.SshShellServerLifecycle(server);

            lifecycle.stop();
            assertThat(server.isRunning()).isFalse();
        }

        @Test
        void isRunningDelegates(@TempDir Path dir) throws IOException {
            SshShellServer server = createServer(dir);
            OyshAutoConfiguration.SshShellServerLifecycle lifecycle =
                  new OyshAutoConfiguration.SshShellServerLifecycle(server);

            assertThat(lifecycle.isRunning()).isFalse();

            server.start();
            try {
                assertThat(lifecycle.isRunning()).isTrue();
            } finally {
                server.stop();
            }
            assertThat(lifecycle.isRunning()).isFalse();
        }

        @Test
        void getPhaseReturnsExpectedValue() {
            OyshAutoConfiguration.SshShellServerLifecycle lifecycle =
                  new OyshAutoConfiguration.SshShellServerLifecycle(null);
            assertThat(lifecycle.getPhase()).isEqualTo(Integer.MAX_VALUE - 1000);
        }

        private SshShellServer createServer(Path dir) throws IOException {
            int port = freePort();
            SshShellConfig config = new SshShellConfig(
                  "127.0.0.1", port, dir.resolve("hostkey.ser"), Duration.ofMinutes(1));
            PasswordAuthenticator auth = (u, p, s) -> true;
            SshShellFactory factory = new SshShellFactory(s -> {}, RoleResolver.empty());
            return new SshShellServer(config, factory, null, auth);
        }
    }

    @Nested
    class DefaultPublickeyAuthenticator {

        @Test
        void createdWhenNoAuthenticatorsDefined() {
            runner.run(context -> {
                assertThat(context).hasSingleBean(PublickeyAuthenticator.class);
                assertThat(context).doesNotHaveBean(PasswordAuthenticator.class);
            });
        }

        @Test
        void usesConfiguredAuthorizedKeysPath() {
            runner.withPropertyValues(
                      "oysh.auth.authorized-keys-path=./custom-keys")
                  .run(context -> {
                      PublickeyAuthenticator bean =
                            context.getBean(PublickeyAuthenticator.class);
                      assertThat(bean.getClass().getName())
                            .contains("AuthorizedKeysAuthenticator");
                  });
        }
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
