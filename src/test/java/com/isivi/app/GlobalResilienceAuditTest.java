package com.isivi.app;

import com.isivi.app.exception.GlobalExceptionHandler;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.ReservaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "isivi.wompi.public-key=pub_test_QA_ISIVI_12345",
        "isivi.wompi.private-key=prv_test_QA_SECRET_98765",
        "isivi.wompi.integrity-secret=test_integrity_QA_SECRET_123",
        "isivi.wompi.events-secret=test_events_QA_SECRET_456",
        "isivi.wompi.sandbox=true",
        "isivi.wompi.base-url=https://sandbox.wompi.co/v1"
})
public class GlobalResilienceAuditTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        reservaRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        reservaRepository.deleteAll();
    }

    @Test
    @DisplayName("Resiliencia 1: Ping y Health Check responden 200 OK inmediatamente")
    void testHealthAndPing() throws Exception {
        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("isivi-app"));

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Resiliencia 2: 401 Unauthorized en endpoints admin protegidos sin token")
    void testUnauthorizedAdminAccess() throws Exception {
        mockMvc.perform(get("/api/dashboard/resumen"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("NO_AUTORIZADO"))
                .andExpect(jsonPath("$.mensaje").exists());

        mockMvc.perform(get("/api/administradores"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Resiliencia 3: 404 Not Found en consulta de reserva inexistente")
    void testNotFoundConsultaReserva() throws Exception {
        mockMvc.perform(get("/api/reservas/consultar")
                        .param("codigo", "ISV-NOEXISTE")
                        .param("telefono", "3000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No encontramos una reserva con esos datos."));
    }

    @Test
    @DisplayName("Resiliencia 4: 400 Bad Request ante JSON malformado o ilegible")
    void testMalformedJsonPayload() throws Exception {
        mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ nombreCliente: corrupt_json_without_quotes..."))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("FORMATO_INVALIDO"))
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("Resiliencia 5: 400 Bad Request ante campos faltantes o inválidos")
    void testValidationFailure() throws Exception {
        mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreCliente\":\"\",\"telefono\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"))
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("Resiliencia 6: 405 Method Not Allowed ante métodos HTTP no soportados")
    void testMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/api/pagos/wompi/disponible"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error").value("METODO_NO_PERMITIDO"));
    }

    @Test
    @DisplayName("Resiliencia 7: 403 Forbidden cuando datos de reprogramación no coinciden con la reserva")
    void testForbiddenReprogramacion() throws Exception {
        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-RESIL-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        r.setNombreCliente("Cliente Resiliencia");
        r.setTelefono("3001234567");
        r.setFechaCita(LocalDate.now().plusDays(3));
        r.setHoraCita("08:00 AM");
        r.setSubtotal(80000.0);
        r.setAnticipo(20000.0);
        r.setSaldo(60000.0);
        Reserva guardada = reservaRepository.save(r);

        // Intento de reprogramación con teléfono incorrecto
        mockMvc.perform(patch("/api/reservas/" + guardada.getId() + "/reprogramar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigoReserva\":\"" + guardada.getCodigoReserva() + "\",\"telefono\":\"3999999999\",\"fechaCita\":\"" + LocalDate.now().plusDays(5) + "\",\"horaCita\":\"11:00 AM\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value(containsString("No pudimos validar tu reserva")));
    }

    @Test
    @DisplayName("Resiliencia 8: GlobalExceptionHandler maneja DuplicateKeyException retornando 409 Conflict")
    void testGlobalExceptionHandlerDuplicateKey() {
        assertNotNull(globalExceptionHandler);
        var response = globalExceptionHandler.handleDuplicateKey(new DuplicateKeyException("E11000 duplicate key error"));
        assertNotNull(response);
        org.junit.jupiter.api.Assertions.assertEquals(409, response.getStatusCode().value());
        org.junit.jupiter.api.Assertions.assertEquals("CONFLICTO", response.getBody().get("error"));
    }
}
