package io.github.noahhhx.oysh.boot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

/**
 * Kind of a completely pointless test, but it stops unintentional drift.
 */
class EnableOyshShellTest {

    @Test
    void hasTypeAndRetention() {
        Target target = EnableOyshShell.class.getAnnotation(Target.class);
        assertEquals(ElementType.TYPE, target.value()[0]);

        java.lang.annotation.Retention retention =
              EnableOyshShell.class.getAnnotation(java.lang.annotation.Retention.class);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void importsOyshAutoConfiguration() {
        Import imp = EnableOyshShell.class.getAnnotation(Import.class);
        assertEquals(1, imp.value().length);
        assertEquals(OyshAutoConfiguration.class, imp.value()[0]);
    }
}
