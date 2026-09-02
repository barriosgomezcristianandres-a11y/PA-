package com.isivi.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.isivi.app.model.Administrador;
import com.isivi.app.model.Reserva;
import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Servicio;
import com.isivi.app.repository.AdministradorRepository;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ServicioRepository;
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

import org.springframework.test.context.TestPropertySource;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
public class SecurityAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String validAdminToken;
    private static final LocalDate SEC_DATE = LocalDate.of(2026, 12, 18);

    @BeforeEach
    void setUp() {
        administradorRepository.findAllByUsuario("admin").forEach(administradorRepository::delete);
        administradorRepository.save(new Administrador("admin", passwordEncoder.encode("1234")));


        validAdminToken = jwtService.generarToken("admin");

        reservaRepository.findByFechaCita(SEC_DATE).forEach(reservaRepository::delete);
        reservaRepository.findByCodigoReserva("ISV-SYNC-01").ifPresent(reservaRepository::delete);
        reservaRepository.findByCodigoReserva("ISV-SYNC-02").ifPresent(reservaRepository::delete);
        reservaRepository.findByCodigoReserva("ISV-SYNC-03").ifPresent(reservaRepository::delete);
        reservaRepository.findByCodigoReserva("ISV-SYNC-04").ifPresent(reservaRepository::delete);
        reservaRepository.findByCodigoReserva("ISV-REG-FLOW").ifPresent(reservaRepository::delete);
    }

    @Test
    @DisplayName("1. Endpoints públicos responden 200 OK sin autenticación")
    void testPublicEndpointsAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/kits"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/servicios"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/categorias-servicio"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/banners"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reservas/disponibilidad").param("fecha", "2026-11-20"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/agenda"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("isivi-app"));

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("2. Endpoints administrativos devuelven 401 Unauthorized sin token")
    void testAdminEndpointsProtectedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/dashboard/resumen"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("NO_AUTORIZADO"));

        mockMvc.perform(get("/api/reservas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("NO_AUTORIZADO"));

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Producto Test\",\"precio\":25000}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/auth/administradores"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("3. Login con credenciales correctas emite JWT válido")
    void testLoginIssuesValidJwtToken() throws Exception {
        String responseStr = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("usuario", "admin", "contrasena", "1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticado").value(true))
                .andExpect(jsonPath("$.token").isString())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(responseStr);
        String token = json.get("token").asText();
        assertNotNull(token);
        assertTrue(jwtService.validarToken(token));
    }

    @Test
    @DisplayName("4. Acceso administrativo permitido con Bearer JWT válido")
    void testAdminEndpointsAccessibleWithValidJwt() throws Exception {
        mockMvc.perform(get("/api/dashboard/resumen")
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.citasHoy").exists());

        mockMvc.perform(get("/api/reservas")
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("5. Token manipulado o inválido es rechazado con 401 Unauthorized")
    void testInvalidTokenRejected() throws Exception {
        mockMvc.perform(get("/api/dashboard/resumen")
                        .header("Authorization", "Bearer token_invalido_falso_12345"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("NO_AUTORIZADO"));
    }

    @Test
    @DisplayName("6. Reprogramación cliente: Código correcto + Teléfono correcto -> Permitido (200 OK)")
    void testClientReschedule_CorrectCodeAndPhone_Permitted() throws Exception {
        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-TEST-901");
        r.setNombreCliente("Cliente Seguridad 1");
        r.setTelefono("3001234567");
        r.setFechaCita(SEC_DATE);
        r.setHoraCita("08:00 AM");
        r.setEstado("Pendiente Comprobante");
        r = reservaRepository.save(r);

        String payload = objectMapper.writeValueAsString(Map.of(
                "codigoReserva", "ISV-TEST-901",
                "telefono", "3001234567",
                "fechaCita", SEC_DATE.toString(),
                "horaCita", "09:30 AM"
        ));

        mockMvc.perform(patch("/api/reservas/" + r.getId() + "/reprogramar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horaCita").value("08:00 AM"))
                .andExpect(jsonPath("$.horaPropuestaReprogramacion").value("09:30 AM"))
                .andExpect(jsonPath("$.estado").value("Pendiente Reprogramación"));

        reservaRepository.deleteById(r.getId());
    }

    @Test
    @DisplayName("7. Reprogramación cliente: Código incorrecto -> 403 Forbidden")
    void testClientReschedule_WrongCode_Forbidden() throws Exception {
        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-TEST-902");
        r.setNombreCliente("Cliente Seguridad 2");
        r.setTelefono("3001234567");
        r.setFechaCita(SEC_DATE);
        r.setHoraCita("11:00 AM");
        r.setEstado("Pendiente Comprobante");
        r = reservaRepository.save(r);

        String payload = objectMapper.writeValueAsString(Map.of(
                "codigoReserva", "ISV-CODIGO-FALSO",
                "telefono", "3001234567",
                "fechaCita", SEC_DATE.toString(),
                "horaCita", "01:30 PM"
        ));

        mockMvc.perform(patch("/api/reservas/" + r.getId() + "/reprogramar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());

        reservaRepository.deleteById(r.getId());
    }

    @Test
    @DisplayName("8. Reprogramación cliente: Teléfono incorrecto -> 403 Forbidden")
    void testClientReschedule_WrongPhone_Forbidden() throws Exception {
        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-TEST-903");
        r.setNombreCliente("Cliente Seguridad 3");
        r.setTelefono("3001234567");
        r.setFechaCita(SEC_DATE);
        r.setHoraCita("01:30 PM");
        r.setEstado("Pendiente Comprobante");
        r = reservaRepository.save(r);

        String payload = objectMapper.writeValueAsString(Map.of(
                "codigoReserva", "ISV-TEST-903",
                "telefono", "3119999999",
                "fechaCita", SEC_DATE.toString(),
                "horaCita", "03:00 PM"
        ));

        mockMvc.perform(patch("/api/reservas/" + r.getId() + "/reprogramar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());

        reservaRepository.deleteById(r.getId());
    }

    @Test
    @DisplayName("9. Reprogramación cliente: Código ausente -> 400 Bad Request")
    void testClientReschedule_MissingCode_BadRequest() throws Exception {
        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-TEST-904");
        r.setNombreCliente("Cliente Seguridad 4");
        r.setTelefono("3001234567");
        r.setFechaCita(SEC_DATE);
        r.setHoraCita("03:00 PM");
        r.setEstado("Pendiente Comprobante");
        r = reservaRepository.save(r);

        String payload = objectMapper.writeValueAsString(Map.of(
                "telefono", "3001234567",
                "fechaCita", SEC_DATE.toString(),
                "horaCita", "04:30 PM"
        ));

        mockMvc.perform(patch("/api/reservas/" + r.getId() + "/reprogramar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        reservaRepository.deleteById(r.getId());
    }

    @Test
    @DisplayName("10. Reprogramación cliente: Teléfono ausente -> 400 Bad Request")
    void testClientReschedule_MissingPhone_BadRequest() throws Exception {
        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-TEST-905");
        r.setNombreCliente("Cliente Seguridad 5");
        r.setTelefono("3001234567");
        r.setFechaCita(SEC_DATE);
        r.setHoraCita("04:30 PM");
        r.setEstado("Pendiente Comprobante");
        r = reservaRepository.save(r);

        String payload = objectMapper.writeValueAsString(Map.of(
                "codigoReserva", "ISV-TEST-905",
                "fechaCita", SEC_DATE.toString(),
                "horaCita", "06:00 PM"
        ));

        mockMvc.perform(patch("/api/reservas/" + r.getId() + "/reprogramar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        reservaRepository.deleteById(r.getId());
    }

    @Test
    @DisplayName("11. Cancelación cliente: Código y teléfono correctos -> Permitido (200 OK)")
    void testClientCancel_CorrectCodeAndPhone_Permitted() throws Exception {
        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-TEST-906");
        r.setNombreCliente("Cliente Seguridad 6");
        r.setTelefono("3001234567");
        r.setFechaCita(SEC_DATE);
        r.setHoraCita("06:00 PM");
        r.setEstado("Pendiente Comprobante");
        r = reservaRepository.save(r);

        String payload = objectMapper.writeValueAsString(Map.of(
                "codigoReserva", "ISV-TEST-906",
                "telefono", "3001234567"
        ));

        mockMvc.perform(patch("/api/reservas/" + r.getId() + "/cancelar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Cancelada"));

        reservaRepository.deleteById(r.getId());
    }

    @Test
    @DisplayName("12. Cancelación cliente: Datos ausentes o incorrectos -> Rechazado")
    void testClientCancel_MissingOrWrongData_Rejected() throws Exception {
        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-TEST-907");
        r.setNombreCliente("Cliente Seguridad 7");
        r.setTelefono("3001234567");
        r.setFechaCita(SEC_DATE);
        r.setHoraCita("09:30 AM");
        r.setEstado("Pendiente Comprobante");
        r = reservaRepository.save(r);

        // Sin datos
        mockMvc.perform(patch("/api/reservas/" + r.getId() + "/cancelar"))
                .andExpect(status().isBadRequest());

        // Con código incorrecto
        mockMvc.perform(patch("/api/reservas/" + r.getId() + "/cancelar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("codigoReserva", "ISV-WRONG", "telefono", "3001234567"))))
                .andExpect(status().isForbidden());

        reservaRepository.deleteById(r.getId());
    }

    @Test
    @DisplayName("13. Sincronización ítems cliente: Código correcto -> Permitido (200 OK)")
    void testClientSyncItems_CorrectCode_Permitted() throws Exception {
        Producto p = new Producto("Shampoo Test", 25000.0, "Desc", "", true, 10);
        p = productoRepository.save(p);

        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-SYNC-01");
        r.setNombreCliente("Cliente Sync 1");
        r.setTelefono("3001112233");
        r.setEstado("Pendiente Pago");
        r.setMedioPago("WOMPI");
        r.setItemsInventario(List.of(new ItemReserva(p.getId(), "producto", 2)));
        r = reservaRepository.save(r);

        String payload = objectMapper.writeValueAsString(List.of(
                Map.of("id", p.getId(), "tipo", "producto", "cantidad", 2)
        ));

        mockMvc.perform(post("/api/reservas/" + r.getId() + "/items")
                        .param("codigo", "ISV-SYNC-01")
                        .param("telefono", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        reservaRepository.deleteById(r.getId());
        productoRepository.deleteById(p.getId());
    }

    @Test
    @DisplayName("14. Sincronización ítems cliente: Código incorrecto -> 403 Forbidden")
    void testClientSyncItems_WrongCode_Forbidden() throws Exception {
        Producto p = new Producto("Shampoo Test 2", 25000.0, "Desc", "", true, 10);
        p = productoRepository.save(p);

        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-SYNC-02");
        r.setNombreCliente("Cliente Sync 2");
        r.setTelefono("3001112233");
        r.setEstado("Pendiente Pago");
        r.setMedioPago("WOMPI");
        r.setItemsInventario(List.of(new ItemReserva(p.getId(), "producto", 2)));
        r = reservaRepository.save(r);

        String payload = objectMapper.writeValueAsString(List.of(
                Map.of("id", p.getId(), "tipo", "producto", "cantidad", 2)
        ));

        mockMvc.perform(post("/api/reservas/" + r.getId() + "/items")
                        .param("codigo", "ISV-WRONG")
                        .param("telefono", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());

        reservaRepository.deleteById(r.getId());
        productoRepository.deleteById(p.getId());
    }

    @Test
    @DisplayName("15. Sincronización ítems cliente: Sin código/teléfono -> 403 Forbidden")
    void testClientSyncItems_NoCredentials_Forbidden() throws Exception {
        Producto p = new Producto("Shampoo Test 3", 25000.0, "Desc", "", true, 10);
        p = productoRepository.save(p);

        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-SYNC-03");
        r.setNombreCliente("Cliente Sync 3");
        r.setTelefono("3001112233");
        r.setEstado("Pendiente Pago");
        r.setMedioPago("WOMPI");
        r.setItemsInventario(List.of(new ItemReserva(p.getId(), "producto", 2)));
        r = reservaRepository.save(r);

        String payload = objectMapper.writeValueAsString(List.of(
                Map.of("id", p.getId(), "tipo", "producto", "cantidad", 2)
        ));

        mockMvc.perform(post("/api/reservas/" + r.getId() + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());

        reservaRepository.deleteById(r.getId());
        productoRepository.deleteById(p.getId());
    }

    @Test
    @DisplayName("16. Sincronización ítems cliente: Lista vacía de ítems -> 400 Bad Request")
    void testClientSyncItems_EmptyList_BadRequest() throws Exception {
        Producto p = new Producto("Shampoo Test 4", 25000.0, "Desc", "", true, 10);
        p = productoRepository.save(p);

        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-SYNC-04");
        r.setNombreCliente("Cliente Sync 4");
        r.setTelefono("3001112233");
        r.setEstado("Pendiente Pago");
        r.setMedioPago("WOMPI");
        r.setItemsInventario(List.of(new ItemReserva(p.getId(), "producto", 2)));
        r = reservaRepository.save(r);

        mockMvc.perform(post("/api/reservas/" + r.getId() + "/items")
                        .param("codigo", "ISV-SYNC-04")
                        .param("telefono", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest());

        reservaRepository.deleteById(r.getId());
        productoRepository.deleteById(p.getId());
    }

    @Test
    @DisplayName("17. Regresión completa del flujo de retoma con cliente anónimo")
    void testResumePaymentRegressionFlow() throws Exception {
        Producto p = new Producto("Producto Regresion", 20000.0, "Desc", "", true, 5);
        p = productoRepository.save(p);

        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-REG-FLOW");
        r.setNombreCliente("Cliente Regresion");
        r.setTelefono("3009998877");
        r.setEstado("Pendiente Pago");
        r.setMedioPago("WOMPI");
        r.setTipoEntrega("pickup");
        r.setItemsInventario(List.of(new ItemReserva(p.getId(), "producto", 2)));
        r = reservaRepository.save(r);

        // 1. Sincronización (POST /items)
        String syncPayload = objectMapper.writeValueAsString(List.of(
                Map.of("id", p.getId(), "tipo", "producto", "cantidad", 2)
        ));

        mockMvc.perform(post("/api/reservas/" + r.getId() + "/items")
                        .param("codigo", "ISV-REG-FLOW")
                        .param("telefono", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(syncPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(40000.0))
                .andExpect(jsonPath("$.itemsInventario[0].cantidad").value(2));

        // 2. Retoma de pago (POST /retomar)
        String retomarPayload = objectMapper.writeValueAsString(Map.of(
                "telefono", "",
                "codigoReserva", "ISV-REG-FLOW"
        ));

        mockMvc.perform(post("/api/pagos/wompi/retomar/" + r.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(retomarPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referencia").exists())
                .andExpect(jsonPath("$.montoCentavos").value(4000000));

        reservaRepository.deleteById(r.getId());
        productoRepository.deleteById(p.getId());
    }
}
