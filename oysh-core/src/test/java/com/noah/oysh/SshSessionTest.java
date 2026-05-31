package com.noah.oysh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class SshSessionTest {
    
    @AfterEach
    void clearBinding() {
        SshSession.unbind();
    }
    
    @Test
    void exposesPrincipalAndRoles() {
        final String principal = "goose";
        final Set<String> roles = Set.of("ADMIN", "SUPER");
        SshSession session = new SshSession(principal, roles, null);
        
        assertEquals(
              principal, session.getPrincipal(),
              "SshSession should return principal: " + principal);
        assertTrue(
              session.getRoles().containsAll(roles),
              "SshSession should return all the roles: " + roles);
    }
    
    @Test
    void rolesAreImmutable() {
        SshSession session = new SshSession("goose", Set.of("ADMIN"), null);
        
        assertThrows(
              UnsupportedOperationException.class, () -> session.getRoles().add("NEW1"),
              "Shouldn't be able to add roles via the SshSession");
    }

    @Test
    void hasAnyRoleMatchesWhenAnyRequiredRolePresent() {
        SshSession session = new SshSession("alice", Set.of("ADMIN"), null);
        
        assertTrue(session.hasAnyRole("ADMIN"));
        assertTrue(session.hasAnyRole("ADMIN", "SUPER"));
        assertFalse(session.hasAnyRole("SUPER"));
    }

    @Test
    void hasAnyRoleIsTrueWhenNoRolesRequired() {
        SshSession session = new SshSession("alice", Set.of(), null);

        // No required roles == "available to any authenticated user".
        assertTrue(session.hasAnyRole());
    }

    @Test
    void currentReturnsBoundSession() {
        SshSession session = new SshSession("bob", Set.of(), null);
        SshSession.bind(session);

        assertEquals(SshSession.current(), session);
    }

    @Test
    void currentThrowsWhenNoSessionBound() {
        assertThrows(
              IllegalStateException.class, SshSession::current);
    }

    @Test
    void bindingIsIsolatedPerThread() throws Exception {
        SshSession main = new SshSession("main", Set.of(), null);
        SshSession.bind(main);

        // A different thread must not see this thread's session.
        Throwable[] caught = new Throwable[1];
        Thread other = new Thread(() -> {
            try {
                assertThrows(
                      IllegalStateException.class, SshSession::current);
            } catch (Throwable t) {
                caught[0] = t;
            }
        });
        other.start();
        other.join();

        assertNull(caught[0]);
        // This thread still sees its own binding.
        assertEquals(SshSession.current(), main);
    }

}
