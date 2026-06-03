package io.github.noahhhx.oysh.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * Demo usage of hooking into the oysh spring-boot event publishing.
 */
@Configuration
public class AuditConfig {
    
    private static final Logger auditLog = LoggerFactory.getLogger("OYSH_AUDIT");

    @EventListener
    public void onCommand(SshCommandExecutedEvent event) {
        auditLog.info("[{}] {} ran: {}", event.at(), event.principal(), event.commandLine());
    }
}
