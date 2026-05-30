package com.noah.oysh;

import java.io.PrintWriter;
import java.util.Set;
import org.jline.terminal.Terminal;

/**
 * Represents a single live SSH shell session.
 * 
 * <p>Because Apache MINA SSHD runs each interactive channel on its own thread, the
 * "current" session is bound to a {@link ThreadLocal}. Command code can therefore call
 * {@link #current()} from anywhere on the session thread to find out who is connected and
 * which terminal to write to - that is the whole of the per-session scoping mechanism.
 */
public final class SshSession {

    private static final ThreadLocal<SshSession> CURRENT = new ThreadLocal<>();
    
    private final String principal;
    private final Set<String> roles;
    private final Terminal terminal;
    
    public SshSession(String principal, Set<String> roles, Terminal terminal) {
        this.principal = principal;
        this.roles = Set.copyOf(roles);
        this.terminal = terminal;
    }
    
    /** The authenticated user name for this session. */
    public String getPrincipal() {
        return principal;
    }
    
    /** Roles granted to the principal */
    public Set<String> getRoles() {
        return roles;
    }
    
    public boolean hasAnyRole(String... required) {
        if (required == null || required.length == 0) {
            return true;
        }
        for (String req : required) {
            if (roles.contains(req)) {
                return true;
            }
        }
        return false;
    }
    
    /** The JLine terminal bound to this SSH channel */
    public Terminal getTerminal() {
        return terminal;
    }
    
    public PrintWriter getWriter() {
        return terminal.writer();
    }
    
    public void writeLine(String line) {
        PrintWriter w = terminal.writer();
        w.println(line);
        w.flush();
    }
    
    /** @return the session bound to the current thread, or throws if none. */
    public static SshSession current() {
        SshSession session = CURRENT.get();
        if (session == null) {
            throw new IllegalStateException("No SSH session is bound to this thread");
        }
        return session;
    }
    
    static void bind(SshSession session) {
        CURRENT.set(session);
    }
    
    static void unbind() {
        CURRENT.remove();
    }
}
