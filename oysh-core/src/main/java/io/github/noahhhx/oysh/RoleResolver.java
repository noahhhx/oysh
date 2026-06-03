package io.github.noahhhx.oysh;

import java.util.Set;

/**
 * Resolves the set of roles granted to an authenticated principal. Used to drive
 * per-command authorization. The default implementation grants no roles; supply your own
 * bean (backed by LDAP, a user, etc.) to enable role-gated commands.
 */
@FunctionalInterface
public interface RoleResolver {

    Set<String> rolesForPrincipal(String principal);
    
    /** A resolves that grants no roles to anyone. */
    static RoleResolver empty() {
        return principal -> Set.of();
    }
}
