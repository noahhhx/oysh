package io.github.noahhhx.oysh.boot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = OyshPropertiesTest.Config.class)
@EnableConfigurationProperties(OyshProperties.class)
public class OyshPropertiesTest {

    @Configuration
    static class Config {
    }

    @Autowired
    private OyshProperties oyshProperties;

    @Test
    void defaultsAreRespected() {
        assertFalse(oyshProperties.isEnabled());
        assertEquals("127.0.0.1", oyshProperties.getHost());
        assertEquals(2222, oyshProperties.getPort());
        assertEquals("hostkey.ser", oyshProperties.getHostKeyPath());
        assertEquals(Duration.ofMinutes(10), oyshProperties.getIdleTimeout());
        assertEquals("oysh> ", oyshProperties.getPrompt());
        assertEquals("authorized_keys", oyshProperties.getAuth().getAuthorizedKeysPath());
    }

    @Nested
    @TestPropertySource("test-props.properties")
    class PropertiesFileTest {
        @Autowired
        private OyshProperties nestedOyshProperties;

        @Test
        void bindingFromPropertiesFileIsRespected() {
            assertTrue(nestedOyshProperties.isEnabled());
            assertEquals("127.1.1.0", nestedOyshProperties.getHost());
            assertEquals(2223, nestedOyshProperties.getPort());
            assertEquals("test-hostkey.ser", nestedOyshProperties.getHostKeyPath());
            assertEquals(Duration.ofMinutes(5), nestedOyshProperties.getIdleTimeout());
            assertEquals("\"demo-oysh> \"", nestedOyshProperties.getPrompt());
            assertEquals("./authorized-keys-test", nestedOyshProperties.getAuth()
                  .getAuthorizedKeysPath());
        }
    }
}
