package com.isivi.app;

import com.isivi.app.controller.DashboardController;
import com.isivi.app.controller.ReservaController;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.service.ReservaService;
import com.isivi.app.service.WhatsAppNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "isivi.wompi.public-key=pub_test_QWERTY12345",
        "isivi.wompi.private-key=prv_test_SECRET98765",
        "isivi.wompi.integrity-secret=test_integrity_SECRET123",
        "isivi.wompi.events-secret=test_events_SECRET456",
        "isivi.wompi.sandbox=true"
})
public class WompiRejectionAndOperationalFlowTest {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private DashboardController dashboardController;

    @MockBean
    private WhatsAppNotificationService whatsAppNotificationService;

    @BeforeEach
    void setUp() {
        reservaRepository.deleteAll();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        reservaRepository.deleteAll();
    }

    private Reserva crearPedidoBase(String codigo, String estado, String estadoPago) {
        Reserva r = new Reserva();
        r.setCodigoReserva(codigo);
        r.setNombreCliente("Cliente Prueba");
        r.setTelefono("3001234567");
        r.setFechaCita(null);
        r.setHoraCita(null);
        r.setSubtotal(120000.0);
        r.setAnticipo(30000.0);
        r.setEstado(estado);
        r.setEstadoPago(estadoPago);
        r.setFechaRegistro(LocalDate.now());
        r.setTipoEntrega("pickup"); // Define como pedido puro
        
        com.isivi.app.model.ItemReserva item = new com.isivi.app.model.ItemReserva();
        item.setTipo("producto");
        item.setCantidad(1);
        item.setPrecioUnitario(120000.0);
        r.getItemsInventario().add(item);
        
        return r;
    }

    @Test
    @DisplayName("PRUEBA 1: Wompi APPROVED ingresa correctamente al flujo operativo como Pago Confirmado")
    void testWompiAprobadoEntraAOperaciones() {
        Reserva r = crearPedidoBase("ISV-APP-1", "Pago Confirmado", "APPROVED");
        reservaRepository.save(r);

        List<Reserva> activos = reservaController.listar(null, false, "pedidos");
        assertEquals(1, activos.size(), "El pedido aprobado debe aparecer en el listado operativo");
        assertEquals("ISV-APP-1", activos.get(0).getCodigoReserva());

        Map<String, Object> resumen = dashboardController.obtenerResumen().getBody();
        Map<String, Object> desglose = (Map<String, Object>) resumen.get("pedidosActivosDesglose");
        assertNotNull(desglose);
        assertEquals(1L, ((Number) desglose.get("pagoConfirmado")).longValue());
    }

    @Test
    @DisplayName("PRUEBA 2: Wompi DECLINED no aparece en la bandeja operativa de pedidos activos")
    void testWompiDeclinedNoEntraAOperaciones() {
        Reserva r = crearPedidoBase("ISV-DEC-1", "Pendiente Pago", "DECLINED");
        reservaRepository.save(r);

        List<Reserva> activos = reservaController.listar(null, false, "pedidos");
        assertTrue(activos.isEmpty(), "Un pedido con pago DECLINED no debe aparecer en los pedidos operativos activos");
    }

    @Test
    @DisplayName("PRUEBA 3: Wompi VOIDED no aparece en la bandeja operativa de pedidos activos")
    void testWompiVoidedNoEntraAOperaciones() {
        Reserva r = crearPedidoBase("ISV-VOI-1", "Pendiente Pago", "VOIDED");
        reservaRepository.save(r);

        List<Reserva> activos = reservaController.listar(null, false, "pedidos");
        assertTrue(activos.isEmpty(), "Un pedido con pago VOIDED no debe aparecer en los pedidos operativos activos");
    }

    @Test
    @DisplayName("PRUEBA 4: Wompi ERROR no aparece en la bandeja operativa de pedidos activos")
    void testWompiErrorNoEntraAOperaciones() {
        Reserva r = crearPedidoBase("ISV-ERR-1", "Pendiente Pago", "ERROR");
        reservaRepository.save(r);

        List<Reserva> activos = reservaController.listar(null, false, "pedidos");
        assertTrue(activos.isEmpty(), "Un pedido con pago ERROR no debe aparecer en los pedidos operativos activos");
    }

