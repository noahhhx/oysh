package io.github.noahhhx.oysh.boot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * Marks a picocli {@code @Command} class as an SSH shell command. The class becomes a Spring
 * bean (this is meta-annotated with {@link Component}) and is auto-registered as a subcommand
 * of the shell.
 *
 * <pre>{@code
 * @SshCommand(roles = "ADMIN")
 * @Command(name = "admin-operation", description = "Carry out admin operation.")
 * public class AdminOperationCommand implements Runnable { ... }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface SshCommand {

    /**
     * Roles required to see and run this command. Empty (default) means available to every
     * authenticated user. Role membership is resolved by the active
     * {@link io.github.noahhhx.oysh.RoleResolver} bean.
     */
    String[] roles() default {};

}
