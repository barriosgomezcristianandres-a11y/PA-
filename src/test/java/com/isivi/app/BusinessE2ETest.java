package com.isivi.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.isivi.app.controller.DashboardController;
import com.isivi.app.controller.PagoController;
import com.isivi.app.controller.ReservaController;
import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Reserva;
import com.isivi.app.model.Servicio;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.repository.ServicioRepository;
import com.isivi.app.service.ReservaService;
import com.isivi.app.service.WhatsAppNotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "isivi.wompi.public-key=pub_test_QA_BUSINESS_123",
        "isivi.wompi.private-key=prv_test_QA_BUSINESS_456",
        "isivi.wompi.integrity-secret=test_integrity_QA_BUSINESS_789",
        "isivi.wompi.events-secret=test_events_QA_BUSINESS_999",
        "isivi.wompi.sandbox=true",
        "isivi.wompi.base-url=https://sandbox.wompi.co/v1"
})
public class BusinessE2ETest {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private PagoController pagoController;

    @Autowired
    private DashboardController dashboardController;

    @MockBean
    private WhatsAppNotificationService whatsAppNotificationService;

    private final ObjectMapper mapper = new ObjectMapper();
    private static final String TEST_EVENTS_SECRET = "test_events_QA_BUSINESS_999";

    private final List<String> createdIds = new ArrayList<>();
    private final List<String> createdProductIds = new ArrayList<>();
    private final List<String> createdServiceIds = new ArrayList<>();

    private Servicio servicioPrueba;

    @BeforeEach
    public void setup() {
        Servicio s = new Servicio();
        s.setNombre("Tratamiento Capilar QA Business");
        s.setPrecio(100000.0);
        s.setCategoria("tratamientos");
        s.setDuracion("60 min");
        servicioPrueba = servicioRepository.save(s);
        createdServiceIds.add(servicioPrueba.getId());
    }

    @AfterEach
    public void cleanup() {
        for (String id : createdIds) {
            try {
                reservaRepository.findById(id).ifPresent(r -> {
                    reservaService.liberarInventario(r);
                    reservaRepository.delete(r);
                });
            } catch (Exception ignored) {}
        }
        createdIds.clear();

        for (String id : createdProductIds) {
            try {
                productoRepository.deleteById(id);
            } catch (Exception ignored) {}
        }
        createdProductIds.clear();

        for (String id : createdServiceIds) {
            try {
                servicioRepository.deleteById(id);
            } catch (Exception ignored) {}
        }
        createdServiceIds.clear();
    }

    private String sha256(String valor) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private ObjectNode crearEventoWebhook(String txId, String status, long amountInCents, String reference, String currency, String environment, long timestamp, String secret) {
        ObjectNode evento = mapper.createObjectNode();
        evento.put("event", "transaction.updated");
        evento.put("timestamp", timestamp);
        evento.put("environment", environment);

        ObjectNode data = evento.putObject("data");
        ObjectNode tx = data.putObject("transaction");
        tx.put("id", txId);
        tx.put("status", status);
        tx.put("amount_in_cents", amountInCents);
        tx.put("reference", reference);
        tx.put("currency", currency);
        tx.put("payment_method_type", "CARD");

        ObjectNode signature = evento.putObject("signature");
        ArrayNode props = signature.putArray("properties");
        props.add("transaction.id");
        props.add("transaction.status");
        props.add("transaction.amount_in_cents");

        String concat = txId + status + amountInCents + timestamp + secret;
        signature.put("checksum", sha256(concat));

        return evento;
    }

