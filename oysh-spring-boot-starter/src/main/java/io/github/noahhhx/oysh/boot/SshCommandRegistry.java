package io.github.noahhhx.oysh.boot;

import java.util.ArrayList;
import java.util.List;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Discovers every {@link SshCommand}-annotated bean at startup and exposes them
 * so a fresh command tree can be built per session.
 */
public class SshCommandRegistry {

    private final List<Entry> entries = new ArrayList<>();
    
    public SshCommandRegistry(ApplicationContext context) {
        for (Object bean: context.getBeansWithAnnotation(SshCommand.class).values()) {
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            SshCommand annotation = AnnotationUtils.findAnnotation(targetClass, SshCommand.class);
            String[] roles = (annotation != null) ? annotation.roles() : new String[0];
            entries.add(new Entry(bean, roles));
        }
    }
    
    public List<Entry> getEntries() {
        return entries;
    }
    
    public record Entry(Object bean, String[] roles) {
    }
}
