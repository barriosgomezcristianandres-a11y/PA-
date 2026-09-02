package com.isivi.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isivi.app.model.Administrador;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.AdministradorRepository;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.security.JwtService;
import com.isivi.app.service.EmailNotificationService;
import com.isivi.app.service.ReservaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        "isivi.wompi.base-url=https://sandbox.wompi.co/v1",
        "app.mail.enabled=false"
})
public class ReservationEmailAndLookupTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String validAdminToken;
    private Reserva reservaPrueba;

    @BeforeEach
    void setUp() {
        reservaRepository.deleteAll();
        administradorRepository.findAllByUsuario("admin").forEach(administradorRepository::delete);
        administradorRepository.save(new Administrador("admin", passwordEncoder.encode("1234")));

        validAdminToken = jwtService.generarToken("admin");

        reservaPrueba = new Reserva();
        reservaPrueba.setCodigoReserva("ISV-9999");
        reservaPrueba.setNombreCliente("Valentina Gómez");
        reservaPrueba.setTelefono("3008949050");
        reservaPrueba.setEmail("valentina@isivi.com");
        reservaPrueba.setCiudad("Cartagena");
        reservaPrueba.setFechaCita(LocalDate.now().plusDays(2));
        reservaPrueba.setHoraCita("10:00 AM");
        reservaPrueba.setSubtotal(120000.0);
        reservaPrueba.setAnticipo(30000.0);
        reservaPrueba.setSaldo(90000.0);
        reservaPrueba.setEstado("Confirmado");
        reservaPrueba.setMedioPago("WOMPI");
        reservaPrueba.setArchivada(false);
        reservaPrueba.setFechaRegistro(LocalDate.now());
        reservaPrueba.setItems(List.of("Repolarización Intensiva"));
        reservaPrueba = reservaRepository.save(reservaPrueba);
    }

    @AfterEach
    void tearDown() {
        reservaRepository.deleteAll();
    }

    @Test
    @DisplayName("1. Consulta con campo único usando código exacto en mayúsculas")
    void testLookupByCodeExact() throws Exception {
        mockMvc.perform(get("/api/reservas/consultar").param("query", "ISV-9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoReserva").value("ISV-9999"))
                .andExpect(jsonPath("$.nombreCliente").value("Valentina Gómez"))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    @DisplayName("2. Consulta con campo único usando código en minúsculas y espacios")
    void testLookupByCodeCaseInsensitiveAndSpaces() throws Exception {
        mockMvc.perform(get("/api/reservas/consultar").param("query", "  isv-9999  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoReserva").value("ISV-9999"))
                .andExpect(jsonPath("$.nombreCliente").value("Valentina Gómez"));
    }

    @Test
    @DisplayName("3. Consulta con campo único usando número de teléfono de 10 dígitos")
    void testLookupByPhoneDigits() throws Exception {
        mockMvc.perform(get("/api/reservas/consultar").param("query", "3008949050"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoReserva").value("ISV-9999"))
                .andExpect(jsonPath("$.telefono").doesNotExist());
    }

    @Test
    @DisplayName("4. Consulta con campo único usando teléfono formateado con +57 y espacios")
    void testLookupByPhoneFormatted() throws Exception {
        mockMvc.perform(get("/api/reservas/consultar").param("query", "+57 300 894 9050"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoReserva").value("ISV-9999"))
                .andExpect(jsonPath("$.telefono").doesNotExist());
    }

    @Test
    @DisplayName("5. Consulta con campo vacío devuelve 400 Bad Request")
    void testLookupEmptyQuery() throws Exception {
        mockMvc.perform(get("/api/reservas/consultar").param("query", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("6. Consulta con código o teléfono inexistente devuelve 404 Not Found")
    void testLookupNotFound() throws Exception {
        mockMvc.perform(get("/api/reservas/consultar").param("query", "ISV-0000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").exists());

        mockMvc.perform(get("/api/reservas/consultar").param("query", "3119999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("7. Compatibilidad hacia atrás: consulta enviando código y teléfono explícitos")
    void testLookupBackwardCompatibility() throws Exception {
        mockMvc.perform(get("/api/reservas/consultar")
                        .param("codigo", "ISV-9999")
                        .param("telefono", "3008949050"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoReserva").value("ISV-9999"));
    }

    @Test
    @DisplayName("8. Validación de correo inválido en endpoint de creación devuelve 400")
    void testCreateReservationInvalidEmail() throws Exception {
        Reserva nueva = new Reserva();
        nueva.setNombreCliente("Prueba Correo");
        nueva.setTelefono("3054449715");
        nueva.setEmail("correo-invalido-sin-arroba");
        nueva.setCiudad("Cartagena");
        nueva.setItems(List.of("Corte Dama"));

        mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nueva)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("EMAIL_INVALIDO"));
    }

    @Test
    @DisplayName("9. Reserva sin email histórica se recupera y procesa sin errores")
    void testHistoricalReservationWithoutEmail() throws Exception {
        Reserva historica = new Reserva();
        historica.setCodigoReserva("ISV-HIST");
        historica.setNombreCliente("Cliente Historico");
        historica.setTelefono("3012345678");
        historica.setEmail(null);
        historica.setCiudad("Cartagena");
        historica.setEstado("Confirmado");
        historica.setFechaRegistro(LocalDate.now().minusMonths(2));
        historica.setArchivada(false);
        historica = reservaRepository.save(historica);

        mockMvc.perform(get("/api/reservas/consultar").param("query", "ISV-HIST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoReserva").value("ISV-HIST"))
                .andExpect(jsonPath("$.nombreCliente").value("Cliente Historico"));
    }

    @Test
    @DisplayName("10. Reenvío manual desde admin protegido por autenticación")
    void testResendEmailSecurity() throws Exception {
        // Sin autenticación debe fallar 401
        mockMvc.perform(post("/api/reservas/" + reservaPrueba.getId() + "/reenviar-email"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("11. Reenvío manual desde admin con token JWT responde con éxito")
    void testResendEmailAuthenticatedAdmin() throws Exception {
        mockMvc.perform(post("/api/reservas/" + reservaPrueba.getId() + "/reenviar-email")
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("12. Idempotencia y resiliencia de confirmación por email")
    void testEmailIdempotencyAndResilience() {
        assertNotNull(emailNotificationService);

        // Reserva en estado Pendiente no debe enviar correo
        Reserva pendiente = new Reserva();
        pendiente.setCodigoReserva("ISV-PEND");
        pendiente.setNombreCliente("Pendiente Test");
        pendiente.setTelefono("3001112233");
        pendiente.setEmail("test@isivi.com");
        pendiente.setEstado("Pendiente Pago");
        pendiente = reservaRepository.save(pendiente);

        boolean enviadoPendiente = emailNotificationService.enviarConfirmacion(pendiente);
        assertFalse(enviadoPendiente, "No debe enviar correo en estado Pendiente");

        // Reserva Confirmada con MAIL_ENABLED=false
        reservaPrueba.setEstado("Confirmado");
        reservaPrueba.setEmailConfirmacionEnviada(false);
        reservaPrueba = reservaRepository.save(reservaPrueba);

        boolean enviado = emailNotificationService.enviarConfirmacion(reservaPrueba);
        assertFalse(enviado, "Con MAIL_ENABLED=false debe retornar false de forma segura");

        // Simular que ya fue enviada previamente para probar idempotencia
        reservaPrueba.setEmailConfirmacionEnviada(true);
        reservaPrueba = reservaRepository.save(reservaPrueba);

        boolean enviadoRepetido = emailNotificationService.enviarConfirmacion(reservaPrueba);
        assertFalse(enviadoRepetido, "Llamada subsecuente no debe reenviar por regla de idempotencia");
    }
}
