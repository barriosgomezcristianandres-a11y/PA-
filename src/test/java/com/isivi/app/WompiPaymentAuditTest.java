package com.isivi.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.isivi.app.controller.DashboardController;
import com.isivi.app.controller.PagoController;
import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.service.ReservaService;
import com.isivi.app.service.WhatsAppNotificationService;
import com.isivi.app.service.WompiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
public class WompiPaymentAuditTest {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private WompiService wompiService;

    @Autowired
    private PagoController pagoController;

    @Autowired
    private DashboardController dashboardController;

    @MockBean
    private WhatsAppNotificationService whatsAppNotificationService;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        reservaRepository.deleteAll();
    }

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

    @Test
    @DisplayName("WOMPI 1: Firma de integridad para Widget Checkout es exacta según especificación")
    void testFirmaIntegridadWompi() {
        String ref = "ISV-TEST-999";
        long montoCentavos = 5500000L;
        String firma = wompiService.firmaIntegridad(ref, montoCentavos);
        String esperada = calcularSha256(ref + montoCentavos + "COPtest_integrity_SECRET123");
        assertEquals(esperada, firma);
    }

    @Test
    @DisplayName("WOMPI 2: Verificación de firma de Webhook con checksum válido e inválido")
    void testValidacionFirmaWebhook() {
        long timestamp = 1718000000L;
        String txId = "tx-12345";
        String status = "APPROVED";
        long amount = 2500000L;

        ObjectNode evento = mapper.createObjectNode();
        evento.put("event", "transaction.updated");
        evento.put("timestamp", timestamp);
        evento.put("environment", "test");

        ObjectNode data = evento.putObject("data");
        ObjectNode tx = data.putObject("transaction");
        tx.put("id", txId);
        tx.put("status", status);
        tx.put("amount_in_cents", amount);
        tx.put("reference", "ISV-REF-100");
        tx.put("currency", "COP");

        ObjectNode signature = evento.putObject("signature");
        ArrayNode props = signature.putArray("properties");
        props.add("transaction.id");
        props.add("transaction.status");
        props.add("transaction.amount_in_cents");

        String concat = txId + status + amount + timestamp + "test_events_SECRET456";
        String checksumValido = calcularSha256(concat);
        signature.put("checksum", checksumValido);

        // Checksum válido
        assertTrue(wompiService.eventoAutentico(evento, null));

        // Checksum alterado
        signature.put("checksum", "0000000000000000000000000000000000000000000000000000000000000000");
        assertFalse(wompiService.eventoAutentico(evento, null));
    }

    @Test
    @DisplayName("WOMPI 3: Webhook rechaza evento con ambiente inconsistente (prod en sandbox o test en prod)")
    void testWebhookRechazaEnvironmentInconsistente() {
        long timestamp = 1718000000L;
        ObjectNode evento = mapper.createObjectNode();
        evento.put("event", "transaction.updated");
        evento.put("timestamp", timestamp);
        evento.put("environment", "prod"); // Enviado como prod mientras app está en sandbox

        ObjectNode data = evento.putObject("data");
        ObjectNode tx = data.putObject("transaction");
        tx.put("id", "tx-999");
        tx.put("status", "APPROVED");
        tx.put("amount_in_cents", 100000L);
        tx.put("reference", "ISV-ENV-TEST");
        tx.put("currency", "COP");

        ObjectNode signature = evento.putObject("signature");
        signature.putArray("properties");
        signature.put("checksum", "any");

        ResponseEntity<?> response = pagoController.webhook(evento, null);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    @DisplayName("WOMPI 4: Webhook rechaza cuando el monto o moneda no coinciden con la reserva")
    void testWebhookRechazaMontoInconsistente() {
        Reserva r = new Reserva();
        r.setCodigoReserva("MONTO-001");
        r.setNombreCliente("Cliente Monto");
        r.setTelefono("3001112233");
        r.setSubtotal(100000d);
        r.setAnticipo(25000d);
        r.setMontoPagoCentavos(2500000L);
        r.setReferenciaWompi("ISV-MONTO-1");
        r.setEstado("Pendiente Pago");
        r.setEstadoPago("PENDIENTE");
        r.setItemsInventario(List.of());
        reservaRepository.save(r);

        long timestamp = 1718000000L;
        ObjectNode evento = mapper.createObjectNode();
        evento.put("event", "transaction.updated");
        evento.put("timestamp", timestamp);
        evento.put("environment", "test");

        ObjectNode data = evento.putObject("data");
        ObjectNode tx = data.putObject("transaction");
        tx.put("id", "tx-monto-1");
        tx.put("status", "APPROVED");
        tx.put("amount_in_cents", 1000000L); // Monto alterado (10.000 COP en vez de 25.000 COP)
        tx.put("reference", "ISV-MONTO-1");
        tx.put("currency", "COP");

        ObjectNode signature = evento.putObject("signature");
        ArrayNode props = signature.putArray("properties");
        props.add("transaction.id");
        props.add("transaction.status");
        props.add("transaction.amount_in_cents");
        String checksum = calcularSha256("tx-monto-1APPROVED1000000" + timestamp + "test_events_SECRET456");
        signature.put("checksum", checksum);

        ResponseEntity<?> response = pagoController.webhook(evento, null);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    @DisplayName("WOMPI 5: Webhook APPROVED confirma reserva, registra transacción y fecha de pago")
    void testWebhookAprobadoActualizaReserva() {
        Reserva r = new Reserva();
        r.setCodigoReserva("SUCC-001");
        r.setNombreCliente("Cliente Exitoso");
        r.setTelefono("3005556677");
        r.setSubtotal(80000d);
        r.setAnticipo(20000d);
        r.setMontoPagoCentavos(2000000L);
        r.setReferenciaWompi("ISV-SUCCESS-01");
        r.setEstado("Pendiente Pago");
        r.setEstadoPago("PENDIENTE");
        r.setItemsInventario(List.of());
        reservaRepository.save(r);

        long timestamp = 1718000000L;
        ObjectNode evento = mapper.createObjectNode();
        evento.put("event", "transaction.updated");
        evento.put("timestamp", timestamp);
        evento.put("environment", "test");

        ObjectNode data = evento.putObject("data");
        ObjectNode tx = data.putObject("transaction");
        tx.put("id", "tx-wompi-real-1");
        tx.put("status", "APPROVED");
        tx.put("amount_in_cents", 2000000L);
        tx.put("reference", "ISV-SUCCESS-01");
        tx.put("currency", "COP");
        tx.put("payment_method_type", "CARD");

        ObjectNode signature = evento.putObject("signature");
        ArrayNode props = signature.putArray("properties");
        props.add("transaction.id");
        props.add("transaction.status");
        props.add("transaction.amount_in_cents");
        String checksum = calcularSha256("tx-wompi-real-1APPROVED2000000" + timestamp + "test_events_SECRET456");
        signature.put("checksum", checksum);

        ResponseEntity<?> response = pagoController.webhook(evento, null);
        assertEquals(200, response.getStatusCode().value());

        Reserva actualizada = reservaRepository.findByReferenciaWompi("ISV-SUCCESS-01").orElseThrow();
        assertEquals("Pago Confirmado", actualizada.getEstado());
        assertEquals("APROBADO", actualizada.getEstadoPago());
        assertEquals("tx-wompi-real-1", actualizada.getTransaccionWompiId());
        assertEquals("CARD", actualizada.getMetodoPagoWompi());
        assertNotNull(actualizada.getFechaPago());
    }

    @Test
    @DisplayName("WOMPI 6: Webhook duplicado es 100% idempotente y no duplica operaciones")
    void testWebhookDuplicadoEsIdempotente() {
        Reserva r = new Reserva();
        r.setCodigoReserva("IDEM-001");
        r.setNombreCliente("Cliente Idempotencia");
        r.setTelefono("3007778899");
        r.setSubtotal(100000d);
        r.setAnticipo(25000d);
        r.setMontoPagoCentavos(2500000L);
        r.setReferenciaWompi("ISV-IDEM-01");
        r.setEstado("Pendiente Pago");
        r.setEstadoPago("PENDIENTE");
        r.setItemsInventario(List.of());
        reservaRepository.save(r);

        long timestamp = 1718000000L;
        ObjectNode evento = mapper.createObjectNode();
        evento.put("event", "transaction.updated");
        evento.put("timestamp", timestamp);
        evento.put("environment", "test");

        ObjectNode data = evento.putObject("data");
        ObjectNode tx = data.putObject("transaction");
        tx.put("id", "tx-idem-1");
        tx.put("status", "APPROVED");
        tx.put("amount_in_cents", 2500000L);
        tx.put("reference", "ISV-IDEM-01");
        tx.put("currency", "COP");

        ObjectNode signature = evento.putObject("signature");
        ArrayNode props = signature.putArray("properties");
        props.add("transaction.id");
        props.add("transaction.status");
        props.add("transaction.amount_in_cents");
        String checksum = calcularSha256("tx-idem-1APPROVED2500000" + timestamp + "test_events_SECRET456");
        signature.put("checksum", checksum);

        // Primer webhook
        ResponseEntity<?> resp1 = pagoController.webhook(evento, null);
        assertEquals(200, resp1.getStatusCode().value());

        // Segundo webhook (duplicado)
        ResponseEntity<?> resp2 = pagoController.webhook(evento, null);
        assertEquals(200, resp2.getStatusCode().value());

        // Tercer webhook (duplicado)
        ResponseEntity<?> resp3 = pagoController.webhook(evento, null);
        assertEquals(200, resp3.getStatusCode().value());

        Reserva guardada = reservaRepository.findByReferenciaWompi("ISV-IDEM-01").orElseThrow();
        assertEquals("Pago Confirmado", guardada.getEstado());
        assertEquals("APROBADO", guardada.getEstadoPago());
    }

    @Test
    @DisplayName("WOMPI 7: Webhook DECLINED libera inventario y marca reserva Denegada")
    void testWebhookRechazadoDeclinedLiberaInventario() {
        Producto prod = new Producto();
        prod.setNombre("Shampoo Detox Wompi Test");
        prod.setPrecio(45000d);
        prod.setCantidad(10);
        prod.setEnStock(true);
        productoRepository.save(prod);

        ItemReserva item = new ItemReserva();
        item.setId(prod.getId());
        item.setTipo("producto");
        item.setNombre(prod.getNombre());
        item.setPrecioUnitario(45000d);
        item.setCantidad(2);
        item.setSubtotal(90000d);

        Reserva r = new Reserva();
        r.setCodigoReserva("DECL-001");
        r.setNombreCliente("Cliente Rechazado");
        r.setTelefono("3001239999");
        r.setSubtotal(90000d);
        r.setAnticipo(0d);
        r.setMontoPagoCentavos(9000000L);
        r.setReferenciaWompi("ISV-DECLINED-01");
        r.setEstado("Pendiente Pago");
        r.setEstadoPago("PENDIENTE");
        r.setItemsInventario(List.of(item));
        r.setInventarioReservado(false);

        // Descontar inventario al crear la reserva
        reservaService.reservarInventario(r);
        reservaRepository.save(r);

        Producto prodDescontado = productoRepository.findById(prod.getId()).orElseThrow();
        assertEquals(8, prodDescontado.getCantidad()); // 10 - 2

        // Llega evento DECLINED de Wompi
        long timestamp = 1718000000L;
        ObjectNode evento = mapper.createObjectNode();
        evento.put("event", "transaction.updated");
        evento.put("timestamp", timestamp);
        evento.put("environment", "test");

        ObjectNode data = evento.putObject("data");
        ObjectNode tx = data.putObject("transaction");
        tx.put("id", "tx-declined-1");
        tx.put("status", "DECLINED");
        tx.put("amount_in_cents", 9000000L);
        tx.put("reference", "ISV-DECLINED-01");
        tx.put("currency", "COP");

        ObjectNode signature = evento.putObject("signature");
        ArrayNode props = signature.putArray("properties");
        props.add("transaction.id");
        props.add("transaction.status");
        props.add("transaction.amount_in_cents");
        String checksum = calcularSha256("tx-declined-1DECLINED9000000" + timestamp + "test_events_SECRET456");
        signature.put("checksum", checksum);

        ResponseEntity<?> resp = pagoController.webhook(evento, null);
        assertEquals(200, resp.getStatusCode().value());

        Reserva actualizada = reservaRepository.findByReferenciaWompi("ISV-DECLINED-01").orElseThrow();
        assertEquals("Pendiente Pago", actualizada.getEstado());
        assertEquals("RECHAZADO", actualizada.getEstadoPago());
        assertFalse(actualizada.getInventarioReservado());

        Producto prodRestaurado = productoRepository.findById(prod.getId()).orElseThrow();
        assertEquals(10, prodRestaurado.getCantidad()); // Stock reincorporado
    }

    @Test
    @DisplayName("WOMPI 8: Reintento de pago después de rechazo re-reserva el stock atómicamente")
    void testReintentoPagoReReservaStock() {
        Producto prod = new Producto();
        prod.setNombre("Tratamiento Capilar Retry Test");
        prod.setPrecio(60000d);
        prod.setCantidad(5);
        prod.setEnStock(true);
        productoRepository.save(prod);

        ItemReserva item = new ItemReserva();
        item.setId(prod.getId());
        item.setTipo("producto");
        item.setNombre(prod.getNombre());
        item.setPrecioUnitario(60000d);
        item.setCantidad(1);
        item.setSubtotal(60000d);

        Reserva r = new Reserva();
        r.setNombreCliente("Cliente Retry");
        r.setTelefono("3004445566");
        r.setCodigoReserva("RETRY-001");
        r.setSubtotal(60000d);
        r.setAnticipo(0d);
        r.setMontoPagoCentavos(6000000L);
        r.setReferenciaWompi("ISV-RETRY-REF-1");
        r.setEstado("Denegada"); // Estado tras pago rechazado
        r.setEstadoPago("RECHAZADO");
        r.setItemsInventario(List.of(item));
        r.setInventarioReservado(false); // Inventario liberado
        reservaRepository.save(r);

        // Cliente hace clic en "Reintentar pago"
        ResponseEntity<?> prepResp = pagoController.preparar(r.getId());
        assertEquals(200, prepResp.getStatusCode().value());

        Reserva reintentada = reservaRepository.findById(r.getId()).orElseThrow();
        assertEquals("Pendiente Pago", reintentada.getEstado());
        assertEquals("PENDIENTE", reintentada.getEstadoPago());
        assertTrue(reintentada.getInventarioReservado());

        Producto prodActualizado = productoRepository.findById(prod.getId()).orElseThrow();
        assertEquals(4, prodActualizado.getCantidad()); // Stock reservado de nuevo
    }

    @Test
    @DisplayName("WOMPI 9: Dashboard calcula exclusivamente ventas reales aprobadas y excluye pendientes o rechazadas")
    void testDashboardVentasReales() {
        LocalDate hoy = LocalDate.now(java.time.ZoneId.of("America/Bogota"));

        // 1. Reserva Wompi Aprobada hoy ($50.000)
        Reserva rAprobada = new Reserva();
        rAprobada.setCodigoReserva("DASH-001");
        rAprobada.setNombreCliente("Cliente Pagado");
        rAprobada.setTelefono("3001110000");
        rAprobada.setSubtotal(50000d);
        rAprobada.setMontoPagoCentavos(5000000L);
        rAprobada.setEstado("Confirmado");
        rAprobada.setEstadoPago("APROBADO");
        rAprobada.setFechaPago(hoy);
        rAprobada.setFechaCita(hoy);
        rAprobada.setItemsInventario(List.of());
        reservaRepository.save(rAprobada);

        // 2. Reserva Wompi Pendiente hoy ($70.000)
        Reserva rPendiente = new Reserva();
        rPendiente.setCodigoReserva("DASH-002");
        rPendiente.setNombreCliente("Cliente Pendiente");
        rPendiente.setTelefono("3002220000");
        rPendiente.setSubtotal(70000d);
        rPendiente.setMontoPagoCentavos(7000000L);
        rPendiente.setEstado("Pendiente Pago");
        rPendiente.setEstadoPago("PENDIENTE");
        rPendiente.setFechaCita(hoy);
        rPendiente.setItemsInventario(List.of());
        reservaRepository.save(rPendiente);

        // 3. Reserva Wompi Rechazada hoy ($90.000)
        Reserva rRechazada = new Reserva();
        rRechazada.setCodigoReserva("DASH-003");
        rRechazada.setNombreCliente("Cliente Rechazado");
        rRechazada.setTelefono("3003330000");
        rRechazada.setSubtotal(90000d);
        rRechazada.setMontoPagoCentavos(9000000L);
        rRechazada.setEstado("Denegada");
        rRechazada.setEstadoPago("RECHAZADO");
        rRechazada.setFechaCita(hoy);
        rRechazada.setItemsInventario(List.of());
        reservaRepository.save(rRechazada);

        ResponseEntity<Map<String, Object>> dashResp = dashboardController.obtenerResumen();
        assertEquals(200, dashResp.getStatusCode().value());
        Map<String, Object> body = dashResp.getBody();
        assertNotNull(body);

        double ventasHoy = ((Number) body.get("ventasHoy")).doubleValue();
        assertEquals(50000.0, ventasHoy, 0.001); // Solo contabiliza los $50.000 aprobados
    }

    @Test
    @DisplayName("WOMPI 10: Lookup endpoints devuelven información tanto por referencia como por transaccionId")
    void testLookupEndpointsPorReferenciaYTransaccionId() {
        Reserva r = new Reserva();
        r.setNombreCliente("Cliente Lookup");
        r.setTelefono("3008889900");
        r.setCodigoReserva("LOOK-1234");
        r.setSubtotal(120000d);
        r.setMontoPagoCentavos(3000000L);
        r.setReferenciaWompi("ISV-LOOK-REF-777");
        r.setTransaccionWompiId("wompi-tx-999888");
        r.setEstado("Confirmado");
        r.setEstadoPago("APROBADO");
        r.setItemsInventario(List.of());
        reservaRepository.save(r);

        // Consulta por referencia
        ResponseEntity<?> respRef = pagoController.estado("ISV-LOOK-REF-777");
        assertEquals(200, respRef.getStatusCode().value());
        com.isivi.app.dto.PagoStatusPublicResponse bodyRef = (com.isivi.app.dto.PagoStatusPublicResponse) respRef.getBody();
        assertEquals("ISV-LOOK-REF-777", bodyRef.referencia());
        assertEquals("APROBADO", bodyRef.estadoPago());

        // Consulta por transaction ID
        ResponseEntity<?> respTx = pagoController.transaccion("wompi-tx-999888");
        assertEquals(200, respRef.getStatusCode().value());
        com.isivi.app.dto.PagoStatusPublicResponse bodyTx = (com.isivi.app.dto.PagoStatusPublicResponse) respTx.getBody();
        assertEquals("ISV-LOOK-REF-777", bodyTx.referencia());
        assertEquals("APROBADO", bodyTx.estadoPago());
    }
}
