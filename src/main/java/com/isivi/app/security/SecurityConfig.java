package com.isivi.app.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final RateLimitingFilter rateLimitingFilter;

    @Value("${isivi.cors.allowed-origins:https://isivi-app.onrender.com,http://localhost:8080,http://localhost:3000,http://127.0.0.1:5500,http://localhost:5500}")
    private String allowedOriginsConfig;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, RateLimitingFilter rateLimitingFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(Customizer.withDefaults())
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"error\":\"NO_AUTORIZADO\",\"mensaje\":\"Se requiere autenticación de administrador para acceder a este recurso.\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"error\":\"ACCESO_DENEGADO\",\"mensaje\":\"No tienes permisos suficientes para realizar esta acción.\"}");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        // Recursos estáticos y frontend público
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",
                                "/assets/**",
                                "/robots.txt",
                                "/sitemap.xml"
                        ).permitAll()

                        // Endpoints públicos de clientes (lectura de catálogo y estado)
                        .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/kits", "/api/kits/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/servicios", "/api/servicios/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categorias-servicio", "/api/categorias-servicio/**", "/api/categorias-servicios", "/api/categorias-servicios/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categorias-producto", "/api/categorias-producto/**", "/api/categorias-productos", "/api/categorias-productos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/banners", "/api/banners/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/experiencias-isivi").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/agenda", "/api/agenda/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/configuracion", "/api/configuracion/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ping", "/api/health").permitAll()

                        // Endpoints públicos de reservas para clientes
                        .requestMatchers(HttpMethod.GET, "/api/reservas/disponibilidad").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reservas/consultar").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reservas").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reservas/*/liberar-retencion").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reservas/*/items").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/reservas/*/reprogramar").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/reservas/*/cancelar").permitAll()

                        // Endpoints de pasarela de pago Wompi
                        .requestMatchers("/api/pagos/wompi/**").permitAll()

                        // Login de administradores
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        
                        // Endpoint público para logging de fallback (para capturar fallas incluso si no hay sesión/JWT)
                        .requestMatchers(HttpMethod.POST, "/api/uploads/log-fallback").permitAll()

                        // Todos los endpoints administrativos protegidos con rol ADMIN
                        .requestMatchers("/api/dashboard/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/configuracion", "/api/configuracion/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/configuracion", "/api/configuracion/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/agenda", "/api/agenda/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/agenda/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/agenda/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/reservas", "/api/reservas/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/reservas/bloqueos", "/api/reservas/bloqueos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/reservas/bloqueos", "/api/reservas/bloqueos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/reservas/bloqueos", "/api/reservas/bloqueos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/reservas/*/aprobar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/reservas/*/denegar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/reservas/*/admin-reprogramar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/reservas/*/aprobar-cancelacion").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/reservas/*/rechazar-cancelacion").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/reservas/*/admin-cancelar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/reservas/*/marcar-cancelacion-vista").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/reservas/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/uploads/imagen", "/api/uploads/imagen/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin/migrar-imagenes", "/api/admin/migrar-imagenes/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/productos", "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/productos", "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/productos", "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos", "/api/productos/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/kits", "/api/kits/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/kits", "/api/kits/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/kits", "/api/kits/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/kits", "/api/kits/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/servicios", "/api/servicios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/servicios", "/api/servicios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/servicios", "/api/servicios/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/categorias-servicio", "/api/categorias-servicio/**", "/api/categorias-servicios", "/api/categorias-servicios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categorias-servicio", "/api/categorias-servicio/**", "/api/categorias-servicios", "/api/categorias-servicios/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/banners", "/api/banners/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/banners", "/api/banners/**").hasRole("ADMIN")

                        .requestMatchers("/api/experiencias-isivi/admin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/experiencias-isivi", "/api/experiencias-isivi/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/experiencias-isivi", "/api/experiencias-isivi/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/experiencias-isivi", "/api/experiencias-isivi/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/experiencias-isivi", "/api/experiencias-isivi/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/agenda", "/api/agenda/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/agenda", "/api/agenda/**").hasRole("ADMIN")

                        .requestMatchers("/api/auth/administradores", "/api/auth/administradores/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/email", "/api/admin/email/**").hasRole("ADMIN")

                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimitingFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOriginsConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Event-Checksum", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

