package io.github.noahhhx.oysh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

public class RoleResolverTest {
    
    @Test
    void emptyResolverGrantsNoRoles() {
        RoleResolver resolver = RoleResolver.empty();
        
        assertTrue(
              resolver.rolesForPrincipal("anyone").isEmpty(),
              "Empty `RoleResolver` should be empty."
        );
    }
    
    @Test
    void customRoleResolverMapsPrincipalToRoles() {
        final String adminName = "admin";
        final String adminRole = "ADMIN";
        
        RoleResolver resolver = principal -> 
              adminName.equals(principal) ? Set.of(adminRole) : Set.of();
        
        assertTrue(
              resolver.rolesForPrincipal(adminName).contains(adminRole),
              "RoleResolver should contain roles for principal."
        );
        assertEquals(
              1, resolver.rolesForPrincipal(adminName).size(),
              "RoleResolver should only contain 1 role.s"
        );
        assertTrue(
              resolver.rolesForPrincipal("who are you").isEmpty(),
              "RoleResolver should be empty for unknown principal."
        );
    }

}