    @Test
    @DisplayName("PRUEBA 5: Reintento de pago aprobado transiciona de Pendiente Pago a Pago Confirmado y entra al flujo operativo")
    void testReintentoPagoAprobado() {
        Reserva r = crearPedidoBase("ISV-RETRY-1", "Pendiente Pago", "DECLINED");
        reservaRepository.save(r);

        // Simular reintento aprobado
        r.setEstado("Pago Confirmado");
        r.setEstadoPago("APPROVED");
        reservaRepository.save(r);

        List<Reserva> activos = reservaController.listar(null, false, "pedidos");
        assertEquals(1, activos.size(), "El pedido reintentado y aprobado debe entrar al flujo operativo");
        assertEquals("ISV-RETRY-1", activos.get(0).getCodigoReserva());
    }

    @Test
    @DisplayName("PRUEBA 6: Reintento de pago fallido permanece fuera de los pedidos operativos")
    void testReintentoPagoFallidoSigueFuera() {
        Reserva r = crearPedidoBase("ISV-RETRY-2", "Pendiente Pago", "DECLINED");
        reservaRepository.save(r);

        // Simular nuevo intento fallido (cambia a ERROR)
        r.setEstadoPago("ERROR");
        reservaRepository.save(r);

        List<Reserva> activos = reservaController.listar(null, false, "pedidos");
        assertTrue(activos.isEmpty(), "El pedido reintentado fallido debe seguir fuera de la bandeja activa");
    }

    @Test
    @DisplayName("PRUEBA 7: Pagos rechazados no generan alerta de preparación ni alerta financiera en Atención Ahora")
    void testAlertasPagoRechazado() {
        Reserva r = crearPedidoBase("ISV-ALERT-1", "Pendiente Pago", "DECLINED");
        reservaRepository.save(r);

        Map<String, Object> resumen = dashboardController.obtenerResumen().getBody();
        List<Map<String, Object>> alertas = (List<Map<String, Object>>) resumen.get("alertasAtencion");
        assertNotNull(alertas);

        boolean tieneAlertaFinanciera = alertas.stream().anyMatch(a -> "alert-pago-rechazado".equals(a.get("id")));
        boolean tieneAlertaLogistica = alertas.stream().anyMatch(a -> "alert-pedidos-preparar".equals(a.get("id")));

        assertFalse(tieneAlertaFinanciera, "NO debe generar una alerta financiera de Pago Rechazado en Atención Ahora");
        assertFalse(tieneAlertaLogistica, "NO debe generar alerta logística de preparación para pedidos rechazados");
    }

    @Test
    @DisplayName("PRUEBA 8: Las métricas operativas del panel excluyen de forma estricta los pagos fallidos o pendientes de pago de Wompi")
    void testMetricasOperativasExcluyenFallidos() {
        Reserva r1 = crearPedidoBase("ISV-M1", "Pendiente Pago", "DECLINED");
        Reserva r2 = crearPedidoBase("ISV-M2", "Pendiente Pago", "VOIDED");
        Reserva r3 = crearPedidoBase("ISV-M3", "Pago Confirmado", "APPROVED");
        reservaRepository.saveAll(List.of(r1, r2, r3));

        Map<String, Object> resumen = dashboardController.obtenerResumen().getBody();
        Map<String, Object> desglose = (Map<String, Object>) resumen.get("pedidosActivosDesglose");
        assertNotNull(desglose);

        long totalActivos = ((Number) desglose.get("total")).longValue();
        assertEquals(1L, totalActivos, "El total de activos operativos sólo debe contar el pedido aprobado");
    }

    @Test
    @DisplayName("PRUEBA 9: PENDIENTE_PAGO no es terminal irreversible (sigue siendo reintentable y no va a historial automáticamente)")
    void testPendientePagoNoEsTerminal() {
        Reserva r = crearPedidoBase("ISV-TERM-1", "Pendiente Pago", "DECLINED");
        reservaRepository.save(r);

        boolean esTerminal = (boolean) ReflectionTestUtils.invokeMethod(reservaController, "esTerminal", r);
        assertFalse(esTerminal, "Un pedido Pendiente Pago con rechazo de Wompi NO debe ser terminal irreversible para permitir reintentos");
    }

