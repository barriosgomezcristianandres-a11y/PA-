package com.isivi.app;

import com.isivi.app.controller.DashboardController;
import com.isivi.app.controller.ReservaController;
import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderWorkflowTest {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private DashboardController dashboardController;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    private Producto testProducto;
    private Authentication mockAuth;

    @BeforeEach
    public void setup() {
        mockAuth = new UsernamePasswordAuthenticationToken("admin", "pass");
        testProducto = new Producto();
        testProducto.setNombre("Óleo de Argán Test " + System.currentTimeMillis());
        testProducto.setCategoriaId("oleos");
        testProducto.setPrecio(65000.0);
        testProducto.setCantidad(20);
        testProducto.setEnStock(true);
        testProducto = productoRepository.save(testProducto);
    }


    @Test
    @DisplayName("1. Ciclo completo de vida de un pedido puro: Pago Confirmado -> En Preparación -> Listo para Recoger -> Entregado")
    public void testOrderLifecycleTransitions() {
        // Crear un pedido de producto puro
        Reserva order = new Reserva();
        order.setCodigoReserva("ISV-ORD-" + System.currentTimeMillis());
        order.setNombreCliente("Laura Pedidos");
        order.setEmail("laura@ejemplo.com");
        order.setTelefono("3009988776");
        order.setMedioPago("WOMPI");
        order.setEstado(ReservaService.PAGO_CONFIRMADO);
        order.setEstadoPago("APROBADO");
        order.setSubtotal(130000.0);
        order.setAnticipo(130000.0);
        order.setSaldo(0.0);

        ItemReserva item = new ItemReserva(testProducto.getId(), "producto", 2);
        item.setNombre(testProducto.getNombre());
        item.setPrecioUnitario(65000.0);
        item.setSubtotal(130000.0);
        order.getItemsInventario().add(item);
        order.getItems().add(testProducto.getNombre() + " (x2)");

        order = reservaRepository.save(order);
        assertTrue(order.esPedidoPuro(), "Debe ser reconocido como un pedido puro");
        assertFalse(order.esCita(), "No debe ser catalogado como cita");

        // 1. Transición a EN PREPARACIÓN
        ResponseEntity<?> prepResp = reservaController.marcarPedidoEnPreparacion(order.getId(), mockAuth);
        assertEquals(200, prepResp.getStatusCode().value());
        Reserva inPrep = reservaRepository.findById(order.getId()).orElseThrow();
        assertEquals(ReservaService.PAGO_CONFIRMADO, inPrep.getEstado());
        assertEquals("EN_PREPARACION", inPrep.getEstadoPedido());
        assertNotNull(inPrep.getFechaEnPreparacion(), "Debe registrar fecha de paso a preparación");

        // 2. Transición a LISTO PARA RECOGER
        ResponseEntity<?> readyResp = reservaController.marcarPedidoListoParaRecoger(order.getId(), mockAuth);
        assertEquals(200, readyResp.getStatusCode().value());
        Reserva ready = reservaRepository.findById(order.getId()).orElseThrow();
        assertEquals(ReservaService.PAGO_CONFIRMADO, ready.getEstado());
        assertEquals("LISTO_RECOGER", ready.getEstadoPedido());
        assertNotNull(ready.getFechaListoParaRecoger(), "Debe registrar fecha de listo para recoger");

        // 3. Transición a ENTREGADO / RECOGIDO (Pickup)
        ResponseEntity<?> deliveredResp = reservaController.marcarPedidoEntregado(order.getId(), mockAuth);
        assertEquals(200, deliveredResp.getStatusCode().value());
        Reserva delivered = reservaRepository.findById(order.getId()).orElseThrow();
        assertEquals(ReservaService.PAGO_CONFIRMADO, delivered.getEstado());
        assertEquals("RECOGIDO", delivered.getEstadoPedido());
        assertNotNull(delivered.getFechaEntregado(), "Debe registrar fecha de entrega");
        assertTrue(delivered.getArchivada(), "Los pedidos entregados deben marcarse archivados (historial)");
    }

    @Test
    @DisplayName("2. Retención de pedidos activos frente al cambio de día (NO archivar automáticamente pedidos sin entregar)")
    public void testOrdersDayChangeRetention() {
        // Pedido registrado con fecha de días anteriores pero aún no entregado
        Reserva olderOrder = new Reserva();
        olderOrder.setCodigoReserva("ISV-RET-" + System.currentTimeMillis());
        olderOrder.setNombreCliente("Carlos Retención");
        olderOrder.setEmail("carlos@ejemplo.com");
        olderOrder.setTelefono("3112233445");
        olderOrder.setMedioPago("WOMPI");
        olderOrder.setEstado(ReservaService.LISTO_PARA_RECOGER);
        olderOrder.setEstadoPago("APROBADO");
        olderOrder.setSubtotal(65000.0);
        olderOrder.setAnticipo(65000.0);
        olderOrder.setSaldo(0.0);
        olderOrder.setArchivada(false);

        // Simular fecha de registro hace 3 días
        olderOrder.setFechaRegistro(LocalDate.now(ZoneId.of("America/Bogota")).minusDays(3));

        ItemReserva item = new ItemReserva(testProducto.getId(), "producto", 1);
        item.setNombre(testProducto.getNombre());
        item.setPrecioUnitario(65000.0);
        item.setSubtotal(65000.0);
        olderOrder.getItemsInventario().add(item);
        olderOrder.getItems().add(testProducto.getNombre());

        Reserva savedOlder = reservaRepository.save(olderOrder);

        // Ejecutar listar reservas activas (que dispara archivarReservasVencidas internamente)
        List<Reserva> activeOrders = reservaController.listar("pedidos", false, null);

        // El pedido DEBE seguir activo y NO haberse archivado
        final String olderId = savedOlder.getId();
        boolean remainsActive = activeOrders.stream().anyMatch(r -> olderId.equals(r.getId()));
        assertTrue(remainsActive, "El pedido no entregado debe permanecer activo sin importar los días transcurridos");

        Reserva checkDb = reservaRepository.findById(olderId).orElseThrow();
        assertFalse(checkDb.getArchivada(), "No debe estar archivada mientras siga en LISTO_PARA_RECOGER");

    }

    @Test
    @DisplayName("3. Métricas en Dashboard: Pedidos Activos y Desglose")
    public void testDashboardOrdersMetrics() {
        String suffix = "-" + System.currentTimeMillis();

        // 1 En preparación
        Reserva o1 = new Reserva();
        o1.setCodigoReserva("ISV-P1" + suffix);
        o1.setNombreCliente("Cliente 1");
        o1.setTelefono("3000000001");
        o1.setEstado(ReservaService.EN_PREPARACION);
        o1.setEstadoPago("APROBADO");
        o1.getItems().add("Shampoo Reparador");
        reservaRepository.save(o1);

        // 1 Listo para recoger
        Reserva o2 = new Reserva();
        o2.setCodigoReserva("ISV-P2" + suffix);
        o2.setNombreCliente("Cliente 2");
        o2.setTelefono("3000000002");
        o2.setEstado(ReservaService.LISTO_PARA_RECOGER);
        o2.setEstadoPago("APROBADO");
        o2.getItems().add("Tratamiento Nutritivo");
        reservaRepository.save(o2);

        ResponseEntity<Map<String, Object>> dashResp = dashboardController.obtenerResumen();
        assertEquals(200, dashResp.getStatusCode().value());
        Map<String, Object> dash = dashResp.getBody();
        assertNotNull(dash);
        assertTrue(((Number) dash.get("pedidosActivos")).intValue() >= 2, "Debe contabilizar al menos los 2 pedidos activos creados");


        @SuppressWarnings("unchecked")
        Map<String, Object> desglose = (Map<String, Object>) dash.get("pedidosActivosDesglose");
        assertNotNull(desglose);
        assertTrue(((Number) desglose.get("enPreparacion")).intValue() >= 1);
        assertTrue(((Number) desglose.get("listosParaRecoger")).intValue() >= 1);
    }

    @Test
    @DisplayName("4. Flujo Domicilio: Pago Confirmado -> En Preparación -> Listo para Envío -> En Camino -> Entregado")
    public void testDeliveryOrderWorkflow() {
        Reserva order = new Reserva();
        order.setCodigoReserva("ISV-DOM-" + System.currentTimeMillis());
        order.setNombreCliente("Andrea Domicilio");
        order.setEmail("andrea@ejemplo.com");
        order.setTelefono("3155554433");
        order.setMedioPago("WOMPI");
        order.setEstado(ReservaService.PAGO_CONFIRMADO);
        order.setEstadoPago("APROBADO");
        order.setTipoEntrega("delivery");
        order.setDireccionEntrega("Bocagrande Carrera 3 # 6-25, Apto 501");
        order.setSubtotal(95000.0);
        order.setAnticipo(95000.0);
        order.setSaldo(0.0);

        ItemReserva item = new ItemReserva(testProducto.getId(), "producto", 1);
        item.setNombre(testProducto.getNombre());
        item.setPrecioUnitario(95000.0);
        order.getItemsInventario().add(item);
        order.getItems().add(testProducto.getNombre());

        order = reservaRepository.save(order);
        assertTrue(order.esDomicilio());
        assertFalse(order.esRecogida());
        assertEquals("PENDIENTE_PREPARACION", order.getEstadoPedido());

        // 1. En Preparación
        ResponseEntity<?> respPrep = reservaController.marcarPedidoEnPreparacion(order.getId(), mockAuth);
        assertEquals(200, respPrep.getStatusCode().value());
        Reserva inPrep = reservaRepository.findById(order.getId()).orElseThrow();
        assertEquals("EN_PREPARACION", inPrep.getEstadoPedido());
        assertEquals("APROBADO", inPrep.getEstadoPago());

        // 2. Listo para Envío
        ResponseEntity<?> respReady = reservaController.marcarPedidoListoEnvio(order.getId(), mockAuth);
        assertEquals(200, respReady.getStatusCode().value());
        Reserva readyShip = reservaRepository.findById(order.getId()).orElseThrow();
        assertEquals("LISTO_ENVIO", readyShip.getEstadoPedido());
        assertEquals(ReservaService.PAGO_CONFIRMADO, readyShip.getEstado());
        assertEquals("APROBADO", readyShip.getEstadoPago());
        assertNotNull(readyShip.getFechaListoEnvio());

        // 3. En Camino
        ResponseEntity<?> respOnWay = reservaController.marcarPedidoEnCamino(order.getId(), mockAuth);
        assertEquals(200, respOnWay.getStatusCode().value());
        Reserva onWay = reservaRepository.findById(order.getId()).orElseThrow();
        assertEquals("EN_CAMINO", onWay.getEstadoPedido());
        assertEquals(ReservaService.PAGO_CONFIRMADO, onWay.getEstado());
        assertEquals("APROBADO", onWay.getEstadoPago());
        assertNotNull(onWay.getFechaEnCamino());

        // 4. Entregado
        ResponseEntity<?> respDelivered = reservaController.marcarPedidoEntregado(order.getId(), mockAuth);
        assertEquals(200, respDelivered.getStatusCode().value());
        Reserva delivered = reservaRepository.findById(order.getId()).orElseThrow();
        assertEquals("ENTREGADO", delivered.getEstadoPedido());
        assertEquals(ReservaService.PAGO_CONFIRMADO, delivered.getEstado());
        assertEquals("APROBADO", delivered.getEstadoPago());
        assertTrue(delivered.getArchivada());
    }

    @Test
    @DisplayName("5. Rechazo estricto de transiciones incompatibles entre Domicilio y Pickup")
    public void testIncompatibleDeliveryTransitions() {
        // Pedido Delivery intentando pasar a LISTO_PARA_RECOGER debe fallar
        Reserva delivOrder = new Reserva();
        delivOrder.setCodigoReserva("ISV-DEL-ERR-" + System.currentTimeMillis());
        delivOrder.setNombreCliente("Cliente Delivery");
        delivOrder.setTelefono("3001110000");
        delivOrder.setTipoEntrega("delivery");
        delivOrder.setDireccionEntrega("Calle 10 # 20-30");
        delivOrder.setEstado(ReservaService.EN_PREPARACION);
        delivOrder.setEstadoPago("APROBADO");
        delivOrder.getItemsInventario().add(new ItemReserva(testProducto.getId(), "producto", 1));
        delivOrder = reservaRepository.save(delivOrder);

        ResponseEntity<?> badRespDeliv = reservaController.marcarPedidoListoParaRecoger(delivOrder.getId(), mockAuth);
        assertEquals(400, badRespDeliv.getStatusCode().value());

        // Pedido Pickup intentando pasar a LISTO_ENVIO o EN_CAMINO debe fallar
        Reserva pickupOrder = new Reserva();
        pickupOrder.setCodigoReserva("ISV-PICK-ERR-" + System.currentTimeMillis());
        pickupOrder.setNombreCliente("Cliente Pickup");
        pickupOrder.setTelefono("3002220000");
        pickupOrder.setTipoEntrega("pickup");
        pickupOrder.setEstado(ReservaService.EN_PREPARACION);
        pickupOrder.setEstadoPago("APROBADO");
        pickupOrder.getItemsInventario().add(new ItemReserva(testProducto.getId(), "producto", 1));
        pickupOrder = reservaRepository.save(pickupOrder);

        ResponseEntity<?> badRespPickupShip = reservaController.marcarPedidoListoEnvio(pickupOrder.getId(), mockAuth);
        assertEquals(400, badRespPickupShip.getStatusCode().value());

        ResponseEntity<?> badRespPickupWay = reservaController.marcarPedidoEnCamino(pickupOrder.getId(), mockAuth);
        assertEquals(400, badRespPickupWay.getStatusCode().value());
    }

    @Test
    @DisplayName("6. Compatibilidad retroactiva: pedidos antiguos sin campo estadoPedido resuelven estado correctamente")
    public void testLegacyOrderStateResolution() {
        Reserva legacy = new Reserva();
        legacy.setCodigoReserva("ISV-LEGACY-" + System.currentTimeMillis());
        legacy.setNombreCliente("Cliente Antiguo");
        legacy.setTelefono("3003330000");
        legacy.setTipoEntrega("pickup");
        legacy.setEstado("Listo para recoger");
        legacy.setEstadoPedido(null); // Nulo como en base de datos preexistente

        assertEquals("LISTO_RECOGER", legacy.getEstadoPedido());

        legacy.setTipoEntrega("delivery");
        legacy.setEstado("Entregado");
        assertEquals("ENTREGADO", legacy.getEstadoPedido());

        legacy.setTipoEntrega("pickup");
        legacy.setEstado("Entregado");
        assertEquals("RECOGIDO", legacy.getEstadoPedido());
    }
}

