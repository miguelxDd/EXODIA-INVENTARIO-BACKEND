package com.exodia.inventario.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

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
            if (principal instanceof Jwt jwt) {
                return extraerUserId(jwt);
            }
            if (auth instanceof JwtAuthenticationToken jwtAuthenticationToken) {
                return extraerUserId(jwtAuthenticationToken.getToken());
            }
            return Optional.of(SYSTEM_USER_ID);
        };
    }

    private Optional<Long> extraerUserId(Jwt jwt) {
        Object userId = jwt.getClaims().get("user_id");
        if (userId == null) {
            userId = jwt.getClaims().get("uid");
        }
        if (userId == null) {
            userId = jwt.getSubject();
        }
        try {
            return Optional.of(Long.parseLong(String.valueOf(userId)));
        } catch (Exception ex) {
            return Optional.of(SYSTEM_USER_ID);
        }
    }
}