    @Test
    @DisplayName("PRUEBA 10: RECOGIDO sí es terminal operativo, sale de activos y pasa al historial")
    void testRecogidoEsTerminalYVaAHistorial() {
        Reserva r = crearPedidoBase("ISV-REC-1", "Recogido", "APPROVED");
        reservaRepository.save(r);

        boolean esTerminal = (boolean) ReflectionTestUtils.invokeMethod(reservaController, "esTerminal", r);
        assertTrue(esTerminal, "El estado Recogido DEBE ser terminal");

        List<Reserva> activos = reservaController.listar(null, false, "pedidos");
        List<Reserva> historicos = reservaController.listar(null, true, "pedidos");

        assertTrue(activos.isEmpty(), "Un pedido Recogido no debe estar en activos");
        assertEquals(1, historicos.size(), "Un pedido Recogido debe pasar al historial de pedidos");
    }

    @Test
    @DisplayName("PRUEBA 11: PENDIENTE_COMPROBANTE sí aparece como tarea administrativa y en alertas")
    void testPendienteComprobanteEsTareaAdmin() {
        Reserva r = crearPedidoBase("ISV-TRANS-1", "Pendiente Comprobante", "PENDIENTE");
        reservaRepository.save(r);

        List<Reserva> activos = reservaController.listar(null, false, "pedidos");
        assertEquals(1, activos.size(), "Un pedido de transferencia en Pendiente Comprobante debe ser visible para el admin");

        Map<String, Object> resumen = dashboardController.obtenerResumen().getBody();
        List<Map<String, Object>> alertas = (List<Map<String, Object>>) resumen.get("alertasAtencion");
        assertNotNull(alertas);

        boolean tieneAlertaValidacion = alertas.stream().anyMatch(a -> "alert-comp".equals(a.get("id")));
        assertTrue(tieneAlertaValidacion, "Debe generar una alerta de validación de comprobante");
    }

    @Test
    @DisplayName("PRUEBA 12: Confirmar comprobante de transferencia pasa el pedido a Pago Confirmado")
    void testConfirmarComprobante() {
        Reserva r = crearPedidoBase("ISV-TRANS-2", "Pendiente Comprobante", "PENDIENTE");
        reservaRepository.save(r);

        reservaController.aprobar(r.getId());

        Reserva actualizada = reservaRepository.findById(r.getId()).orElse(null);
        assertNotNull(actualizada);
        assertEquals("Pago Confirmado", actualizada.getEstado(), "El pedido debe pasar a Pago Confirmado al aprobar el comprobante");
        assertEquals("APROBADO", actualizada.getEstadoPago(), "El estado del pago debe ser APROBADO");
    }

    @Test
    @DisplayName("PRUEBA 13: Las ventas del dashboard requieren estricta coherencia (pago aprobado + estado operativo)")
    void testVentaValidaCoherencia() {
        // Venta 1: Aprobado + Estado Operativo
        Reserva r1 = crearPedidoBase("ISV-V1", "Pago Confirmado", "APPROVED");
        // Venta 2: Aprobado pero Cancelada (No operativa)
        Reserva r2 = crearPedidoBase("ISV-V2", "Cancelada", "APPROVED");
        // Venta 3: Pendiente Pago (No operativa, no pagado)
        Reserva r3 = crearPedidoBase("ISV-V3", "Pendiente Pago", "DECLINED");
        // Venta 4: Pendiente Comprobante (No operativa)
        Reserva r4 = crearPedidoBase("ISV-V4", "Pendiente Comprobante", "PENDIENTE");

        reservaRepository.saveAll(List.of(r1, r2, r3, r4));

        boolean v1Valida = (boolean) ReflectionTestUtils.invokeMethod(dashboardController, "esVentaValida", r1);
        boolean v2Valida = (boolean) ReflectionTestUtils.invokeMethod(dashboardController, "esVentaValida", r2);
        boolean v3Valida = (boolean) ReflectionTestUtils.invokeMethod(dashboardController, "esVentaValida", r3);
        boolean v4Valida = (boolean) ReflectionTestUtils.invokeMethod(dashboardController, "esVentaValida", r4);

        assertTrue(v1Valida, "Venta aprobada y operativa debe ser válida");
        assertFalse(v2Valida, "Venta cancelada debe ser inválida");
        assertFalse(v3Valida, "Pendiente de pago Wompi debe ser inválido");
        assertFalse(v4Valida, "Pendiente comprobante transferencia debe ser inválido");
    }
}
