package io.github.noahhhx.oysh.boot;

import java.time.Instant;

/**
 * Published after each command line is executed in an SSH session. Listen for this in the
 * consuming program to build an audit trail without coupling to shell internals.
 */
public record SshCommandExecutedEvent(
      String principal,
      String commandLine,
      Instant at) {
}
