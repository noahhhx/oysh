package io.github.noahhhx.oysh.boot;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalised configuration, bound from the {@code oysh.*} namespace.
 */
@ConfigurationProperties("oysh")
public class OyshProperties {

    /** Master switch. Server only starts when true. **/
    private boolean enabled = false;
    
    /** Interface to bind. Default is loopback only - don't expose to the world. **/
    private String host = "127.0.0.1";
    
    /** Listen port. **/
    private int port = 2222;
    
    /** Where the server's persistent host key is stored (generated on the first run). **/
    private String hostKeyPath = "hostkey.ser";
    
    /** Disconnect idle sessions after this duration. **/
    private Duration idleTimeout = Duration.ofMinutes(10);
    
    /** Interactive prompt string. **/
    private String prompt = "oysh> ";
    
    private final Auth auth = new Auth();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHostKeyPath() {
        return hostKeyPath;
    }

    public void setHostKeyPath(String hostKeyPath) {
        this.hostKeyPath = hostKeyPath;
    }

    public Duration getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(Duration idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Auth getAuth() {
        return auth;
    }

    public static class Auth {

        /**
         * Path to an OpenSSH {@code authorized_keys} file. When set (and no custom authenticator)
         * bean is defined, public-key auth is enabled against this file.
         */
        private String authorizedKeysPath = "authorized_keys";
        
        public String getAuthorizedKeysPath() {
            return authorizedKeysPath;
        }
        
        public void setAuthorizedKeysPath(String authorizedKeysPath) {
            this.authorizedKeysPath = authorizedKeysPath;
        }
    }
}
