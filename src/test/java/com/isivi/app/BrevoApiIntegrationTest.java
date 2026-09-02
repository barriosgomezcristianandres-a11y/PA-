package com.isivi.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isivi.app.model.Administrador;
import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.AdministradorRepository;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.security.JwtService;
import com.isivi.app.service.EmailNotificationService;
import com.isivi.app.service.ReservaService;
import com.isivi.app.service.WompiService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "isivi.brevo.api-key=xkeysib-test-dummy-api-key-12345",
        "isivi.brevo.base-url=https://api.brevo.com/v3/smtp/email",
        "isivi.brevo.sender-name=ISIVI",
        "app.mail.from=contacto@isivi.com",
        "app.mail.enabled=true",
        "isivi.wompi.public-key=pub_test_BREVO_API_123",
        "isivi.wompi.private-key=prv_test_BREVO_API_456",
        "isivi.wompi.integrity-secret=test_integrity_BREVO_API_789",
        "isivi.wompi.events-secret=test_events_BREVO_API_000",
        "isivi.wompi.sandbox=true",
        "isivi.wompi.base-url=https://sandbox.wompi.co/v1"
})
public class BrevoApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockRestServiceServer mockServer;
    private String validAdminToken;
    private Reserva reservaPrueba;

    private String calcularSha256(String valor) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @BeforeEach
    void setUp() {
        reservaRepository.deleteAll();
        administradorRepository.findByUsuario("admin_brevo").ifPresent(administradorRepository::delete);
        administradorRepository.save(new Administrador("admin_brevo", passwordEncoder.encode("secretAdmin123")));
        validAdminToken = jwtService.generarToken("admin_brevo");

        // Configurar MockRestServiceServer para interceptar llamadas HTTP de RestClient
        RestClient.Builder builder = RestClient.builder();
        this.mockServer = MockRestServiceServer.bindTo(builder).build();
        emailNotificationService.setRestClient(builder.build());

        ItemReserva item = new ItemReserva();
        item.setId("serv-brevo-1");
        item.setNombre("Terapia Capilar Orgánica");
        item.setTipo("servicio");
        item.setCantidad(1);
        item.setPrecioUnitario(120000.0);
        item.setSubtotal(120000.0);

        reservaPrueba = new Reserva();
        reservaPrueba.setCodigoReserva("ISV-8888");
        reservaPrueba.setNombreCliente("Laura Restrepo");
        reservaPrueba.setTelefono("3007778899");
        reservaPrueba.setEmail("laura.restrepo@example.com");
        reservaPrueba.setCiudad("Cartagena");
        reservaPrueba.setFechaCita(LocalDate.now().plusDays(2));
        reservaPrueba.setHoraCita("11:00 AM");
        reservaPrueba.setItemsInventario(List.of(item));
        reservaPrueba.setItems(List.of("Terapia Capilar Orgánica"));
        reservaPrueba.setSubtotal(120000.0);
        reservaPrueba.setAnticipo(30000.0);
        reservaPrueba.setSaldo(90000.0);
        reservaPrueba.setEstado("Confirmado");
        reservaPrueba.setMedioPago("WOMPI");
        reservaPrueba.setReferenciaWompi("ISV-ISV-8888-REFTEST");
        reservaPrueba.setMontoPagoCentavos(3000000L);
        reservaPrueba.setArchivada(false);
        reservaPrueba.setEmailConfirmacionEnviada(false);
        reservaPrueba.setFechaRegistro(LocalDate.now());
        reservaPrueba = reservaRepository.save(reservaPrueba);
    }

    @AfterEach
    void tearDown() {
        reservaRepository.deleteAll();
        administradorRepository.findByUsuario("admin_brevo").ifPresent(administradorRepository::delete);
    }

    @Test
    @DisplayName("1. Diagnóstico de configuración GET /api/admin/email/config reporta provider=brevo-api")
    void testConfigReportsBrevoApi() throws Exception {
        mockMvc.perform(get("/api/admin/email/config")
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.provider").value("brevo-api"))
                .andExpect(jsonPath("$.apiKeyConfigured").value(true))
                .andExpect(jsonPath("$.fromConfigured").value(true))
                .andExpect(jsonPath("$.apiUrl").value("https://api.brevo.com/v3/smtp/email"));
    }

    @Test
    @DisplayName("2. Despacho exitoso por Brevo API HTTPS (201 Created) confirma reserva y fecha")
    void testBrevoApiSuccess() {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("api-key", "xkeysib-test-dummy-api-key-12345"))
                .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath("$.sender.email").value("contacto@isivi.com"))
                .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath("$.to[0].email").value("laura.restrepo@example.com"))
                .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath("$.subject").exists())
                .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath("$.htmlContent").exists())
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"messageId\":\"<202608191745.test123@smtp-relay.brevo.com>\"}"));

        boolean enviado = emailNotificationService.enviarConfirmacion(reservaPrueba);
        assertTrue(enviado);
        mockServer.verify();

        Reserva actualizada = reservaRepository.findById(reservaPrueba.getId()).orElseThrow();
        assertTrue(actualizada.getEmailConfirmacionEnviada());
        assertNotNull(actualizada.getFechaEnvioConfirmacion());
        assertNull(actualizada.getEmailErrorEnvio());
    }

    @Test
    @DisplayName("3. Idempotencia: Una reserva con emailConfirmacionEnviada=true no repite llamada a Brevo API")
    void testIdempotencyPreventsDuplicateApiCall() {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body("{\"messageId\":\"<id1>\"}"));

        boolean primeraVez = emailNotificationService.enviarConfirmacion(reservaPrueba);
        assertTrue(primeraVez);
        mockServer.verify();

        Reserva guardada = reservaRepository.findById(reservaPrueba.getId()).orElseThrow();
        assertTrue(guardada.getEmailConfirmacionEnviada());

        // Segunda llamada no debe hacer request HTTP a Brevo
        boolean segundaVez = emailNotificationService.enviarConfirmacion(guardada);
        assertFalse(segundaVez);
    }

    @Test
    @DisplayName("4. Brevo API 401/403 Auth Failed es capturado como BREVO_AUTH_FAILED sin revocar pagos")
    void testBrevoApiAuthFailed() {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":\"unauthorized\",\"message\":\"Key not found\"}"));

        boolean enviado = emailNotificationService.enviarConfirmacion(reservaPrueba);
        assertFalse(enviado);
        mockServer.verify();

        Reserva actualizada = reservaRepository.findById(reservaPrueba.getId()).orElseThrow();
        assertEquals("Confirmado", actualizada.getEstado());
        assertFalse(actualizada.getEmailConfirmacionEnviada());
        assertNotNull(actualizada.getEmailErrorEnvio());
        assertTrue(actualizada.getEmailErrorEnvio().contains("autenticación Brevo API"));
    }

    @Test
    @DisplayName("5. Brevo API 400 Bad Request es capturado como BREVO_BAD_REQUEST")
    void testBrevoApiBadRequest() {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":\"invalid_parameter\",\"message\":\"Invalid email address\"}"));

        boolean enviado = emailNotificationService.enviarConfirmacion(reservaPrueba);
        assertFalse(enviado);
        mockServer.verify();

        Reserva actualizada = reservaRepository.findById(reservaPrueba.getId()).orElseThrow();
        assertFalse(actualizada.getEmailConfirmacionEnviada());
        assertNotNull(actualizada.getEmailErrorEnvio());
    }

    @Test
    @DisplayName("6. Brevo API 429 Rate Limit ejecuta reintento y captura código seguro")
    void testBrevoApiRateLimitWithRetry() {
        // Intento 1: 429
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).contentType(MediaType.APPLICATION_JSON).body("{\"code\":\"rate_limit\"}"));
        // Intento 2 (reintento): 429
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).contentType(MediaType.APPLICATION_JSON).body("{\"code\":\"rate_limit\"}"));

        boolean enviado = emailNotificationService.enviarConfirmacion(reservaPrueba);
        assertFalse(enviado);
        mockServer.verify();
    }

    @Test
    @DisplayName("7. Brevo API 500 Server Error ejecuta reintento y recupera con éxito en 2do intento")
    void testBrevoApi500RecoversOnSecondAttempt() {
        // Intento 1: 500
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(withServerError().contentType(MediaType.APPLICATION_JSON).body("{\"code\":\"server_error\"}"));
        // Intento 2: 201 Created
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body("{\"messageId\":\"<retry_ok>\"}"));

        boolean enviado = emailNotificationService.enviarConfirmacion(reservaPrueba);
        assertTrue(enviado);
        mockServer.verify();

        Reserva actualizada = reservaRepository.findById(reservaPrueba.getId()).orElseThrow();
        assertTrue(actualizada.getEmailConfirmacionEnviada());
    }

    @Test
    @DisplayName("8. Desactivación cuando MAIL_ENABLED=false no intenta llamar a la API")
    void testMailDisabledBehavior() {
        ReflectionTestUtils.setField(emailNotificationService, "mailEnabled", false);
        try {
            boolean resultado = emailNotificationService.enviarConfirmacion(reservaPrueba);
            assertFalse(resultado);
            // El mockServer no espera ninguna llamada
            mockServer.verify();
        } finally {
            ReflectionTestUtils.setField(emailNotificationService, "mailEnabled", true);
        }
    }

    @Test
    @DisplayName("9. Flujo Wompi APPROVED dispara Brevo API automáticamente")
    void testWompiApprovedDispatchesBrevoApi() throws Exception {
        reservaPrueba.setEstado("Pendiente Pago");
        reservaPrueba.setEstadoPago("PENDIENTE");
        reservaPrueba.setEmailConfirmacionEnviada(false);
        reservaRepository.save(reservaPrueba);

        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body("{\"messageId\":\"<wompi_ok>\"}"));

        String transaccionId = "tx-brevo-api-123";
        long timestamp = 1719000000L;
        String concat = transaccionId + "APPROVED" + reservaPrueba.getMontoPagoCentavos() + timestamp + "test_events_BREVO_API_000";
        String checksum = calcularSha256(concat);

        String payloadJson = "{"
                + "\"event\":\"transaction.updated\","
                + "\"data\":{"
                + "  \"transaction\":{"
                + "    \"id\":\"" + transaccionId + "\","
                + "    \"reference\":\"" + reservaPrueba.getReferenciaWompi() + "\","
                + "    \"amount_in_cents\":" + reservaPrueba.getMontoPagoCentavos() + ","
                + "    \"currency\":\"COP\","
                + "    \"status\":\"APPROVED\","
                + "    \"payment_method_type\":\"CARD\""
                + "  }"
                + "},"
                + "\"environment\":\"test\","
                + "\"timestamp\":" + timestamp + ","
                + "\"signature\":{"
                + "  \"properties\":[\"transaction.id\",\"transaction.status\",\"transaction.amount_in_cents\"],"
                + "  \"checksum\":\"" + checksum + "\""
                + "}"
                + "}";

        mockMvc.perform(post("/api/pagos/wompi/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadJson))
                .andExpect(status().isOk());

        mockServer.verify();
        Reserva enDb = reservaRepository.findById(reservaPrueba.getId()).orElseThrow();
        assertEquals("Confirmado", enDb.getEstado());
        assertTrue(enDb.getEmailConfirmacionEnviada());
    }

    @Test
    @DisplayName("10. Aprobación manual de transferencia por admin dispara Brevo API")
    void testAdminApprovalDispatchesBrevoApi() {
        reservaPrueba.setEstado("Pendiente Comprobante");
        reservaPrueba.setEmailConfirmacionEnviada(false);
        reservaRepository.save(reservaPrueba);

        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body("{\"messageId\":\"<admin_ok>\"}"));

        reservaService.aprobar(reservaPrueba);

        mockServer.verify();
        Reserva enDb = reservaRepository.findById(reservaPrueba.getId()).orElseThrow();
        assertEquals("Confirmado", enDb.getEstado());
        assertTrue(enDb.getEmailConfirmacionEnviada());
    }

    @Test
    @DisplayName("11. Reserva en estado Pendiente NO llama a Brevo API")
    void testPendingReservationDoesNotCallBrevoApi() {
        Reserva pendiente = new Reserva();
        pendiente.setCodigoReserva("ISV-PEND-API");
        pendiente.setNombreCliente("Pendiente API");
        pendiente.setTelefono("3001119999");
        pendiente.setEmail("pendiente.api@example.com");
        pendiente.setEstado("Pendiente Pago");
        pendiente = reservaRepository.save(pendiente);

        boolean enviado = emailNotificationService.enviarConfirmacion(pendiente);
        assertFalse(enviado);
        mockServer.verify();
    }

    @Test
    @DisplayName("12. Wompi DECLINED no llama a Brevo API")
    void testWompiDeclinedDoesNotCallBrevoApi() throws Exception {
        reservaPrueba.setEstado("Pendiente Pago");
        reservaPrueba.setEstadoPago("PENDIENTE");
        reservaPrueba.setEmailConfirmacionEnviada(false);
        reservaRepository.save(reservaPrueba);

        String transaccionId = "tx-declined-api";
        long timestamp = 1719000000L;
        String concat = transaccionId + "DECLINED" + reservaPrueba.getMontoPagoCentavos() + timestamp + "test_events_BREVO_API_000";
        String checksum = calcularSha256(concat);

        String payloadJson = "{"
                + "\"event\":\"transaction.updated\","
                + "\"data\":{"
                + "  \"transaction\":{"
                + "    \"id\":\"" + transaccionId + "\","
                + "    \"reference\":\"" + reservaPrueba.getReferenciaWompi() + "\","
                + "    \"amount_in_cents\":" + reservaPrueba.getMontoPagoCentavos() + ","
                + "    \"currency\":\"COP\","
                + "    \"status\":\"DECLINED\","
                + "    \"payment_method_type\":\"CARD\""
                + "  }"
                + "},"
                + "\"environment\":\"test\","
                + "\"timestamp\":" + timestamp + ","
                + "\"signature\":{"
                + "  \"properties\":[\"transaction.id\",\"transaction.status\",\"transaction.amount_in_cents\"],"
                + "  \"checksum\":\"" + checksum + "\""
                + "}"
                + "}";

        mockMvc.perform(post("/api/pagos/wompi/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadJson))
                .andExpect(status().isOk());

        mockServer.verify();
        Reserva enDb = reservaRepository.findById(reservaPrueba.getId()).orElseThrow();
        assertEquals("Denegada", enDb.getEstado());
        assertFalse(enDb.getEmailConfirmacionEnviada());
    }

    @Test
    @DisplayName("13. Endpoint de prueba POST /api/admin/email/test despacha por Brevo API exitosamente")
    void testAdminTestEndpointUsesBrevoApi() throws Exception {
        mockServer.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("api-key", "xkeysib-test-dummy-api-key-12345"))
                .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath("$.to[0].email").value("test.admin@isivi.com"))
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body("{\"messageId\":\"<test_admin_ok>\"}"));

        mockMvc.perform(post("/api/admin/email/test")
                        .header("Authorization", "Bearer " + validAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "test.admin@isivi.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.provider").value("brevo-api"))
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        mockServer.verify();
    }
}
