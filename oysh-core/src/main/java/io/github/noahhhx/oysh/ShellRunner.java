package io.github.noahhhx.oysh;

/**
 * SPI for the interactive loop that drives a single session.
 */
@FunctionalInterface
public interface ShellRunner {

    /**
     * Run the interactive read-eval-print loop for one session. Returning from this
     * method (e.g., Ctrl-D) ends the SSH session.
     * 
     * @param session
     * @throws Exception
     */
    void run(SshSession session) throws Exception;
}