    @Test
    @DisplayName("FLUJO 1: Cliente Servicio + Wompi Aprobado -> Confirmación Automática sin intervención manual")
    public void testFlujoCompletoClienteServicioWompiAprobado() {
        // 1. Crear solicitud de reserva para servicio
        Reserva r = new Reserva();
        r.setNombreCliente("Laura Gómez Test");
        r.setTelefono("3001234567");
        r.setCiudad("Cartagena");
        r.setFechaCita(LocalDate.now().getDayOfWeek().getValue() == 1 ? LocalDate.now().plusDays(8) : LocalDate.now().plusDays(7));
        r.setHoraCita("09:30 AM");

        ItemReserva serv = new ItemReserva();
        serv.setId(servicioPrueba.getId());
        serv.setNombre(servicioPrueba.getNombre());
        serv.setTipo("servicio");
        serv.setCantidad(1);
        serv.setPrecioUnitario(100000.0);
        serv.setSubtotal(100000.0);
        r.setItemsInventario(List.of(serv));

        r.setMedioPago("WOMPI");

        ResponseEntity<?> respCrear = reservaController.crear(r);
        assertEquals(201, respCrear.getStatusCode().value());
        assertTrue(respCrear.getBody() instanceof Reserva);
        Reserva guardada = (Reserva) respCrear.getBody();
        assertNotNull(guardada.getId());
        createdIds.add(guardada.getId());

        // 2. Preparar pago Wompi
        ResponseEntity<?> respPrep = pagoController.preparar(guardada.getId());
        assertEquals(200, respPrep.getStatusCode().value());

        Reserva prep = reservaRepository.findById(guardada.getId()).orElseThrow();
        assertEquals("Pendiente Pago", prep.getEstado());
        assertEquals("PENDIENTE", prep.getEstadoPago());
        assertEquals(2500000L, prep.getMontoPagoCentavos()); // 25.000 COP en centavos

        // 3. Webhook APPROVED simulando pasarela
        String txId = "tx-wompi-" + UUID.randomUUID().toString().substring(0, 8);
        long timestamp = System.currentTimeMillis() / 1000;
        ObjectNode evento = crearEventoWebhook(txId, "APPROVED", 2500000L, prep.getReferenciaWompi(), "COP", "test", timestamp, TEST_EVENTS_SECRET);

        ResponseEntity<?> respWebhook = pagoController.webhook(evento, evento.path("signature").path("checksum").asText());
        assertEquals(200, respWebhook.getStatusCode().value());

        // 4. Verificar confirmación automática de negocio
        Reserva confirmada = reservaRepository.findById(guardada.getId()).orElseThrow();
        assertEquals("Confirmado", confirmada.getEstado(), "Wompi Aprobado debe confirmar automáticamente");
        assertEquals("APROBADO", confirmada.getEstadoPago());
        assertEquals(txId, confirmada.getTransaccionWompiId());
        assertEquals(LocalDate.now(), confirmada.getFechaPago());

        // 5. Verificar Dashboard (debe contabilizar la venta y no exigir aprobación manual)
        ResponseEntity<Map<String, Object>> respDash = dashboardController.obtenerResumen();
        assertEquals(200, respDash.getStatusCode().value());
        Map<String, Object> dash = respDash.getBody();
        assertNotNull(dash);
        assertTrue(((Number) dash.get("ventasHoy")).doubleValue() >= 25000.0);
    }

    @Test
    @DisplayName("FLUJO 2: Cliente Transferencia -> Requiere validación manual por Administrador")
    public void testFlujoCompletoClienteTransferenciaManual() {
        // 1. Crear reserva de transferencia
        Reserva r = new Reserva();
        r.setNombreCliente("Carlos Transferencia Test");
        r.setTelefono("3007654321");
        r.setCiudad("Cartagena");
        LocalDate date = LocalDate.now().plusDays(8);
        while (date.getDayOfWeek() == DayOfWeek.MONDAY) {
            date = date.plusDays(1);
        }
        r.setFechaCita(date);
        r.setHoraCita("11:00 AM");

        ItemReserva serv = new ItemReserva();
        serv.setId(servicioPrueba.getId());
        serv.setNombre(servicioPrueba.getNombre());
        serv.setTipo("servicio");
        serv.setCantidad(1);
        serv.setPrecioUnitario(100000.0);
        serv.setSubtotal(100000.0);
        r.setItemsInventario(List.of(serv));

        r.setMedioPago("BANCOLOMBIA");

        ResponseEntity<?> respCrear = reservaController.crear(r);
        assertEquals(201, respCrear.getStatusCode().value());
        Reserva creada = (Reserva) respCrear.getBody();
        createdIds.add(creada.getId());

        assertEquals("Pendiente Comprobante", creada.getEstado());

        // 2. Administrador aprueba el comprobante
        ResponseEntity<?> respAprobar = reservaController.aprobar(creada.getId());
        assertEquals(200, respAprobar.getStatusCode().value());

        Reserva aprobada = reservaRepository.findById(creada.getId()).orElseThrow();
        assertEquals("Confirmado", aprobada.getEstado(), "Debe cambiar a Confirmado tras aprobación manual");

        // 3. Cliente consulta su reserva como confirmada
        ResponseEntity<?> respConsulta = reservaController.consultar(aprobada.getCodigoReserva(), "3007654321");
        assertEquals(200, respConsulta.getStatusCode().value());
        assertEquals("Confirmado", ((Reserva) respConsulta.getBody()).getEstado());
    }

