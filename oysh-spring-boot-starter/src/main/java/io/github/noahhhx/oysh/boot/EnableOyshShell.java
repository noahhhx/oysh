package io.github.noahhhx.oysh.boot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * Opt-in switch that imports the Oysh autoconfiguration. Put this on any {@code Configuration} or
 * {@code SpringBootApplication} class.
 * 
 * <p>Opening a network port is something you usually want to make explicit, hence the 
 * {@code Enable} annotation. You still need {@code oysh.enable=true} in your properties fro the
 * server to actually start, so it can be toggled per env.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(OyshAutoConfiguration.class)
public @interface EnableOyshShell {

}
