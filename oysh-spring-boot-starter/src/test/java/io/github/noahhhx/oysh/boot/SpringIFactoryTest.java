package io.github.noahhhx.oysh.boot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

class SpringIFactoryTest {

    public static class KnownBean {
    }

    public static class UnknownBean {
    }

    @Test
    void knownBeanReturnsFromFactory() throws Exception {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        KnownBean registered = new KnownBean();
        bf.registerSingleton("knownBean", registered);

        SpringIFactory factory = new SpringIFactory(bf);
        KnownBean result = factory.create(KnownBean.class);

        assertEquals(registered, result);
    }

    @Test
    void unknownBeanFallsBackToPicocliAndAutowires() throws Exception {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        SpringIFactory factory = new SpringIFactory(bf);

        UnknownBean result = factory.create(UnknownBean.class);

        assertNotNull(result);
    }

    @Test
    void unknownBeanThrowsNoSuchBeanDefinitionInFactory() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        assertThrows(NoSuchBeanDefinitionException.class,
              () -> bf.getBean(UnknownBean.class));
    }
}