    @Test
    @DisplayName("FLUJO 3: Pedido 100% de Productos -> Pago completo, anticipo 0, descuento de stock")
    public void testFlujoCompletoPedidoProductos() {
        // Crear producto de prueba con stock = 5
        Producto p = new Producto("Shampoo Especial QA Business", 30000.0, "Desc", "img.jpg", true, 5);
        p = productoRepository.save(p);
        createdProductIds.add(p.getId());

        // Crear pedido de 2 unidades
        Reserva pedido = new Reserva();
        pedido.setNombreCliente("María Compradora");
        pedido.setTelefono("3109876543");
        pedido.setCiudad("Medellín");
        pedido.setTipoEntrega("delivery");
        pedido.setDireccionEntrega("Carrera 43A # 1-50");

        ItemReserva item = new ItemReserva();
        item.setId(p.getId());
        item.setNombre(p.getNombre());
        item.setTipo("producto");
        item.setCantidad(2);
        item.setPrecioUnitario(30000.0);
        item.setSubtotal(60000.0);
        pedido.setItemsInventario(List.of(item));

        ResponseEntity<?> respCrear = reservaController.crear(pedido);
        assertEquals(201, respCrear.getStatusCode().value());
        Reserva guardado = (Reserva) respCrear.getBody();
        createdIds.add(guardado.getId());

        // Verificar que el stock se descontó atómicamente: 5 - 2 = 3
        Producto postReserva = productoRepository.findById(p.getId()).orElseThrow();
        assertEquals(3, postReserva.getCantidad());

        // Preparar Wompi para el 100% (60.000 COP = 6.000.000 centavos)
        pagoController.preparar(guardado.getId());
        Reserva prep = reservaRepository.findById(guardado.getId()).orElseThrow();
        assertEquals(6000000L, prep.getMontoPagoCentavos());

        // Webhook APPROVED
        String txId = "tx-prod-999";
        long timestamp = System.currentTimeMillis() / 1000;
        ObjectNode evento = crearEventoWebhook(txId, "APPROVED", 6000000L, prep.getReferenciaWompi(), "COP", "test", timestamp, TEST_EVENTS_SECRET);

        pagoController.webhook(evento, evento.path("signature").path("checksum").asText());

        Reserva completado = reservaRepository.findById(guardado.getId()).orElseThrow();
        assertEquals("Pago Confirmado", completado.getEstado());
        assertEquals("APROBADO", completado.getEstadoPago());
        assertEquals(0.0, completado.getSaldo(), "En pedidos 100% de productos el saldo debe ser 0");
    }

    @Test
    @DisplayName("FLUJO 4: Validación de conflictos de horario en agenda")
    public void testValidacionConflictosHorario() {
        LocalDate fecha = LocalDate.now().plusDays(12);
        while (fecha.getDayOfWeek() == DayOfWeek.MONDAY) {
            fecha = fecha.plusDays(1);
        }
        String hora = "03:00 PM";

        // Crear primera reserva confirmada
        Reserva r1 = new Reserva();
        r1.setNombreCliente("Cliente Primero");
        r1.setTelefono("3001112222");
        r1.setFechaCita(fecha);
        r1.setHoraCita(hora);
        r1.setEstado("Confirmado");
        r1 = reservaRepository.save(r1);
        createdIds.add(r1.getId());

        // Intentar crear segunda reserva en el mismo horario
        Reserva r2 = new Reserva();
        r2.setNombreCliente("Cliente Segundo");
        r2.setTelefono("3003334444");
        r2.setFechaCita(fecha);
        r2.setHoraCita(hora);

        ItemReserva serv = new ItemReserva();
        serv.setId(servicioPrueba.getId());
        serv.setNombre(servicioPrueba.getNombre());
        serv.setTipo("servicio");
        serv.setCantidad(1);
        serv.setPrecioUnitario(100000.0);
        serv.setSubtotal(100000.0);
        r2.setItemsInventario(List.of(serv));

        ResponseEntity<?> respConflicto = reservaController.crear(r2);
        assertEquals(409, respConflicto.getStatusCode().value(), "Debe retornar 409 Conflicto si el horario está ocupado");
        assertTrue(respConflicto.getBody().toString().contains("HORARIO_NO_DISPONIBLE"));
    }

    @Test
    @DisplayName("FLUJO 5: Cancelación libera horario e inventario")
    public void testCancelacionLiberaHorarioEInventario() {
        // Crear producto con stock = 10
        Producto p = new Producto("Serum QA Business", 40000.0, "Desc", "img.jpg", true, 10);
        p = productoRepository.save(p);
        createdProductIds.add(p.getId());

        Reserva r = new Reserva();
        r.setNombreCliente("Cliente a Cancelar");
        r.setTelefono("3008887766");
        r.setFechaCita(LocalDate.now().plusDays(9));
        r.setHoraCita("04:30 PM");

        ItemReserva item = new ItemReserva();
        item.setId(p.getId());
        item.setNombre(p.getNombre());
        item.setTipo("producto");
        item.setCantidad(3);
        item.setPrecioUnitario(40000.0);
        item.setSubtotal(120000.0);
        r.setItemsInventario(List.of(item));

        ResponseEntity<?> respCrear = reservaController.crear(r);
        Reserva guardada = (Reserva) respCrear.getBody();
        createdIds.add(guardada.getId());

        // Stock bajó a 7
        assertEquals(7, productoRepository.findById(p.getId()).orElseThrow().getCantidad());

        // Cancelar reserva
        reservaService.cancelar(guardada);

        // Stock debe haberse restituido a 10
        assertEquals(10, productoRepository.findById(p.getId()).orElseThrow().getCantidad(), "La cancelación debe restituir el stock atómicamente");
        assertEquals("Cancelada", reservaRepository.findById(guardada.getId()).orElseThrow().getEstado());
    }
}
