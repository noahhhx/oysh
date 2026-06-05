package io.github.noahhhx.oysh.boot;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

class SshCommandTest {

    @Test
    void isMetaAnnotatedWithComponent() {
        assertTrue(SshCommand.class.isAnnotationPresent(Component.class));
    }

    @Test
    void rolesDefaultsToEmptyArray() throws NoSuchMethodException {
        String[] defaults = (String[]) SshCommand.class.getMethod("roles").getDefaultValue();
        assertEquals(0, defaults.length);
    }

    @Test
    void rolesAcceptsExplicitValues() {
        @SshCommand(roles = {"ADMIN", "OPS"})
        class ExplicitRoles {}
        SshCommand annotation = ExplicitRoles.class.getAnnotation(SshCommand.class);
        assertArrayEquals(new String[]{"ADMIN", "OPS"}, annotation.roles());
    }
}
