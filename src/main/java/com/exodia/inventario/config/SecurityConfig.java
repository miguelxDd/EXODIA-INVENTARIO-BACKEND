package com.exodia.inventario.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

/**
 * Configuracion de seguridad para desarrollo.
 *
 * ADVERTENCIA: Esta configuracion permite acceso sin autenticacion.
 * X-Empresa-Id es confiable SOLO si un API Gateway / JWT filter upstream
 * lo inyecta tras autenticar al usuario. En produccion se debe:
 *
 * 1. Crear un SecurityConfig para @Profile("prod") con JWT validation.
 * 2. Extraer empresaId del claim del token, NO del header del cliente.
 * 3. Restringir CORS a dominios conocidos.
 * 4. Habilitar CSRF si hay sesiones (o mantener deshabilitado con JWT stateless).
 */
@Configuration
@EnableWebSecurity
@Profile("!prod")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    var config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("*"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    return config;
                }))
                .headers(headers -> headers
                        .contentTypeOptions(ct -> {})
                        .frameOptions(fo -> fo.deny())
                        .referrerPolicy(rp -> rp.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/api-docs/**", "/actuator/health").permitAll()
                        .anyRequest().permitAll());

        return http.build();
    }
}
