package io.github.noahhhx.oysh.boot;

import io.github.noahhhx.oysh.RoleResolver;
import java.util.Set;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Demo wiring of security extension points.
 * 
 * <p><b>DEMO ONLY - don't use this!</b> Password auth below accepts hard-coded credentials
 * so you can quickly connect to the demo app. Prefer to replace it with a 
 * {@code PublicKeyAuthenticator} or delegate to a real identify provider.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordAuthenticator demoPasswordAuthenticator() {
        return ((username, password, session) -> 
              "admin".equals(username) && "admin".equals(password));
    }
    
    @Bean
    public RoleResolver roleResolver() {
        return principal -> "admin".equals(principal) ? Set.of("ADMIN") : Set.of();
    }
}
