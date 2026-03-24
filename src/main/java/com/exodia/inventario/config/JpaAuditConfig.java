package com.exodia.inventario.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditConfig {

    // TODO: Obtener el ID del usuario autenticado desde el JWT (SecurityContext)
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> Optional.of(1L);
    }
}
