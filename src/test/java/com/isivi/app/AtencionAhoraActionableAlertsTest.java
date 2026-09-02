package com.isivi.app;

import com.isivi.app.controller.DashboardController;
import com.isivi.app.model.Administrador;
import com.isivi.app.model.Kit;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.AdministradorRepository;
import com.isivi.app.repository.KitRepository;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AtencionAhoraActionableAlertsTest {

    @Autowired
    private DashboardController dashboardController;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private KitRepository kitRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    private LocalDate hoy;
    private String adminToken;

    @BeforeEach
    public void setup() {
        hoy = LocalDate.now(ZoneId.of("America/Bogota"));
        LocalDateTime morning = LocalDateTime.of(hoy, LocalTime.of(8, 0));
        dashboardController.setClock(Clock.fixed(morning.atZone(ZoneId.of("America/Bogota")).toInstant(), ZoneId.of("America/Bogota")));

        administradorRepository.findAllByUsuario("admin_alert_test").forEach(administradorRepository::delete);
        administradorRepository.save(new Administrador("admin_alert_test", passwordEncoder.encode("secret")));
        adminToken = jwtService.generarToken("admin_alert_test");
    }

    @AfterEach
    public void tearDown() {
        dashboardController.setClock(Clock.system(ZoneId.of("America/Bogota")));
        administradorRepository.findAllByUsuario("admin_alert_test").forEach(administradorRepository::delete);
    }

    @Test
    @DisplayName("1. Alerta de Producto Agotado Único contiene targetId, nombre, entidad, filtro y acción exacta")
    public void testAlertaProductoAgotadoUnico() {
        String suffix = "-single-" + System.currentTimeMillis();
        Producto p = new Producto();
        p.setNombre("Crema para Peinar " + suffix);
        p.setPrecio(42000.0);
        p.setCantidad(0);
        p.setEnStock(false);
        final Producto savedP = productoRepository.save(p);
        final String savedId = savedP.getId();

        try {
            ResponseEntity<Map<String, Object>> resp = dashboardController.obtenerResumen();
            assertEquals(200, resp.getStatusCode().value());
            Map<String, Object> body = resp.getBody();
            assertNotNull(body);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> alertas = (List<Map<String, Object>>) body.get("alertasAtencion");
            assertNotNull(alertas);

            Map<String, Object> alertaAgotado = alertas.stream()
                    .filter(a -> "agotado".equals(a.get("tipo")))
                    .findFirst()
                    .orElse(null);

            assertNotNull(alertaAgotado, "Debe existir alerta de agotados");
            assertEquals("PRODUCTO", alertaAgotado.get("entidad"));
            assertEquals("products", alertaAgotado.get("modulo"));
            assertEquals("agotados", alertaAgotado.get("filtro"));
            assertEquals("EDITAR_PRODUCTO", alertaAgotado.get("accion"));
            assertNotNull(alertaAgotado.get("targetId"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) alertaAgotado.get("items");
            assertNotNull(items);
            assertFalse(items.isEmpty());
            assertTrue(items.stream().anyMatch(it -> savedId.equals(it.get("id"))));
        } finally {
            productoRepository.delete(savedP);
        }
    }

    @Test
    @DisplayName("2. Alerta de Múltiples Productos y Kits Agotados lista todos los ítems afectados")
    public void testAlertaMultiplesAgotados() {
        String suffix = "-multi-" + System.currentTimeMillis();
        Producto p1 = new Producto();
        p1.setNombre("Shampoo Romero " + suffix);
        p1.setPrecio(38000.0);
        p1.setCantidad(0);
        p1.setEnStock(false);
        final Producto savedP = productoRepository.save(p1);
        final String savedPId = savedP.getId();

        Kit k1 = new Kit();
        k1.setNombre("Kit Anticaída " + suffix);
        k1.setPrecio(85000.0);
        k1.setCantidad(0);
        k1.setEnStock(false);
        final Kit savedK = kitRepository.save(k1);
        final String savedKId = savedK.getId();

        try {
            ResponseEntity<Map<String, Object>> resp = dashboardController.obtenerResumen();
            Map<String, Object> body = resp.getBody();
            assertNotNull(body);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> alertas = (List<Map<String, Object>>) body.get("alertasAtencion");
            assertNotNull(alertas);

            Map<String, Object> alertaAgotado = alertas.stream()
                    .filter(a -> "agotado".equals(a.get("tipo")))
                    .findFirst()
                    .orElse(null);

            assertNotNull(alertaAgotado);
            assertTrue(((Number) alertaAgotado.get("cantidad")).intValue() >= 2);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) alertaAgotado.get("items");
            assertNotNull(items);
            assertTrue(items.size() >= 2);

            boolean hasProd = items.stream().anyMatch(it -> savedPId.equals(it.get("id")) && "Producto".equalsIgnoreCase(String.valueOf(it.get("tipo"))));
            boolean hasKit = items.stream().anyMatch(it -> savedKId.equals(it.get("id")) && "Kit".equalsIgnoreCase(String.valueOf(it.get("tipo"))));

            assertTrue(hasProd, "El producto agotado debe estar en los ítems de la alerta");
            assertTrue(hasKit, "El kit agotado debe estar en los ítems de la alerta");
        } finally {
            productoRepository.delete(savedP);
            kitRepository.delete(savedK);
        }
    }

    @Test
    @DisplayName("3. Alerta de Solicitud de Cancelación incluye targetId, código, motivo y datos de acción")
    public void testAlertaSolicitudCancelacionActionable() {
        String suffix = "-cancel-" + System.currentTimeMillis();
        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-CANC" + suffix);
        r.setNombreCliente("Cliente Cancelación " + suffix);
        r.setTelefono("3005551234");
        r.setFechaCita(hoy.plusDays(2));
        r.setHoraCita("10:00 AM");
        r.setEstado("Solicitud Cancelación");
        r.setMotivoCancelacion("Viaje imprevisto de trabajo");
        final Reserva savedR = reservaRepository.save(r);
        final String savedRId = savedR.getId();

        try {
            ResponseEntity<Map<String, Object>> resp = dashboardController.obtenerResumen();
            Map<String, Object> body = resp.getBody();
            assertNotNull(body);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> alertas = (List<Map<String, Object>>) body.get("alertasAtencion");
            assertNotNull(alertas);

            Map<String, Object> alertaCancel = alertas.stream()
                    .filter(a -> "solicitud_cancelacion".equals(a.get("tipo")))
                    .findFirst()
                    .orElse(null);

            assertNotNull(alertaCancel, "Debe existir alerta de solicitud_cancelacion");
            assertEquals("RESERVA", alertaCancel.get("entidad"));
            assertEquals("bookings", alertaCancel.get("modulo"));
            assertEquals("solicitud_cancelacion", alertaCancel.get("filtro"));
            assertEquals("DETALLE_CANCELACION", alertaCancel.get("accion"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) alertaCancel.get("items");
            assertNotNull(items);
            assertTrue(items.stream().anyMatch(it -> savedRId.equals(it.get("id")) && "Viaje imprevisto de trabajo".equals(it.get("motivoCancelacion"))));
        } finally {
            reservaRepository.delete(savedR);
        }
    }

    @Test
    @DisplayName("4. Alerta de Pedidos Listos para Recoger y Pedidos por Preparar son accionables")
    public void testAlertasPedidosAccionables() {
        String suffix = "-ord-" + System.currentTimeMillis();

        // 1 Pedido listo para recoger
        Reserva pListo = new Reserva();
        pListo.setCodigoReserva("ISV-ORDLST" + suffix);
        pListo.setNombreCliente("Cliente Listo " + suffix);
        pListo.setTelefono("3001239988");
        pListo.setTipoEntrega("pickup");
        pListo.setEstado("Listo para recoger");
        pListo.setFechaRegistro(hoy);
        final Reserva savedListo = reservaRepository.save(pListo);

        // 1 Pedido pagado por preparar
        Reserva pPrep = new Reserva();
        pPrep.setCodigoReserva("ISV-ORDPRP" + suffix);
        pPrep.setNombreCliente("Cliente Prep " + suffix);
        pPrep.setTelefono("3001239977");
        pPrep.setTipoEntrega("delivery");
        pPrep.setEstado("Pago Confirmado");
        pPrep.setFechaRegistro(hoy);
        final Reserva savedPrep = reservaRepository.save(pPrep);

        try {
            ResponseEntity<Map<String, Object>> resp = dashboardController.obtenerResumen();
            Map<String, Object> body = resp.getBody();
            assertNotNull(body);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> alertas = (List<Map<String, Object>>) body.get("alertasAtencion");
            assertNotNull(alertas);

            Map<String, Object> alertaListo = alertas.stream()
                    .filter(a -> "pedido_listo".equals(a.get("tipo")))
                    .findFirst()
                    .orElse(null);

            assertNotNull(alertaListo, "Debe existir alerta de pedido_listo");
            assertEquals("PEDIDO", alertaListo.get("entidad"));
            assertEquals("orders", alertaListo.get("modulo"));
            assertEquals("LISTO PARA RECOGER", alertaListo.get("filtro"));
            assertEquals("DETALLE_PEDIDO", alertaListo.get("accion"));

            Map<String, Object> alertaPrep = alertas.stream()
                    .filter(a -> "pedido_por_preparar".equals(a.get("tipo")))
                    .findFirst()
                    .orElse(null);

            assertNotNull(alertaPrep, "Debe existir alerta de pedido_por_preparar");
            assertEquals("PEDIDO", alertaPrep.get("entidad"));
            assertEquals("orders", alertaPrep.get("modulo"));
            assertEquals("PAGO CONFIRMADO", alertaPrep.get("filtro"));
            assertEquals("PREPARAR_PEDIDO", alertaPrep.get("accion"));
        } finally {
            reservaRepository.delete(savedListo);
            reservaRepository.delete(savedPrep);
        }
    }

    @Test
    @DisplayName("5. Reposición de stock elimina la alerta de agotados reactivamente")
    public void testReposicionStockEliminaAlerta() {
        String suffix = "-restock-" + System.currentTimeMillis();
        Producto p = new Producto();
        p.setNombre("Ampolla Capilar " + suffix);
        p.setPrecio(25000.0);
        p.setCantidad(0);
        p.setEnStock(false);
        final Producto savedP = productoRepository.save(p);
        final String savedId = savedP.getId();

        try {
            // Verificar que la alerta existe
            ResponseEntity<Map<String, Object>> resp1 = dashboardController.obtenerResumen();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> alertas1 = (List<Map<String, Object>>) resp1.getBody().get("alertasAtencion");
            boolean tieneAlerta = alertas1.stream().anyMatch(a -> "agotado".equals(a.get("tipo")));
            assertTrue(tieneAlerta);

            // Reponer stock
            savedP.setCantidad(10);
            savedP.setEnStock(true);
            productoRepository.save(savedP);

            // Verificar que el producto específico ya no figura como agotado
            ResponseEntity<Map<String, Object>> resp2 = dashboardController.obtenerResumen();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> alertas2 = (List<Map<String, Object>>) resp2.getBody().get("alertasAtencion");
            Map<String, Object> alertaAgotado = alertas2.stream()
                    .filter(a -> "agotado".equals(a.get("tipo")))
                    .findFirst()
                    .orElse(null);

            if (alertaAgotado != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) alertaAgotado.get("items");
                assertFalse(items.stream().anyMatch(it -> savedId.equals(it.get("id"))),
                        "El producto reabastecido ya no debe figurar en la lista de agotados");
            }
        } finally {
            productoRepository.delete(savedP);
        }
    }

    @Test
    @DisplayName("6. Seguridad: Dashboard requiere autorización administrativa con Bearer JWT")
    public void testSeguridadDashboard() throws Exception {
        // Sin token -> 401
        mockMvc.perform(get("/api/dashboard/resumen"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("NO_AUTORIZADO"));

        // Con token admin -> 200 OK y alertas presentes
        mockMvc.perform(get("/api/dashboard/resumen")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertasAtencion").isArray());
    }
}
