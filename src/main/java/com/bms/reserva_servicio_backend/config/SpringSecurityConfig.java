package com.bms.reserva_servicio_backend.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.bms.reserva_servicio_backend.security.filter.JwtAuthenticationFilter;
import com.bms.reserva_servicio_backend.security.filter.JwtValidationFilter;
import com.bms.reserva_servicio_backend.service.UserService;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SpringSecurityConfig {

    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    private final UserService userService;

    public SpringSecurityConfig(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Bean
    AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .authorizeHttpRequests(auth -> auth
                        // ============================================
                        // ENDPOINTS PÚBLICOS (Sin autenticación)
                        // ============================================
                        
                        // Usuarios - Registro y login
                        .requestMatchers(HttpMethod.GET, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                        
                        
                        // Disponibilidad - Consulta pública (para que clientes vean disponibilidad)
                        .requestMatchers(HttpMethod.GET, "/api/disponibilidad/**").permitAll()
                        
                        // Inventario - Solo lectura pública (ver qué items hay disponibles)
                        .requestMatchers(HttpMethod.GET, "/api/inventario/recurso/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/inventario/*/disponibilidad").permitAll()

                        // Catálogo público - Ver cabañas y servicios (sin autenticación)
                        .requestMatchers(HttpMethod.GET, "/api/cabanas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/cabanas/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/servicios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/servicios/*").permitAll()

                        // Imágenes - Acceso público para ver imágenes cargadas
                        .requestMatchers("/uploads/**").permitAll()

                        // ============================================
                        // CLIENTES - Requiere autenticación
                        // ============================================
                        
                        
                        // Solo ADMIN puede promover a VIP
                        .requestMatchers(HttpMethod.PUT, "/api/clientes/*/vip").hasRole("ADMIN")
                        
                        // ============================================
                        // RESERVAS - Según rol
                        // ============================================
                        
                        // Crear reservas - Cualquier usuario autenticado
                        .requestMatchers(HttpMethod.POST, "/api/reservas/cabana").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/reservas/servicio").authenticated()
                        
                        // Ver reservas propias - Usuario autenticado
                        .requestMatchers(HttpMethod.GET, "/api/reservas/cliente/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/reservas/*").authenticated()
                        
                        // Confirmar y cancelar - Usuario autenticado (solo sus propias reservas)
                        .requestMatchers(HttpMethod.PUT, "/api/reservas/*/confirmar").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/reservas/*").authenticated()
                        
                        // Ver TODAS las reservas - Solo ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/reservas").hasRole("ADMIN")
                        
                        // ============================================
                        // PAQUETES - Según rol
                        // ============================================
                        
                        // Crear paquetes - Usuario autenticado
                        .requestMatchers(HttpMethod.POST, "/api/paquetes").authenticated()
                        
                        // Ver, confirmar y cancelar paquetes - Usuario autenticado
                        .requestMatchers(HttpMethod.GET, "/api/paquetes/*").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/paquetes/*/confirmar").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/paquetes/*").authenticated()
                        
                        // Modificar paquetes - Usuario autenticado
                        .requestMatchers(HttpMethod.PUT, "/api/paquetes/*").authenticated()
                        
                        // Ver TODOS los paquetes - Solo ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/paquetes").hasRole("ADMIN")
                        
                        // ============================================
                        // INVENTARIO - Solo ADMIN
                        // ============================================
                        
                        // Agregar, modificar y eliminar items - Solo ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/inventario").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/inventario/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/inventario/*").hasRole("ADMIN")
                        
                        // ============================================
                        // RECURSOS (Cabañas y Servicios) - Solo ADMIN
                        // ============================================
                        
                        // CRUD completo de recursos - Solo ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/recursos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/recursos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/recursos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/recursos/**").hasRole("ADMIN")
                        
                        // ============================================
                        // REPORTES Y ESTADÍSTICAS - Solo ADMIN
                        // ============================================
                        
                        .requestMatchers(HttpMethod.GET, "/api/reportes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/estadisticas/**").hasRole("ADMIN")
                        
                        // ============================================
                        // HEALTH CHECK Y ACTUATOR (Opcional)
                        // ============================================
                        
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        
                        // ============================================
                        // TODO LO DEMÁS - Requiere autenticación
                        // ============================================
                        
                        .anyRequest().authenticated()
                )
                .addFilter(new JwtAuthenticationFilter(authenticationManager(), userService))
                .addFilter(new JwtValidationFilter(authenticationManager()))
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Orígenes permitidos (ajustar según tu frontend)
        config.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // Métodos HTTP permitidos
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Headers permitidos
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        
        // Headers expuestos (que el frontend puede leer)
        config.setExposedHeaders(Arrays.asList("Authorization"));
        
        // Permitir credenciales
        config.setAllowCredentials(true);
        
        // Tiempo de caché para preflight requests
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    FilterRegistrationBean<CorsFilter> corsFilter() {
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(
            new CorsFilter(corsConfigurationSource())
        );
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}