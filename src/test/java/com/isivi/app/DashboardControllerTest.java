package com.isivi.app;

import com.isivi.app.controller.DashboardController;
import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Kit;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.KitRepository;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DashboardControllerTest {

    @Autowired
    private DashboardController dashboardController;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private KitRepository kitRepository;

    private LocalDate hoy;
    private LocalDate ayer;

    @BeforeEach
    public void setup() {
        hoy = LocalDate.now(ZoneId.of("America/Bogota"));
        ayer = hoy.minusDays(1);
        LocalDateTime morningToday = LocalDateTime.of(hoy, LocalTime.of(8, 0));
        dashboardController.setClock(Clock.fixed(morningToday.atZone(ZoneId.of("America/Bogota")).toInstant(), ZoneId.of("America/Bogota")));
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() {
        dashboardController.setClock(Clock.system(ZoneId.of("America/Bogota")));
    }

    @Test
    @DisplayName("1. Dashboard: Citas de Hoy y Desglose contextual real")
    public void testCitasHoyYDesglose() {
        String testSuffix = "-citas-" + System.currentTimeMillis();

        // 1 cita confirmada hoy
        Reserva r1 = new Reserva();
        r1.setCodigoReserva("ISV-CONF" + testSuffix);
        r1.setNombreCliente("Cliente Confirmed");
        r1.setTelefono("3001112233");
        r1.setFechaCita(hoy);
        r1.setHoraCita("09:00 AM");
        r1.setEstado("Confirmado");
        r1.setEstadoPago("APROBADO");
        reservaRepository.save(r1);

        // 1 cita pendiente comprobante hoy
        Reserva r2 = new Reserva();
        r2.setCodigoReserva("ISV-PEND" + testSuffix);
        r2.setNombreCliente("Cliente Pending");
        r2.setTelefono("3001112244");
        r2.setFechaCita(hoy);
        r2.setHoraCita("11:00 AM");
        r2.setEstado("Pendiente Comprobante");
        reservaRepository.save(r2);

        ResponseEntity<Map<String, Object>> resp = dashboardController.obtenerResumen();
        assertEquals(200, resp.getStatusCode().value());
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);

        assertTrue(((Number) body.get("citasHoy")).longValue() >= 2);

        @SuppressWarnings("unchecked")
        Map<String, Object> desglose = (Map<String, Object>) body.get("citasHoyDesglose");
        assertNotNull(desglose);
        assertTrue(((Number) desglose.get("confirmadas")).longValue() >= 1);
        assertTrue(((Number) desglose.get("pendientes")).longValue() >= 1);

        // Limpieza
        reservaRepository.delete(r1);
        reservaRepository.delete(r2);
    }

    @Test
    @DisplayName("2. Dashboard: Ventas reales hoy excluyendo rechazados y pendientes")
    public void testVentasFinancierasReales() {
        String testSuffix = "-fin-" + System.currentTimeMillis();

        // Reserva APROBADA hoy (debe sumar a ventasHoy)
        Reserva rAprobada = new Reserva();
        rAprobada.setCodigoReserva("ISV-APROB" + testSuffix);
        rAprobada.setNombreCliente("Cliente Pagado");
        rAprobada.setTelefono("3009998877");
        rAprobada.setFechaCita(hoy);
        rAprobada.setFechaPago(hoy);
        rAprobada.setEstado("Confirmado");
        rAprobada.setEstadoPago("APROBADO");
        rAprobada.setMontoPagoCentavos(5000000L); // $50.000 COP
        rAprobada.setAnticipo(50000.0);
        reservaRepository.save(rAprobada);

        // Reserva RECHAZADA hoy (NO debe sumar)
        Reserva rRechazada = new Reserva();
        rRechazada.setCodigoReserva("ISV-RECH" + testSuffix);
        rRechazada.setNombreCliente("Cliente Rechazado");
        rRechazada.setTelefono("3009998866");
        rRechazada.setFechaCita(hoy);
        rRechazada.setFechaPago(hoy);
        rRechazada.setEstado("Denegada");
        rRechazada.setEstadoPago("RECHAZADO");
        rRechazada.setMontoPagoCentavos(10000000L);
        reservaRepository.save(rRechazada);

        // Reserva PENDIENTE hoy (NO debe sumar)
        Reserva rPendiente = new Reserva();
        rPendiente.setCodigoReserva("ISV-PENDPAG" + testSuffix);
        rPendiente.setNombreCliente("Cliente Pendiente");
        rPendiente.setTelefono("3009998855");
        rPendiente.setFechaCita(hoy);
        rPendiente.setEstado("Pendiente Comprobante");
        rPendiente.setEstadoPago("PENDIENTE");
        rPendiente.setAnticipo(30000.0);
        reservaRepository.save(rPendiente);

        ResponseEntity<Map<String, Object>> resp = dashboardController.obtenerResumen();
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);

        double ventasHoy = ((Number) body.get("ventasHoy")).doubleValue();
        assertTrue(ventasHoy >= 50000.0, "Ventas de hoy debe incluir al menos los $50.000 aprobados");

        @SuppressWarnings("unchecked")
        Map<String, Object> detalle = (Map<String, Object>) body.get("ventasHoyDetalle");
        assertNotNull(detalle);
        assertTrue(((Number) detalle.get("transaccionesHoy")).longValue() >= 1);
        assertTrue(((Number) detalle.get("ticketPromedioHoy")).doubleValue() > 0);

        // Limpieza
        reservaRepository.delete(rAprobada);
        reservaRepository.delete(rRechazada);
        reservaRepository.delete(rPendiente);
    }

    @Test
    @DisplayName("3. Dashboard: Desglose de Servicios vs Productos y Ticket Promedio")
    public void testServiciosVsProductosYTicketPromedio() {
        String testSuffix = "-srvprod-" + System.currentTimeMillis();

        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-MIX" + testSuffix);
        r.setNombreCliente("Cliente Mixto");
        r.setTelefono("3112223344");
        r.setFechaCita(hoy);
        r.setFechaPago(hoy);
        r.setEstado("Confirmado");
        r.setEstadoPago("APROBADO");
        r.setSubtotal(120000.0);
        r.setAnticipo(120000.0);
        r.setMontoPagoCentavos(12000000L);

        List<ItemReserva> items = new ArrayList<>();
        ItemReserva itemServ = new ItemReserva();
        itemServ.setTipo("servicio");
        itemServ.setNombre("Corte y Cepillado");
        itemServ.setPrecioUnitario(70000.0);
        itemServ.setCantidad(1);
        itemServ.setSubtotal(70000.0);
        items.add(itemServ);

        ItemReserva itemProd = new ItemReserva();
        itemProd.setTipo("producto");
        itemProd.setNombre("Shampoo Reparador");
        itemProd.setPrecioUnitario(50000.0);
        itemProd.setCantidad(1);
        itemProd.setSubtotal(50000.0);
        items.add(itemProd);

        r.setItemsInventario(items);
        reservaRepository.save(r);

        ResponseEntity<Map<String, Object>> resp = dashboardController.obtenerResumen();
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);

        @SuppressWarnings("unchecked")
        Map<String, Object> detalle = (Map<String, Object>) body.get("ventasHoyDetalle");
        assertNotNull(detalle);
        assertTrue(((Number) detalle.get("productosHoy")).doubleValue() >= 50000.0);
        assertTrue(((Number) detalle.get("serviciosHoy")).doubleValue() >= 70000.0);

        reservaRepository.delete(r);
    }

    @Test
    @DisplayName("4. Dashboard: Wompi Aprobado NO es pendiente, Transferencia Pendiente SÍ es pendiente")
    public void testPendientesDesgloseReglasNegocio() {
        String testSuffix = "-pendrules-" + System.currentTimeMillis();

        // 1. Wompi Aprobado -> Confirmado (NO debe estar en pendientes)
        Reserva rWompi = new Reserva();
        rWompi.setCodigoReserva("ISV-WOMPI-OK" + testSuffix);
        rWompi.setNombreCliente("Wompi Aprobado");
        rWompi.setTelefono("3201112233");
        rWompi.setMedioPago("WOMPI");
        rWompi.setEstadoPago("APROBADO");
        rWompi.setEstado("Confirmado");
        reservaRepository.save(rWompi);

        // 2. Transferencia Pendiente Comprobante -> SÍ debe estar en pendientes
        Reserva rTransf = new Reserva();
        rTransf.setCodigoReserva("ISV-TRANSF-PEND" + testSuffix);
        rTransf.setNombreCliente("Transferencia Pendiente");
        rTransf.setTelefono("3201112244");
        rTransf.setMedioPago("TRANSFERENCIA");
        rTransf.setEstado("Pendiente Comprobante");
        reservaRepository.save(rTransf);

        // 3. Reprogramación Pendiente -> SÍ debe estar en pendientes
        Reserva rReprog = new Reserva();
        rReprog.setCodigoReserva("ISV-REPROG-PEND" + testSuffix);
        rReprog.setNombreCliente("Reprogramación Pendiente");
        rReprog.setTelefono("3201112255");
        rReprog.setEstado("Pendiente Reprogramación");
        reservaRepository.save(rReprog);

        ResponseEntity<Map<String, Object>> resp = dashboardController.obtenerResumen();
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);

        @SuppressWarnings("unchecked")
        Map<String, Object> pendientesDesglose = (Map<String, Object>) body.get("pendientesDesglose");
        assertNotNull(pendientesDesglose);
        assertTrue(((Number) pendientesDesglose.get("comprobantes")).longValue() >= 1);
        assertTrue(((Number) pendientesDesglose.get("reprogramaciones")).longValue() >= 1);

        @SuppressWarnings("unchecked")
        List<Reserva> listaPendientes = (List<Reserva>) body.get("pendientesLista");
        boolean wompiInPending = listaPendientes.stream().anyMatch(res -> ("ISV-WOMPI-OK" + testSuffix).equals(res.getCodigoReserva()));
        assertFalse(wompiInPending, "Wompi Aprobado nunca debe listarse en pendientes");

        reservaRepository.delete(rWompi);
        reservaRepository.delete(rTransf);
        reservaRepository.delete(rReprog);
    }

    @Test
    @DisplayName("4b. Dashboard: Wompi Pendiente Pago is strictly excluded from pending list and counts")
    public void testWompiPendientePagoExcluido() {
        String testSuffix = "-wompipend-" + System.currentTimeMillis();

        Reserva rWompiPendiente = new Reserva();
        rWompiPendiente.setCodigoReserva("ISV-WOMPI-PEND" + testSuffix);
        rWompiPendiente.setNombreCliente("Wompi Pendiente");
        rWompiPendiente.setTelefono("3209990000");
        rWompiPendiente.setMedioPago("WOMPI");
        rWompiPendiente.setEstadoPago("PENDIENTE");
        rWompiPendiente.setEstado("Pendiente Pago");
        reservaRepository.save(rWompiPendiente);

        ResponseEntity<Map<String, Object>> resp = dashboardController.obtenerResumen();
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);

        @SuppressWarnings("unchecked")
        List<Reserva> listaPendientes = (List<Reserva>) body.get("pendientesLista");
        boolean isPresent = listaPendientes.stream().anyMatch(res -> rWompiPendiente.getCodigoReserva().equals(res.getCodigoReserva()));
        assertFalse(isPresent, "Wompi pendiente de pago debe estar excluido de la bandeja operativa de acciones");

        reservaRepository.delete(rWompiPendiente);
    }

    @Test
    @DisplayName("5. Dashboard: Stock Bajo y Alertas de Agotados")
    public void testStockBajoDesgloseYAleras() {
        String testSuffix = "-stock-" + System.currentTimeMillis();

        Producto pAgotado = new Producto();
        pAgotado.setNombre("Producto Agotado Test " + testSuffix);
        pAgotado.setPrecio(45000.0);
        pAgotado.setCantidad(0);
        pAgotado.setEnStock(false);
        productoRepository.save(pAgotado);

        Producto pPorReponer = new Producto();
        pPorReponer.setNombre("Producto Bajo Test " + testSuffix);
        pPorReponer.setPrecio(35000.0);
        pPorReponer.setCantidad(2);
        pPorReponer.setEnStock(true);
        productoRepository.save(pPorReponer);

        ResponseEntity<Map<String, Object>> resp = dashboardController.obtenerResumen();
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);

        @SuppressWarnings("unchecked")
        Map<String, Object> stockDesglose = (Map<String, Object>) body.get("stockBajoDesglose");
        assertNotNull(stockDesglose);
        assertTrue(((Number) stockDesglose.get("agotados")).longValue() >= 1);
        assertTrue(((Number) stockDesglose.get("porReponer")).longValue() >= 1);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> alertas = (List<Map<String, Object>>) body.get("alertasAtencion");
        assertNotNull(alertas);
        boolean tieneAlertaAgotado = alertas.stream().anyMatch(a -> "agotado".equals(a.get("tipo")));
        assertTrue(tieneAlertaAgotado, "Debe existir alerta de productos agotados si hay existencias en 0");

        productoRepository.delete(pAgotado);
        productoRepository.delete(pPorReponer);
    }

    @Test
    @DisplayName("6. Dashboard: Historial 7 Días contiene exactamente 7 días cronológicos")
    public void testVentas7Dias() {
        ResponseEntity<Map<String, Object>> resp = dashboardController.obtenerResumen();
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> ventas7Dias = (List<Map<String, Object>>) body.get("ventas7Dias");
        assertNotNull(ventas7Dias);
        assertEquals(7, ventas7Dias.size());

        for (Map<String, Object> dia : ventas7Dias) {
            assertNotNull(dia.get("fecha"));
            assertNotNull(dia.get("dia"));
            assertNotNull(dia.get("total"));
        }
    }

    @Test
    @DisplayName("7. Wompi DECLINED/ERROR/VOIDED status is strictly excluded from administrative action tray and alerts list")
    public void testWompiDeclinedErrorVoidedExcludedFromActionTray() {
        String testSuffix = "-wompirej-" + System.currentTimeMillis();

        // 1. Reserva Wompi DECLINED
        Reserva rDeclined = new Reserva();
        rDeclined.setCodigoReserva("ISV-DEC-" + testSuffix);
        rDeclined.setNombreCliente("Wompi Declined Client");
        rDeclined.setTelefono("3000000001");
        rDeclined.setMedioPago("WOMPI");
        rDeclined.setEstadoPago("DECLINED");
        rDeclined.setEstado("Pendiente Pago");
        reservaRepository.save(rDeclined);

        // 2. Reserva Wompi ERROR
        Reserva rError = new Reserva();
        rError.setCodigoReserva("ISV-ERR-" + testSuffix);
        rError.setNombreCliente("Wompi Error Client");
        rError.setTelefono("3000000002");
        rError.setMedioPago("WOMPI");
        rError.setEstadoPago("ERROR");
        rError.setEstado("Pendiente Pago");
        reservaRepository.save(rError);

        // 3. Reserva Wompi VOIDED
        Reserva rVoided = new Reserva();
        rVoided.setCodigoReserva("ISV-VOID-" + testSuffix);
        rVoided.setNombreCliente("Wompi Voided Client");
        rVoided.setTelefono("3000000003");
        rVoided.setMedioPago("WOMPI");
        rVoided.setEstadoPago("VOIDED");
        rVoided.setEstado("Pendiente Pago");
        reservaRepository.save(rVoided);

        ResponseEntity<Map<String, Object>> resp = dashboardController.obtenerResumen();
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> alertas = (List<Map<String, Object>>) body.get("alertasAtencion");
        assertNotNull(alertas);

        // Verify that no alert with type "pago_rechazado" is generated
        boolean hasRejectAlert = alertas.stream().anyMatch(a -> "pago_rechazado".equals(a.get("tipo")));
        assertFalse(hasRejectAlert, "La bandeja operativa no debe incluir ninguna alerta de intentos de pago rechazados para Wompi");

        // Verify that every generated alert has a valid target action
        for (Map<String, Object> a : alertas) {
            assertNotNull(a.get("modulo"), "Toda alerta de atención debe tener un módulo de destino válido");
            assertNotNull(a.get("accion"), "Toda alerta de atención debe tener una acción válida");
        }

        reservaRepository.delete(rDeclined);
        reservaRepository.delete(rError);
        reservaRepository.delete(rVoided);
    }

    @Test
    @DisplayName("8. WhatsApp PENDIENTE_COMPROBANTE is correctly preserved in action tray and alerts list")
    public void testWhatsAppPendingComprobanteInActionTray() {
        String testSuffix = "-wapend-" + System.currentTimeMillis();

        Reserva rWhatsApp = new Reserva();
        rWhatsApp.setCodigoReserva("ISV-WA-" + testSuffix);
        rWhatsApp.setNombreCliente("WhatsApp Pending Client");
        rWhatsApp.setTelefono("3000000009");
        rWhatsApp.setEmail("wa@example.com");
        rWhatsApp.setMedioPago("TRANSFERENCIA");
        rWhatsApp.setEstado("Pendiente Comprobante");
        reservaRepository.save(rWhatsApp);

        ResponseEntity<Map<String, Object>> resp = dashboardController.obtenerResumen();
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> alertas = (List<Map<String, Object>>) body.get("alertasAtencion");
        assertNotNull(alertas);

        boolean hasComprobanteAlert = alertas.stream().anyMatch(a -> "comprobante".equals(a.get("tipo")));
        assertTrue(hasComprobanteAlert, "Las reservas de transferencia en Pendiente Comprobante deben generar alerta operativa");

        reservaRepository.delete(rWhatsApp);
    }
}
