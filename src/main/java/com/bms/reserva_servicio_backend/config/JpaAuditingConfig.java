package com.bms.reserva_servicio_backend.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Configuración de JPA Auditing
 * Habilita el tracking automático de cambios en entidades
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    /**
     * Proveedor del auditor actual (usuario autenticado)
     * Retorna el username del usuario actualmente autenticado
     * o "system" si no hay usuario autenticado
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }

    /**
     * Implementación del AuditorAware
     * Obtiene el usuario actual desde el contexto de seguridad
     */
    public static class AuditorAwareImpl implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system");
            }

            // Si el principal es "anonymousUser", retornar "system"
            if ("anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("system");
            }

            String username = authentication.getName();
            return Optional.of(username != null ? username : "system");
        }
    }
}
