package io.github.noahhhx.oysh.boot;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import picocli.CommandLine;

/**
 * Lets picocli resolve command objects from the Spring context, falling back to 
 * picocli's default factory plus autowiring when the type is not a registered bean. 
 * Keeps DI working inside commands without pulling in the picocli-spring
 * starter and its auto-run behaviour.
 */
public class SpringIFactory implements CommandLine.IFactory {

    private final AutowireCapableBeanFactory beanFactory;
    private final CommandLine.IFactory fallback = CommandLine.defaultFactory();

    public SpringIFactory(AutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public <K> K create(Class<K> cls) throws Exception {
        try {
            return beanFactory.getBean(cls);
        } catch (NoSuchBeanDefinitionException e) {
            K instance = fallback.create(cls);
            beanFactory.autowireBean(instance);
            return instance;
        }
    }
}
