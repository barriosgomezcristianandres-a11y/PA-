package com.isivi.app;

import com.isivi.app.model.Administrador;
import com.isivi.app.repository.AdministradorRepository;
import com.isivi.app.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RateLimitingAndSecurityHeadersTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;

    @BeforeEach
    void setUp() {
        administradorRepository.findAllByUsuario("admin_limit_test").forEach(administradorRepository::delete);
        administradorRepository.save(new Administrador("admin_limit_test", passwordEncoder.encode("1234")));
        adminToken = jwtService.generarToken("admin_limit_test");
    }

    @Test
    @DisplayName("1. Debe incluir headers de seguridad HTTP en las respuestas")
    void testSecurityHeaders() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().exists("Referrer-Policy"));
    }

    @Test
    @DisplayName("2. Debe aplicar Rate Limiting (429) tras exceder intentos en login público")
    void testRateLimitingOnLogin() throws Exception {
        String jsonPayload = "{\"usuario\":\"admin_test\",\"contrasena\":\"wrong\"}";
        String testIp = "203.0.113.111";

        // 5 peticiones permitidas
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .header("X-Forwarded-For", testIp)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonPayload))
                    .andExpect(status().isOk());
        }

        // Petición 6 bloqueada
        mockMvc.perform(post("/api/auth/login")
                        .header("X-Forwarded-For", testIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.error").value("RATE_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("3. Público excede límite en reservas -> 429")
    void testRateLimitingPublicReservations() throws Exception {
        String testIp = "203.0.113.112";

        // 30 peticiones permitidas por IP para reservas
        for (int i = 0; i < 30; i++) {
            mockMvc.perform(get("/api/reservas/disponibilidad")
                            .header("X-Forwarded-For", testIp)
                            .param("fecha", "2026-12-01"))
                    .andExpect(status().isOk());
        }

        // Petición 31 bloqueada
        mockMvc.perform(get("/api/reservas/disponibilidad")
                        .header("X-Forwarded-For", testIp)
                        .param("fecha", "2026-12-01"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("4. Wompi preparar/retomar excede límite público -> 429")
    void testRateLimitingWompiPaymentPreparation() throws Exception {
        String testIp = "203.0.113.113";

        // 10 peticiones permitidas para preparar/retomar pago por IP
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/api/pagos/wompi/preparar/fake_id")
                            .header("X-Forwarded-For", testIp))
                    .andExpect(status().isNotFound()); // NotFound significa que el filtro dejó pasar el request al controlador
        }

        // Petición 11 bloqueada
        mockMvc.perform(post("/api/pagos/wompi/preparar/fake_id")
                        .header("X-Forwarded-For", testIp))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("5. /api/ping funciona de forma amplia sin verse afectado")
    void testPingAlwaysAllowed() throws Exception {
        for (int i = 0; i < 40; i++) {
            mockMvc.perform(get("/api/ping"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("6. Administrador autenticado puede cargar el dashboard completo sin 429")
    void testAdminLoadsDashboardWithout429() throws Exception {
        String authHeader = "Bearer " + adminToken;
        String testIp = "203.0.113.114";

        // Un admin realizando más de 35 peticiones de lectura consecutivas (que bloquearían a un usuario normal)
        for (int i = 0; i < 50; i++) {
            mockMvc.perform(get("/api/dashboard/resumen")
                            .header("Authorization", authHeader)
                            .header("X-Forwarded-For", testIp))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("7. Administrador autenticado puede cargar agenda y bloqueos múltiples veces")
    void testAdminLoadsAgendaAndBlocks() throws Exception {
        String authHeader = "Bearer " + adminToken;
        String testIp = "203.0.113.115";

        // Realizar 50 llamadas de disponibilidad y bloqueos (simulando Promise.all del calendario)
        for (int i = 0; i < 50; i++) {
            mockMvc.perform(get("/api/reservas/bloqueos")
                            .header("Authorization", authHeader)
                            .header("X-Forwarded-For", testIp)
                            .param("fecha", "2026-08-22"))
                    .andExpect(status().isOk());
        }
    }
}
