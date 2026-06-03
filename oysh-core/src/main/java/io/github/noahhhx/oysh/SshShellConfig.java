package io.github.noahhhx.oysh;

import java.nio.file.Path;
import java.time.Duration;

/**
 * Transport configuration consumed by {@link SshShellServer}. The start builds this
 * from {@code ssh-shell.*} properties, but the core modules stay free from Spring.
 * 
 * @param host
 * @param port
 * @param hostKeyPath
 * @param idleTimeout
 */
public record SshShellConfig(
   String host,
   int port,
   Path hostKeyPath,
   Duration idleTimeout
) {
}
