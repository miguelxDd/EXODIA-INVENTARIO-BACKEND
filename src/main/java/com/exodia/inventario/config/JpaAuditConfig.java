package com.exodia.inventario.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditConfig {

    private static final Long SYSTEM_USER_ID = 0L;

    /**
     * Intenta obtener el ID del usuario autenticado desde el SecurityContext.
     * Si no hay autenticacion (dev sin JWT), retorna SYSTEM_USER_ID (0).
     * Cuando se integre JWT, el principal/claim debe exponer el userId como Long.
     */
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()
                    || "anonymousUser".equals(auth.getPrincipal())) {
                return Optional.of(SYSTEM_USER_ID);
            }
            Object principal = auth.getPrincipal();
            if (principal instanceof Long userId) {
                return Optional.of(userId);
            }
            return Optional.of(SYSTEM_USER_ID);
        };
    }
}
