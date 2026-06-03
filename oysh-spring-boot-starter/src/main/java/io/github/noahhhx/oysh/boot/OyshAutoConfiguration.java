package io.github.noahhhx.oysh.boot;

import io.github.noahhhx.oysh.RoleResolver;
import io.github.noahhhx.oysh.ShellRunner;
import io.github.noahhhx.oysh.SshShellConfig;
import io.github.noahhhx.oysh.SshShellFactory;
import io.github.noahhhx.oysh.SshShellServer;
import java.nio.file.Path;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.config.keys.AuthorizedKeysAuthenticator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import picocli.CommandLine;

/**
 * Wires the SSH shell into a Spring Boot application. Activated when {@code oysh.enable=true} and
 * Apache MINA SSHD is on the classpath.
 *
 * <p>Every bean is a {@link ConditionalOnMissingBean}, so a consuming project can override any
 * piece.
 */
@AutoConfiguration
@ConditionalOnClass(SshServer.class)
@ConditionalOnProperty(prefix = "oysh", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(OyshProperties.class)
public class OyshAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RoleResolver roleResolver() {
        return RoleResolver.empty();
    }

    @Bean
    @ConditionalOnMissingBean
    public SshCommandRegistry sshCommandRegistry(ApplicationContext context) {
        return new SshCommandRegistry(context);
    }

    @Bean
    @ConditionalOnMissingBean
    public CommandLine.IFactory sshPicocliFactory(AutowireCapableBeanFactory beanFactory) {
        return new SpringIFactory(beanFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public ShellRunner shellRunner(SshCommandRegistry commandRegistry,
          CommandLine.IFactory factory,
          ApplicationEventPublisher eventPublisher,
          OyshProperties oyshProperties) {
        return new PicocliShellRunner(commandRegistry, factory, eventPublisher,
              oyshProperties.getPrompt());
    }

    @Bean
    @ConditionalOnMissingBean
    public SshShellFactory sshShellFactory(ShellRunner shellRunner, RoleResolver roleResolver) {
        return new SshShellFactory(shellRunner, roleResolver);
    }

    /**
     * Default public-key authentication backed by an {@code authorized_keys} file. Only registered
     * when the application defines no authenticator of its own.
     */
    @Bean
    @ConditionalOnMissingBean({PublickeyAuthenticator.class, PasswordAuthenticator.class})
    public PublickeyAuthenticator defaultPublicKeyAuthenticator(OyshProperties oyshProperties) {
        return new AuthorizedKeysAuthenticator(
              Path.of(oyshProperties.getAuth().getAuthorizedKeysPath()));
    }

    @Bean
    @ConditionalOnMissingBean
    public SshShellServer sshShellServer(OyshProperties oyshProperties,
          SshShellFactory shellFactory,
          ObjectProvider<PublickeyAuthenticator> publicKeyAuth,
          ObjectProvider<PasswordAuthenticator> passwordAuth) {
        SshShellConfig config = new SshShellConfig(
              oyshProperties.getHost(),
              oyshProperties.getPort(),
              Path.of(oyshProperties.getHostKeyPath()),
              oyshProperties.getIdleTimeout()
        );
        return new SshShellServer(
              config,
              shellFactory,
              publicKeyAuth.getIfAvailable(),
              passwordAuth.getIfAvailable()
        );
    }

    @Bean
    public SmartLifecycle sshShellServerLifecycle(SshShellServer server) {
        return new SshShellServerLifecycle(server);
    }

    /**
     * Starts & stops the SSH Server with the application context.
     */
    static final class SshShellServerLifecycle implements SmartLifecycle {

        private final SshShellServer server;

        SshShellServerLifecycle(SshShellServer server) {
            this.server = server;
        }

        @Override
        public void start() {
            server.start();
        }

        @Override
        public void stop() {
            server.stop();
        }

        @Override
        public boolean isRunning() {
            return server.isRunning();
        }

        @Override
        public int getPhase() {
            // Start late relative to most beans.
            return Integer.MAX_VALUE - 1000;
        }
    }
}
