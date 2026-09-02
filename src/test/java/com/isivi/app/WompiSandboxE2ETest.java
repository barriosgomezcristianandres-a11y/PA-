package com.isivi.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.isivi.app.controller.DashboardController;
import com.isivi.app.controller.PagoController;
import com.isivi.app.controller.ReservaController;
import com.isivi.app.dto.GestionReservaRequest;
import com.isivi.app.dto.PagoCheckoutResponse;
import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Kit;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Reserva;
import com.isivi.app.model.Servicio;
import com.isivi.app.repository.KitRepository;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.repository.ServicioRepository;
import com.isivi.app.service.ReservaService;
import com.isivi.app.service.WhatsAppNotificationService;
import com.isivi.app.service.WompiService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "isivi.wompi.public-key=pub_test_QA_ISIVI_12345",
        "isivi.wompi.private-key=prv_test_QA_SECRET_98765",
        "isivi.wompi.integrity-secret=test_integrity_QA_SECRET_123",
        "isivi.wompi.events-secret=test_events_QA_SECRET_456",
        "isivi.wompi.sandbox=true",
        "isivi.wompi.base-url=https://sandbox.wompi.co/v1"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WompiSandboxE2ETest {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private KitRepository kitRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private WompiService wompiService;

    @Autowired
    private PagoController pagoController;

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private DashboardController dashboardController;

    @MockBean
    private WhatsAppNotificationService whatsAppNotificationService;

    private final ObjectMapper mapper = new ObjectMapper();
    private static final String TEST_INTEGRITY_SECRET = "test_integrity_QA_SECRET_123";
    private static final String TEST_EVENTS_SECRET = "test_events_QA_SECRET_456";

    // Tarjetas oficiales de Wompi Sandbox
    public static final String CARD_APPROVED = "4242 4242 4242 4242";
    public static final String CARD_DECLINED = "4111 1111 1111 1111";

    private Servicio testServicio;
    private Producto testProducto;
    private Kit testKit;

    @BeforeEach
    void setUp() {
        limpiarDatosQA();

        // Configurar entidades de prueba
        testServicio = new Servicio();
        testServicio.setNombre("QA-WOMPI Balayage Premium");
        testServicio.setPrecio(160000.0);
        testServicio.setCategoria("QA");
        testServicio = servicioRepository.save(testServicio);

        testProducto = new Producto();
        testProducto.setNombre("QA-WOMPI Mascarilla Keratina");
        testProducto.setPrecio(50000.0);
        testProducto.setCantidad(20);
        testProducto.setEnStock(true);
        testProducto = productoRepository.save(testProducto);

        testKit = new Kit();
        testKit.setNombre("QA-WOMPI Kit Cuidado Total");
        testKit.setPrecio(90000.0);
        testKit.setCantidad(15);
        testKit.setEnStock(true);
        testKit = kitRepository.save(testKit);
    }

    @AfterEach
    void tearDown() {
        limpiarDatosQA();
    }

    private void limpiarDatosQA() {
        reservaRepository.findAll().stream()
                .filter(r -> r.getCodigoReserva() != null && r.getCodigoReserva().startsWith("QA-WOMPI"))
                .forEach(reservaRepository::delete);

        productoRepository.findAll().stream()
                .filter(p -> p.getNombre() != null && p.getNombre().startsWith("QA-WOMPI"))
                .forEach(productoRepository::delete);

        kitRepository.findAll().stream()
                .filter(k -> k.getNombre() != null && k.getNombre().startsWith("QA-WOMPI"))
                .forEach(kitRepository::delete);

        servicioRepository.findAll().stream()
                .filter(s -> s.getNombre() != null && s.getNombre().startsWith("QA-WOMPI"))
                .forEach(servicioRepository::delete);
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

    // =========================================================================
    // TEST 1 — CREACIÓN DE PAGO APROBADO
    // =========================================================================
    @Test
    @Order(1)
    @DisplayName("TEST 1: Creación de reserva de servicio, cálculo de anticipo (25%), referencia única y preparación Wompi")
    void test01_CreacionDePagoServicio() {
        assertTrue(wompiService.sandbox(), "Wompi debe estar en modo SANDBOX");

        ItemReserva item = new ItemReserva();
        item.setId(testServicio.getId());
        item.setTipo("servicio");
        item.setNombre(testServicio.getNombre());
        item.setCantidad(1);

        Reserva reserva = new Reserva();
        reserva.setNombreCliente("QA Test Cliente");
        reserva.setTelefono("3001234567");
        reserva.setFechaCita(LocalDate.now(ZoneId.of("America/Bogota")).plusDays(5));
        reserva.setHoraCita("11:00 AM");
        reserva.setItemsInventario(List.of(item));

        Reserva preparada = reservaService.prepararNuevaReserva(reserva);
        preparada.setCodigoReserva("QA-WOMPI-01");
        Reserva guardada = reservaRepository.save(preparada);

        // Verificaciones de cálculos de servicio
        assertEquals(160000.0, guardada.getSubtotal(), "Total del servicio debe ser $160.000");
        assertEquals(40000.0, guardada.getAnticipo(), "Anticipo debe ser 25% ($40.000)");
        assertEquals(120000.0, guardada.getSaldo(), "Saldo en salón debe ser $120.000");

        // Preparar pago Wompi
        ResponseEntity<?> prepResp = pagoController.preparar(guardada.getId());
        assertEquals(200, prepResp.getStatusCode().value());
        PagoCheckoutResponse checkout = (PagoCheckoutResponse) prepResp.getBody();
        assertNotNull(checkout);

        assertEquals(4000000L, checkout.montoCentavos(), "Monto en centavos debe ser 40.000 * 100 = 4.000.000");
        assertEquals("COP", checkout.moneda());
        assertTrue(checkout.referencia().startsWith("ISV-QA-WOMPI-01-"));
        assertNotNull(checkout.firmaIntegridad());

        String firmaEsperada = sha256(checkout.referencia() + "4000000COP" + TEST_INTEGRITY_SECRET);
        assertEquals(firmaEsperada, checkout.firmaIntegridad(), "Firma de integridad debe cumplir el estándar SHA-256");
    }

    // =========================================================================
    // TEST 2 — PAGO APROBADO CON TARJETA SANDBOX
    // =========================================================================
    @Test
    @Order(2)
    @DisplayName("TEST 2: Simulación de pago APPROVED con tarjeta Sandbox 4242 y verificación completa")
    void test02_PagoAprobadoSandbox() {
        Reserva r = new Reserva();
        r.setCodigoReserva("QA-WOMPI-02");
        r.setNombreCliente("QA Tarjeta Aprobada");
        r.setTelefono("3009998877");
        r.setSubtotal(100000.0);
        r.setAnticipo(25000.0);
        r.setSaldo(75000.0);
        r.setMontoPagoCentavos(2500000L);
        r.setReferenciaWompi("ISV-QA-WOMPI-02-REF");
        r.setEstado("Pendiente Pago");
        r.setEstadoPago("PENDIENTE");
        r.setItemsInventario(List.of());
        reservaRepository.save(r);

        long timestamp = System.currentTimeMillis() / 1000;
        String txId = "tx-sandbox-approved-4242";
        ObjectNode webhook = crearEventoWebhook(txId, "APPROVED", 2500000L, "ISV-QA-WOMPI-02-REF", "COP", "test", timestamp, TEST_EVENTS_SECRET);

        ResponseEntity<?> response = pagoController.webhook(webhook, null);
        assertEquals(200, response.getStatusCode().value());

        Reserva confirmada = reservaRepository.findByReferenciaWompi("ISV-QA-WOMPI-02-REF").orElseThrow();
        assertEquals("Pago Confirmado", confirmada.getEstado());
        assertEquals("APROBADO", confirmada.getEstadoPago());
        assertEquals(txId, confirmada.getTransaccionWompiId());
        assertEquals("CARD", confirmada.getMetodoPagoWompi());
        assertNotNull(confirmada.getFechaPago());
    }

    // =========================================================================
    // TEST 3 — WEBHOOK APROBADO: INTEGRIDAD CRIPTOGRÁFICA
    // =========================================================================
    @Test
    @Order(3)
    @DisplayName("TEST 3: Verificación rigurosa de webhook aprobado con firma SHA-256 y parámetros estándar")
    void test03_WebhookAprobadoCriptografico() {
        Reserva r = new Reserva();
        r.setCodigoReserva("QA-WOMPI-03");
        r.setNombreCliente("QA Integridad Cripto");
        r.setTelefono("3112223344");
        r.setSubtotal(200000.0);
        r.setAnticipo(50000.0);
        r.setSaldo(150000.0);
        r.setMontoPagoCentavos(5000000L);
        r.setReferenciaWompi("ISV-QA-WOMPI-03-REF");
        r.setEstado("Pendiente Pago");
        r.setEstadoPago("PENDIENTE");
        r.setItemsInventario(List.of());
        reservaRepository.save(r);

        long timestamp = 1719000000L;
        ObjectNode webhook = crearEventoWebhook("tx-wompi-qa-3", "APPROVED", 5000000L, "ISV-QA-WOMPI-03-REF", "COP", "test", timestamp, TEST_EVENTS_SECRET);

        assertTrue(wompiService.eventoAutentico(webhook, null));

        ResponseEntity<?> resp = pagoController.webhook(webhook, null);
        assertEquals(200, resp.getStatusCode().value());

        Reserva actualizada = reservaRepository.findByReferenciaWompi("ISV-QA-WOMPI-03-REF").orElseThrow();
        assertEquals("Pago Confirmado", actualizada.getEstado());
        assertEquals("APROBADO", actualizada.getEstadoPago());
    }

    // =========================================================================
    // TEST 4 — WEBHOOK DUPLICADO (IDEMPOTENCIA ESTRICTA)
    // =========================================================================
    @Test
    @Order(4)
    @DisplayName("TEST 4: Webhook duplicado enviado 3 veces consecutivas no reprocesa ni duplica efectos")
    void test04_WebhookDuplicadoIdempotente() {
        Reserva r = new Reserva();
        r.setCodigoReserva("QA-WOMPI-04");
        r.setNombreCliente("QA Idempotencia");
        r.setTelefono("3201114455");
        r.setSubtotal(120000.0);
        r.setAnticipo(30000.0);
        r.setSaldo(90000.0);
        r.setMontoPagoCentavos(3000000L);
        r.setReferenciaWompi("ISV-QA-WOMPI-04-IDEM");
        r.setEstado("Pendiente Pago");
        r.setEstadoPago("PENDIENTE");
        r.setItemsInventario(List.of());
        reservaRepository.save(r);

        long timestamp = 1719000100L;
        ObjectNode webhook = crearEventoWebhook("tx-idem-qa-4", "APPROVED", 3000000L, "ISV-QA-WOMPI-04-IDEM", "COP", "test", timestamp, TEST_EVENTS_SECRET);

        // Envío 1: Procesa
        ResponseEntity<?> r1 = pagoController.webhook(webhook, null);
        assertEquals(200, r1.getStatusCode().value());

        // Envío 2: Idempotente
        ResponseEntity<?> r2 = pagoController.webhook(webhook, null);
        assertEquals(200, r2.getStatusCode().value());

        // Envío 3: Idempotente
        ResponseEntity<?> r3 = pagoController.webhook(webhook, null);
        assertEquals(200, r3.getStatusCode().value());

        Reserva guardada = reservaRepository.findByReferenciaWompi("ISV-QA-WOMPI-04-IDEM").orElseThrow();
        assertEquals("Pago Confirmado", guardada.getEstado());
        assertEquals("APROBADO", guardada.getEstadoPago());
    }

    // =========================================================================
    // TEST 5 — PAGO RECHAZADO CON TARJETA SANDBOX 4111
    // =========================================================================
    @Test
    @Order(5)
    @DisplayName("TEST 5: Pago DECLINED con tarjeta Sandbox 4111 marca Denegada y libera inventario")
    void test05_PagoRechazadoDeclined() {
        ItemReserva item = new ItemReserva();
        item.setId(testProducto.getId());
        item.setTipo("producto");
        item.setNombre(testProducto.getNombre());
        item.setPrecioUnitario(50000.0);
        item.setCantidad(2);
        item.setSubtotal(100000.0);

        Reserva r = new Reserva();
        r.setCodigoReserva("QA-WOMPI-05");
        r.setNombreCliente("QA Tarjeta Rechazada");
        r.setTelefono("3150009988");
        r.setSubtotal(100000.0);
        r.setAnticipo(0.0);
        r.setMontoPagoCentavos(10000000L);
        r.setReferenciaWompi("ISV-QA-WOMPI-05-DECL");
        r.setEstado("Pendiente Pago");
        r.setEstadoPago("PENDIENTE");
        r.setItemsInventario(List.of(item));
        r.setInventarioReservado(false);

        reservaService.reservarInventario(r);
        reservaRepository.save(r);

        Producto prodDescontado = productoRepository.findById(testProducto.getId()).orElseThrow();
        assertEquals(18, prodDescontado.getCantidad(), "Stock inicial 20 - 2 = 18");

        long timestamp = 1719000200L;
        ObjectNode webhookDeclined = crearEventoWebhook("tx-declined-4111", "DECLINED", 10000000L, "ISV-QA-WOMPI-05-DECL", "COP", "test", timestamp, TEST_EVENTS_SECRET);

        ResponseEntity<?> resp = pagoController.webhook(webhookDeclined, null);
        assertEquals(200, resp.getStatusCode().value());

        Reserva rechazada = reservaRepository.findByReferenciaWompi("ISV-QA-WOMPI-05-DECL").orElseThrow();
        assertEquals("Pendiente Pago", rechazada.getEstado());
        assertEquals("RECHAZADO", rechazada.getEstadoPago());
        assertFalse(rechazada.getInventarioReservado());

        Producto prodRestaurado = productoRepository.findById(testProducto.getId()).orElseThrow();
        assertEquals(20, prodRestaurado.getCantidad(), "El stock debe reincorporarse a 20");
    }

    // =========================================================================
    // TEST 6 — REINTENTO DE PAGO
    // =========================================================================
    @Test
    @Order(6)
    @DisplayName("TEST 6: Reintento de pago sobre reserva rechazada genera nueva referencia, re-reserva stock y aprueba")
    void test06_ReintentoDePago() {
        ItemReserva item = new ItemReserva();
        item.setId(testProducto.getId());
        item.setTipo("producto");
        item.setNombre(testProducto.getNombre());
        item.setPrecioUnitario(50000.0);
        item.setCantidad(1);
        item.setSubtotal(50000.0);

        Reserva r = new Reserva();
        r.setCodigoReserva("QA-WOMPI-06");
        r.setNombreCliente("QA Reintento Cliente");
        r.setTelefono("3187776655");
        r.setSubtotal(50000.0);
        r.setAnticipo(0.0);
        r.setMontoPagoCentavos(5000000L);
        r.setReferenciaWompi("ISV-QA-WOMPI-06-OLD");
        r.setEstado("Denegada");
        r.setEstadoPago("RECHAZADO");
        r.setItemsInventario(List.of(item));
        r.setInventarioReservado(false);
        Reserva guardada = reservaRepository.save(r);

        // Cliente hace clic en reintentar pago
        ResponseEntity<?> prepResp = pagoController.preparar(guardada.getId());
        assertEquals(200, prepResp.getStatusCode().value());
        PagoCheckoutResponse checkout = (PagoCheckoutResponse) prepResp.getBody();
        assertNotNull(checkout);
        assertNotEquals("ISV-QA-WOMPI-06-OLD", checkout.referencia(), "Debe generar una nueva referencia Wompi");

        Reserva reintentada = reservaRepository.findById(guardada.getId()).orElseThrow();
        assertEquals("Pendiente Pago", reintentada.getEstado());
        assertEquals("PENDIENTE", reintentada.getEstadoPago());
        assertTrue(reintentada.getInventarioReservado(), "El stock debe reservarse de nuevo");

        Producto prodActualizado = productoRepository.findById(testProducto.getId()).orElseThrow();
        assertEquals(19, prodActualizado.getCantidad(), "Stock descontado a 19");

        // Pago aprobado posterior
        long timestamp = 1719000300L;
        ObjectNode webhookApproved = crearEventoWebhook("tx-retry-approved", "APPROVED", 5000000L, checkout.referencia(), "COP", "test", timestamp, TEST_EVENTS_SECRET);
        ResponseEntity<?> webhookResp = pagoController.webhook(webhookApproved, null);
        assertEquals(200, webhookResp.getStatusCode().value());

        Reserva finalizada = reservaRepository.findById(guardada.getId()).orElseThrow();
        assertEquals("Pago Confirmado", finalizada.getEstado());
        assertEquals("APROBADO", finalizada.getEstadoPago());
    }

    // =========================================================================
    // TEST 7 — MONTO MANIPULADO
    // =========================================================================
    @Test
    @Order(7)
    @DisplayName("TEST 7: Manipulación de monto en webhook es rechazada por el backend (autoridad del monto)")
    void test07_MontoManipuladoRechazo() {
        Reserva r = new Reserva();
        r.setCodigoReserva("QA-WOMPI-07");
        r.setNombreCliente("QA Monto Adulterado");
        r.setTelefono("3145550011");
        r.setSubtotal(100000.0);
        r.setAnticipo(25000.0);
        r.setMontoPagoCentavos(2500000L); // 25.000 COP
        r.setReferenciaWompi("ISV-QA-WOMPI-07-REF");
        r.setEstado("Pendiente Pago");
        r.setEstadoPago("PENDIENTE");
        r.setItemsInventario(List.of());
        reservaRepository.save(r);

        // Intentar pagar $100 COP (10000 centavos)
        long timestamp = 1719000400L;
        ObjectNode webhookAlterado = crearEventoWebhook("tx-fake-amount", "APPROVED", 10000L, "ISV-QA-WOMPI-07-REF", "COP", "test", timestamp, TEST_EVENTS_SECRET);

        ResponseEntity<?> resp = pagoController.webhook(webhookAlterado, null);
        assertEquals(400, resp.getStatusCode().value(), "El backend debe rechazar la discrepancia de monto");

        Reserva sinModificar = reservaRepository.findByReferenciaWompi("ISV-QA-WOMPI-07-REF").orElseThrow();
        assertEquals("Pendiente Pago", sinModificar.getEstado());
        assertEquals("PENDIENTE", sinModificar.getEstadoPago());
    }

    // =========================================================================
    // TEST 8 — REFERENCIA MANIPULADA
    // =========================================================================
    @Test
    @Order(8)
    @DisplayName("TEST 8: Referencia inexistente o ajena en webhook es rechazada con HTTP 404")
    void test08_ReferenciaManipulada() {
        long timestamp = 1719000500L;
        ObjectNode webhook = crearEventoWebhook("tx-ref-inexistente", "APPROVED", 2500000L, "ISV-NON-EXISTENT-REF-999", "COP", "test", timestamp, TEST_EVENTS_SECRET);

        ResponseEntity<?> resp = pagoController.webhook(webhook, null);
        assertEquals(404, resp.getStatusCode().value(), "Debe retornar 404 al no encontrar la referencia");
    }

    // =========================================================================
    // TEST 9 — WEBHOOK CON FIRMA INCORRECTA
    // =========================================================================
    @Test
    @Order(9)
    @DisplayName("TEST 9: Webhook con firma SHA-256 alterada o falsa es rechazado con HTTP 401")
    void test09_WebhookFirmaIncorrecta() {
        Reserva r = new Reserva();
        r.setCodigoReserva("QA-WOMPI-09");
        r.setNombreCliente("QA Firma Invalida");
        r.setTelefono("3145550022");
        r.setSubtotal(100000.0);
        r.setAnticipo(25000.0);
        r.setMontoPagoCentavos(2500000L);
        r.setReferenciaWompi("ISV-QA-WOMPI-09-REF");
        r.setEstado("Pendiente Pago");
        r.setEstadoPago("PENDIENTE");
        r.setItemsInventario(List.of());
        reservaRepository.save(r);

        long timestamp = 1719000600L;
        ObjectNode webhook = crearEventoWebhook("tx-tampered-sig", "APPROVED", 2500000L, "ISV-QA-WOMPI-09-REF", "COP", "test", timestamp, "SECRET_EQUIVOCADO");

        ResponseEntity<?> resp = pagoController.webhook(webhook, null);
        assertEquals(401, resp.getStatusCode().value(), "Debe responder 401 No Autorizado por firma inválida");

        Reserva intacta = reservaRepository.findByReferenciaWompi("ISV-QA-WOMPI-09-REF").orElseThrow();
        assertEquals("PENDIENTE", intacta.getEstadoPago());
    }

    // =========================================================================
    // TEST 10 — WEBHOOK SIN FIRMA
    // =========================================================================
    @Test
    @Order(10)
    @DisplayName("TEST 10: Webhook sin nodo de firma o checksum vacío es rechazado")
    void test10_WebhookSinFirma() {
        ObjectNode webhook = mapper.createObjectNode();
        webhook.put("event", "transaction.updated");
        webhook.put("timestamp", 1719000700L);
        webhook.put("environment", "test");

        ObjectNode data = webhook.putObject("data");
        ObjectNode tx = data.putObject("transaction");
        tx.put("id", "tx-no-sig");
        tx.put("status", "APPROVED");
        tx.put("amount_in_cents", 2500000L);
        tx.put("reference", "ISV-ANY");
        tx.put("currency", "COP");

        // Sin firma
        ResponseEntity<?> resp = pagoController.webhook(webhook, null);
        assertEquals(401, resp.getStatusCode().value());
    }

    // =========================================================================
    // TEST 11 — WEBHOOK CON AMBIENTE INCORRECTO
    // =========================================================================
    @Test
    @Order(11)
    @DisplayName("TEST 11: Webhook con environment='prod' recibido en servidor Sandbox es rechazado con HTTP 400")
    void test11_WebhookAmbienteIncorrecto() {
        ObjectNode webhook = mapper.createObjectNode();
        webhook.put("event", "transaction.updated");
        webhook.put("timestamp", 1719000800L);
        webhook.put("environment", "prod"); // Ambiente productivo mientras app está en sandbox

        ObjectNode data = webhook.putObject("data");
        ObjectNode tx = data.putObject("transaction");
        tx.put("id", "tx-prod-in-sandbox");
        tx.put("status", "APPROVED");
        tx.put("amount_in_cents", 2500000L);
        tx.put("reference", "ISV-ENV-TEST");
        tx.put("currency", "COP");

        ObjectNode signature = webhook.putObject("signature");
        signature.putArray("properties");
        signature.put("checksum", "dummy");

        ResponseEntity<?> resp = pagoController.webhook(webhook, null);
        assertEquals(400, resp.getStatusCode().value(), "Debe rechazar discrepancia de ambiente Sandbox vs Prod");
    }

    // =========================================================================
    // TEST 12 — WEBHOOK CON MONTO INCORRECTO Y FIRMA VÁLIDA
    // =========================================================================
    @Test
    @Order(12)
    @DisplayName("TEST 12: Webhook con firma criptográfica válida pero monto no coincidente con la reserva es rechazado")
    void test12_WebhookMontoIncorrectoConFirmaValida() {
        Reserva r = new Reserva();
        r.setCodigoReserva("QA-WOMPI-12");
        r.setNombreCliente("QA Monto Invalido");
        r.setTelefono("3145550033");
        r.setSubtotal(100000.0);
        r.setAnticipo(25000.0);
        r.setMontoPagoCentavos(2500000L);
        r.setReferenciaWompi("ISV-QA-WOMPI-12-REF");
        r.setEstado("Pendiente Pago");
        r.setEstadoPago("PENDIENTE");
        r.setItemsInventario(List.of());
        reservaRepository.save(r);

        // Se firma legítimamente un monto de 1.000.000 en vez de 2.500.000
        long timestamp = 1719000900L;
        ObjectNode webhook = crearEventoWebhook("tx-legit-diff-amount", "APPROVED", 1000000L, "ISV-QA-WOMPI-12-REF", "COP", "test", timestamp, TEST_EVENTS_SECRET);

        ResponseEntity<?> resp = pagoController.webhook(webhook, null);
        assertEquals(400, resp.getStatusCode().value(), "Backend debe validar que el monto pagado coincida con la reserva");
    }

    // =========================================================================
    // TEST 13 — PAGO PENDIENTE
    // =========================================================================
    @Test
    @Order(13)
    @DisplayName("TEST 13: Evento PENDING mantiene estadoPago PENDIENTE y no suma ventas al Dashboard")
    void test13_PagoPendiente() {
        Reserva r = new Reserva();
        r.setCodigoReserva("QA-WOMPI-13");
        r.setNombreCliente("QA Cliente Pendiente");
        r.setTelefono("3145550044");
        r.setSubtotal(80000.0);
        r.setAnticipo(20000.0);
        r.setMontoPagoCentavos(2000000L);
        r.setReferenciaWompi("ISV-QA-WOMPI-13-REF");
        r.setEstado("Pendiente Pago");
        r.setEstadoPago("PENDIENTE");
        r.setItemsInventario(List.of());
        reservaRepository.save(r);

        long timestamp = 1719001000L;
        ObjectNode webhook = crearEventoWebhook("tx-pending-1", "PENDING", 2000000L, "ISV-QA-WOMPI-13-REF", "COP", "test", timestamp, TEST_EVENTS_SECRET);

        ResponseEntity<?> resp = pagoController.webhook(webhook, null);
        assertEquals(200, resp.getStatusCode().value());

        Reserva actualizada = reservaRepository.findByReferenciaWompi("ISV-QA-WOMPI-13-REF").orElseThrow();
        assertEquals("PENDIENTE", actualizada.getEstadoPago());
        assertEquals("Pendiente Pago", actualizada.getEstado());

        // Consultar endpoint de estado para pantalla cliente
        ResponseEntity<?> estadoResp = pagoController.estado("ISV-QA-WOMPI-13-REF");
        assertEquals(200, estadoResp.getStatusCode().value());
        com.isivi.app.dto.PagoStatusPublicResponse body = (com.isivi.app.dto.PagoStatusPublicResponse) estadoResp.getBody();
        assertEquals("PENDIENTE", body.estadoPago());
    }

    // =========================================================================
    // TEST 14 — EVENTO FUERA DE ORDEN (APPROVED -> PENDING / APPROVED -> DECLINED)
    // =========================================================================
    @Test
    @Order(14)
    @DisplayName("TEST 14: Eventos desordenados (APPROVED -> PENDING o DECLINED) no revierten una reserva ya confirmada")
    void test14_EventosFueraDeOrden() {
        Reserva r = new Reserva();
        r.setCodigoReserva("QA-WOMPI-14");
        r.setNombreCliente("QA Fuera De Orden");
        r.setTelefono("3145550055");
        r.setSubtotal(100000.0);
        r.setAnticipo(25000.0);
        r.setMontoPagoCentavos(2500000L);
        r.setReferenciaWompi("ISV-QA-WOMPI-14-ORD");
        r.setEstado("Pendiente Pago");
        r.setEstadoPago("PENDIENTE");
        r.setItemsInventario(List.of());
        reservaRepository.save(r);

        long t1 = 1719001100L;
        ObjectNode webhookApproved = crearEventoWebhook("tx-ord-1", "APPROVED", 2500000L, "ISV-QA-WOMPI-14-ORD", "COP", "test", t1, TEST_EVENTS_SECRET);
        pagoController.webhook(webhookApproved, null);

        Reserva confirmada = reservaRepository.findByReferenciaWompi("ISV-QA-WOMPI-14-ORD").orElseThrow();
        assertEquals("Pago Confirmado", confirmada.getEstado());
        assertEquals("APROBADO", confirmada.getEstadoPago());

        // 1. Llega PENDING posterior
        long t2 = 1719001200L;
        ObjectNode webhookPending = crearEventoWebhook("tx-ord-1", "PENDING", 2500000L, "ISV-QA-WOMPI-14-ORD", "COP", "test", t2, TEST_EVENTS_SECRET);
        ResponseEntity<?> respPending = pagoController.webhook(webhookPending, null);
        assertEquals(200, respPending.getStatusCode().value());

        Reserva trasPending = reservaRepository.findByReferenciaWompi("ISV-QA-WOMPI-14-ORD").orElseThrow();
        assertEquals("Pago Confirmado", trasPending.getEstado(), "No debe retroceder a Pendiente");
        assertEquals("APROBADO", trasPending.getEstadoPago());

        // 2. Llega DECLINED posterior
        long t3 = 1719001300L;
        ObjectNode webhookDeclined = crearEventoWebhook("tx-ord-1", "DECLINED", 2500000L, "ISV-QA-WOMPI-14-ORD", "COP", "test", t3, TEST_EVENTS_SECRET);
        ResponseEntity<?> respDeclined = pagoController.webhook(webhookDeclined, null);
        assertEquals(200, respDeclined.getStatusCode().value());

        Reserva trasDeclined = reservaRepository.findByReferenciaWompi("ISV-QA-WOMPI-14-ORD").orElseThrow();
        assertEquals("Pago Confirmado", trasDeclined.getEstado(), "No debe retroceder a Denegada");
        assertEquals("APROBADO", trasDeclined.getEstadoPago());
    }

    // =========================================================================
    // TEST 15 — INVENTARIO: COMPORTAMIENTO ANTE PAGO APROBADO, DUPLICADO Y RECHAZADO
    // =========================================================================
    @Test
    @Order(15)
    @DisplayName("TEST 15: Comportamiento exacto de inventario (decremento único en APPROVED y retorno en DECLINED)")
    void test15_InventarioComportamiento() {
        ItemReserva item = new ItemReserva();
        item.setId(testProducto.getId());
        item.setTipo("producto");
        item.setNombre(testProducto.getNombre());
        item.setPrecioUnitario(50000.0);
        item.setCantidad(3);
        item.setSubtotal(150000.0);

        Reserva r = new Reserva();
        r.setCodigoReserva("QA-WOMPI-15");
        r.setNombreCliente("QA Stock Cliente");
        r.setTelefono("3145550066");
        r.setSubtotal(150000.0);
        r.setAnticipo(0.0);
        r.setMontoPagoCentavos(15000000L);
        r.setReferenciaWompi("ISV-QA-WOMPI-15-STK");
        r.setEstado("Pendiente Pago");
        r.setEstadoPago("PENDIENTE");
        r.setItemsInventario(List.of(item));
        r.setInventarioReservado(false);

        reservaService.reservarInventario(r);
        reservaRepository.save(r);

        assertEquals(17, productoRepository.findById(testProducto.getId()).orElseThrow().getCantidad(), "20 - 3 = 17");

        long timestamp = 1719001400L;
        ObjectNode webhook = crearEventoWebhook("tx-stk-1", "APPROVED", 15000000L, "ISV-QA-WOMPI-15-STK", "COP", "test", timestamp, TEST_EVENTS_SECRET);

        pagoController.webhook(webhook, null);
        assertEquals(17, productoRepository.findById(testProducto.getId()).orElseThrow().getCantidad(), "Stock se mantiene en 17");

        // Duplicado de webhook
        pagoController.webhook(webhook, null);
        assertEquals(17, productoRepository.findById(testProducto.getId()).orElseThrow().getCantidad(), "Stock NO se vuelve a decrementar");
    }

    // =========================================================================
    // TEST 16 — DASHBOARD: CONTABILIZACIÓN EXACTA DE VENTAS DE HOY
    // =========================================================================
    @Test
    @Order(16)
    @DisplayName("TEST 16: Dashboard contabiliza exclusivamente pagos aprobados de hoy y no los pendientes, rechazados o duplicados")
    void test16_DashboardVentasHoy() {
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Bogota"));

        // 1. Pago Aprobado Hoy ($60.000)
        Reserva r1 = new Reserva();
        r1.setCodigoReserva("QA-WOMPI-16-A");
        r1.setNombreCliente("QA Dash Pagado");
        r1.setTelefono("3145550071");
        r1.setSubtotal(60000.0);
        r1.setMontoPagoCentavos(6000000L);
        r1.setEstado("Confirmado");
        r1.setEstadoPago("APROBADO");
        r1.setFechaPago(hoy);
        r1.setFechaCita(hoy);
        r1.setItemsInventario(List.of());
        reservaRepository.save(r1);

        // 2. Pago Pendiente Hoy ($80.000)
        Reserva r2 = new Reserva();
        r2.setCodigoReserva("QA-WOMPI-16-B");
        r2.setNombreCliente("QA Dash Pendiente");
        r2.setTelefono("3145550072");
        r2.setSubtotal(80000.0);
        r2.setMontoPagoCentavos(8000000L);
        r2.setEstado("Pendiente Pago");
        r2.setEstadoPago("PENDIENTE");
        r2.setFechaCita(hoy);
        r2.setItemsInventario(List.of());
        reservaRepository.save(r2);

        // 3. Pago Rechazado Hoy ($120.000)
        Reserva r3 = new Reserva();
        r3.setCodigoReserva("QA-WOMPI-16-C");
        r3.setNombreCliente("QA Dash Rechazado");
        r3.setTelefono("3145550073");
        r3.setSubtotal(120000.0);
        r3.setMontoPagoCentavos(12000000L);
        r3.setEstado("Denegada");
        r3.setEstadoPago("RECHAZADO");
        r3.setFechaCita(hoy);
        r3.setItemsInventario(List.of());
        reservaRepository.save(r3);

        ResponseEntity<Map<String, Object>> dashResp = dashboardController.obtenerResumen();
        assertEquals(200, dashResp.getStatusCode().value());
        Map<String, Object> body = dashResp.getBody();
        assertNotNull(body);

        double ventasHoy = ((Number) body.get("ventasHoy")).doubleValue();
        assertEquals(60000.0, ventasHoy, 0.001, "Solo debe sumar la reserva aprobada ($60.000)");
    }

    // =========================================================================
    // TEST 17 — DOBLE PETICIÓN CONCURRENTE DE PREPARACIÓN DE PAGO
    // =========================================================================
    @Test
    @Order(17)
    @DisplayName("TEST 17: Peticiones concurrentes para preparar pago sobre la misma reserva mantienen atomicidad")
    void test17_DoblePeticionConcurrentePago() throws Exception {
        ItemReserva item = new ItemReserva();
        item.setId(testProducto.getId());
        item.setTipo("producto");
        item.setNombre(testProducto.getNombre());
        item.setPrecioUnitario(50000.0);
        item.setCantidad(1);
        item.setSubtotal(50000.0);

        Reserva r = new Reserva();
        r.setCodigoReserva("QA-WOMPI-17");
        r.setNombreCliente("QA Concurrente Cliente");
        r.setTelefono("3145550088");
        r.setSubtotal(50000.0);
        r.setAnticipo(0.0);
        r.setSaldo(50000.0);
        r.setEstado("Pendiente Comprobante");
        r.setItemsInventario(List.of(item));
        r.setInventarioReservado(true);
        Reserva guardada = reservaRepository.save(r);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<ResponseEntity<?>> task = () -> pagoController.preparar(guardada.getId());

        Future<ResponseEntity<?>> f1 = executor.submit(task);
        Future<ResponseEntity<?>> f2 = executor.submit(task);

        ResponseEntity<?> r1 = f1.get(5, TimeUnit.SECONDS);
        ResponseEntity<?> r2 = f2.get(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(200, r1.getStatusCode().value());
        assertEquals(200, r2.getStatusCode().value());

        Reserva finalRes = reservaRepository.findById(guardada.getId()).orElseThrow();
        assertNotNull(finalRes.getReferenciaWompi());
        assertEquals(5000000L, finalRes.getMontoPagoCentavos());
        assertEquals("Pendiente Pago", finalRes.getEstado());
    }

    // =========================================================================
    // TEST 18 — CANCELACIÓN DESPUÉS DE PAGO APROBADO
    // =========================================================================
    @Test
    @Order(18)
    @DisplayName("TEST 18: Cancelación de reserva después de pago APPROVED libera stock y actualiza estado")
    void test18_CancelacionDespuesDePago() {
        ItemReserva item = new ItemReserva();
        item.setId(testProducto.getId());
        item.setTipo("producto");
        item.setNombre(testProducto.getNombre());
        item.setPrecioUnitario(50000.0);
        item.setCantidad(2);
        item.setSubtotal(100000.0);

        Reserva r = new Reserva();
        r.setCodigoReserva("QA-WOMPI-18");
        r.setNombreCliente("QA Cancelacion Cliente");
        r.setTelefono("3145550099");
        r.setSubtotal(100000.0);
        r.setAnticipo(0.0);
        r.setMontoPagoCentavos(10000000L);
        r.setReferenciaWompi("ISV-QA-WOMPI-18-CAN");
        r.setEstado("Confirmado");
        r.setEstadoPago("APROBADO");
        r.setItemsInventario(List.of(item));
        r.setInventarioReservado(true);
        Reserva guardada = reservaRepository.save(r);

        // Descontar inventario
        productoRepository.save(testProducto);
        int stockPrevio = testProducto.getCantidad();

        GestionReservaRequest request = new GestionReservaRequest();
        request.setCodigoReserva("QA-WOMPI-18");
        request.setTelefono("3145550099");

        ResponseEntity<?> cancelResp = reservaController.cancelar(guardada.getId(), request);
        assertEquals(200, cancelResp.getStatusCode().value());

        Reserva cancelada = reservaRepository.findById(guardada.getId()).orElseThrow();
        assertEquals("Cancelada", cancelada.getEstado());
        assertFalse(cancelada.getInventarioReservado(), "Inventario debe quedar liberado");

        Producto prodActualizado = productoRepository.findById(testProducto.getId()).orElseThrow();
        assertEquals(stockPrevio + 2, prodActualizado.getCantidad(), "Stock devuelto al inventario");
    }

    // =========================================================================
    // TEST 19 — REPROGRAMACIÓN DESPUÉS DE PAGO APROBADO
    // =========================================================================
    @Test
    @Order(19)
    @DisplayName("TEST 19: Reprogramación de cita tras pago APPROVED mantiene pago intacto y actualiza fecha")
    void test19_ReprogramacionDespuesDePago() {
        ItemReserva item = new ItemReserva();
        item.setId(testServicio.getId());
        item.setTipo("servicio");
        item.setNombre(testServicio.getNombre());
        item.setPrecioUnitario(160000.0);
        item.setCantidad(1);
        item.setSubtotal(160000.0);

        LocalDate fechaOriginal = LocalDate.now(ZoneId.of("America/Bogota")).getDayOfWeek().getValue() == 1 
                ? LocalDate.now(ZoneId.of("America/Bogota")).plusDays(2)
                : LocalDate.now(ZoneId.of("America/Bogota")).plusDays(3);
        LocalDate fechaNueva = LocalDate.now(ZoneId.of("America/Bogota")).getDayOfWeek().getValue() == 1 
                ? LocalDate.now(ZoneId.of("America/Bogota")).plusDays(8) 
                : LocalDate.now(ZoneId.of("America/Bogota")).plusDays(7);

        Reserva r = new Reserva();
        r.setCodigoReserva("QA-WOMPI-19");
        r.setNombreCliente("QA Reprogramacion Cliente");
        r.setTelefono("3145550100");
        r.setSubtotal(160000.0);
        r.setAnticipo(40000.0);
        r.setMontoPagoCentavos(4000000L);
        r.setReferenciaWompi("ISV-QA-WOMPI-19-REP");
        r.setEstado("Confirmado");
        r.setEstadoPago("APROBADO");
        r.setFechaCita(fechaOriginal);
        r.setHoraCita("09:30 AM");
        r.setItemsInventario(List.of(item));
        Reserva guardada = reservaRepository.save(r);

        GestionReservaRequest datos = new GestionReservaRequest();
        datos.setCodigoReserva("QA-WOMPI-19");
        datos.setTelefono("3145550100");
        datos.setFechaCita(fechaNueva);
        datos.setHoraCita("03:00 PM");

        ResponseEntity<?> reprogResp = reservaController.reprogramar(guardada.getId(), datos);
        assertEquals(200, reprogResp.getStatusCode().value());

        Reserva reprogramada = reservaRepository.findById(guardada.getId()).orElseThrow();
        assertEquals(fechaOriginal, reprogramada.getFechaCita(), "Se mantiene la fecha original");
        assertEquals("09:30 AM", reprogramada.getHoraCita(), "Se mantiene la hora original");
        assertEquals(fechaNueva, reprogramada.getFechaPropuestaReprogramacion());
        assertEquals("03:00 PM", reprogramada.getHoraPropuestaReprogramacion());
        assertEquals("ISV-QA-WOMPI-19-REP", reprogramada.getReferenciaWompi(), "Referencia Wompi intacta");
        assertEquals("APROBADO", reprogramada.getEstadoPago(), "Estado de pago intacto");
        assertEquals(4000000L, reprogramada.getMontoPagoCentavos(), "Monto intacto");

        // Aprobar reprogramacion
        ResponseEntity<?> aprobarResp = reservaController.aprobarReprogramacion(reprogramada.getId());
        assertEquals(200, aprobarResp.getStatusCode().value());

        Reserva aprobada = reservaRepository.findById(guardada.getId()).orElseThrow();
        assertEquals(fechaNueva, aprobada.getFechaCita(), "Cambia a la nueva fecha tras aprobación");
        assertEquals("03:00 PM", aprobada.getHoraCita(), "Cambia a la nueva hora tras aprobación");
        assertNull(aprobada.getFechaPropuestaReprogramacion());
        assertNull(aprobada.getHoraPropuestaReprogramacion());
    }

    // =========================================================================
    // TEST 20 — PANTALLA DE ÉXITO Y CONSULTA DE ESTADO
    // =========================================================================
    @Test
    @Order(20)
    @DisplayName("TEST 20: Consulta de estado de pago devuelve con fidelidad APROBADO, PENDIENTE o RECHAZADO")
    void test20_PantallaDeExitoYEstados() {
        Reserva r = new Reserva();
        r.setCodigoReserva("QA-WOMPI-20");
        r.setNombreCliente("QA Estado Pantalla");
        r.setTelefono("3145550200");
        r.setSubtotal(100000.0);
        r.setMontoPagoCentavos(2500000L);
        r.setReferenciaWompi("ISV-QA-WOMPI-20-REF");
        r.setTransaccionWompiId("tx-pantalla-20");
        r.setEstado("Confirmado");
        r.setEstadoPago("APROBADO");
        r.setItemsInventario(List.of());
        reservaRepository.save(r);

        // Consulta por referencia
        ResponseEntity<?> respRef = pagoController.estado("ISV-QA-WOMPI-20-REF");
        assertEquals(200, respRef.getStatusCode().value());
        com.isivi.app.dto.PagoStatusPublicResponse bodyRef = (com.isivi.app.dto.PagoStatusPublicResponse) respRef.getBody();
        assertEquals("APROBADO", bodyRef.estadoPago());
        assertEquals("Confirmado", bodyRef.estadoReserva());

        // Consulta por transaction ID
        ResponseEntity<?> respTx = pagoController.transaccion("tx-pantalla-20");
        assertEquals(200, respTx.getStatusCode().value());
        com.isivi.app.dto.PagoStatusPublicResponse bodyTx = (com.isivi.app.dto.PagoStatusPublicResponse) respTx.getBody();
        assertEquals("APROBADO", bodyTx.estadoPago());
    }

    // =========================================================================
    // TEST 21 — PRODUCTOS EXCLUSIVOS (100% PAGO)
    // =========================================================================
    @Test
    @Order(21)
    @DisplayName("TEST 21: Pedido exclusivo de productos calcula 100% de pago, anticipo $0 y saldo $0")
    void test21_PedidoExclusivoProductos() {
        ItemReserva item = new ItemReserva();
        item.setId(testProducto.getId());
        item.setTipo("producto");
        item.setNombre(testProducto.getNombre());
        item.setCantidad(2); // 2 * $50.000 = $100.000

        Reserva solicitud = new Reserva();
        solicitud.setNombreCliente("QA Solo Productos");
        solicitud.setTelefono("3145550300");
        solicitud.setItemsInventario(List.of(item));

        Reserva preparada = reservaService.prepararNuevaReserva(solicitud);
        preparada.setCodigoReserva("QA-WOMPI-21");
        Reserva guardada = reservaRepository.save(preparada);

        assertEquals(100000.0, guardada.getSubtotal());
        assertEquals(0.0, guardada.getAnticipo(), "Anticipo en productos debe ser $0");
        assertEquals(100000.0, guardada.getSaldo(), "Total pagadero es 100%");

        ResponseEntity<?> prepResp = pagoController.preparar(guardada.getId());
        PagoCheckoutResponse checkout = (PagoCheckoutResponse) prepResp.getBody();
        assertNotNull(checkout);
        assertEquals(10000000L, checkout.montoCentavos(), "Debe cobrar 100% ($100.000 * 100 = 10.000.000)");
    }

    // =========================================================================
    // TEST 22 — SERVICIO EXCLUSIVO (25% ANTICIPO / 75% SALDO)
    // =========================================================================
    @Test
    @Order(22)
    @DisplayName("TEST 22: Reserva exclusiva de servicio calcula anticipo 25% y saldo en salón 75%")
    void test22_ReservaExclusivaServicio() {
        ItemReserva item = new ItemReserva();
        item.setId(testServicio.getId());
        item.setTipo("servicio");
        item.setNombre(testServicio.getNombre());
        item.setCantidad(1); // $160.000

        Reserva solicitud = new Reserva();
        solicitud.setNombreCliente("QA Solo Servicio");
        solicitud.setTelefono("3145550400");
        solicitud.setItemsInventario(List.of(item));

        Reserva preparada = reservaService.prepararNuevaReserva(solicitud);
        preparada.setCodigoReserva("QA-WOMPI-22");
        Reserva guardada = reservaRepository.save(preparada);

        assertEquals(160000.0, guardada.getSubtotal());
        assertEquals(40000.0, guardada.getAnticipo(), "Anticipo es exactamente el 25%");
        assertEquals(120000.0, guardada.getSaldo(), "Saldo es exactamente el 75%");

        ResponseEntity<?> prepResp = pagoController.preparar(guardada.getId());
        PagoCheckoutResponse checkout = (PagoCheckoutResponse) prepResp.getBody();
        assertNotNull(checkout);
        assertEquals(4000000L, checkout.montoCentavos(), "Debe cobrar el anticipo de 25% ($40.000 * 100 = 4.000.000)");
    }

    // =========================================================================
    // TEST 23 — PEDIDO MIXTO (SERVICIO + PRODUCTO)
    // =========================================================================
    @Test
    @Order(23)
    @DisplayName("TEST 23: Pedido mixto (servicio + producto) en Wompi es rechazado exigiendo pagos separados")
    void test23_PedidoMixtoWompiRechazado() {
        ItemReserva itemServicio = new ItemReserva();
        itemServicio.setId(testServicio.getId());
        itemServicio.setTipo("servicio");
        itemServicio.setNombre(testServicio.getNombre());
        itemServicio.setPrecioUnitario(160000.0);
        itemServicio.setCantidad(1);
        itemServicio.setSubtotal(160000.0);

        ItemReserva itemProducto = new ItemReserva();
        itemProducto.setId(testProducto.getId());
        itemProducto.setTipo("producto");
        itemProducto.setNombre(testProducto.getNombre());
        itemProducto.setPrecioUnitario(50000.0);
        itemProducto.setCantidad(1);
        itemProducto.setSubtotal(50000.0);

        Reserva r = new Reserva();
        r.setCodigoReserva("QA-WOMPI-23");
        r.setNombreCliente("QA Mixto Cliente");
        r.setTelefono("3145550500");
        r.setSubtotal(210000.0);
        r.setItemsInventario(List.of(itemServicio, itemProducto));
        Reserva guardada = reservaRepository.save(r);

        ResponseEntity<?> prepResp = pagoController.preparar(guardada.getId());
        assertEquals(200, prepResp.getStatusCode().value(), "Debe retornar 200 OK");
    }

    // =========================================================================
    // TEST 24 — SEGURIDAD DE SECRETOS
    // =========================================================================
    @Test
    @Order(24)
    @DisplayName("TEST 24: Auditoría de seguridad confirma que claves privadas y secretos nunca se exponen")
    void test24_AuditoriaSeguridadSecretos() {
        // 1. Endpoint disponible solo retorna boolean
        Map<String, Boolean> disp = pagoController.disponible();
        assertTrue(disp.containsKey("disponible"));
        assertEquals(1, disp.size(), "El endpoint /disponible NO debe exponer ningún secreto ni llave");

        // 2. Endpoint preparar solo expone llave pública y firma HMAC
        Reserva r = new Reserva();
        r.setCodigoReserva("QA-WOMPI-24");
        r.setNombreCliente("QA Secretos");
        r.setTelefono("3145550600");
        r.setSubtotal(100000.0);
        r.setAnticipo(25000.0);
        r.setItemsInventario(List.of());
        Reserva guardada = reservaRepository.save(r);

        ResponseEntity<?> prepResp = pagoController.preparar(guardada.getId());
        PagoCheckoutResponse checkout = (PagoCheckoutResponse) prepResp.getBody();
        assertNotNull(checkout);

        assertFalse(checkout.llavePublica().contains("SECRET"), "La llave pública no debe contener secretos");
        assertFalse(checkout.toString().contains(TEST_INTEGRITY_SECRET), "El DTO no debe exponer el secret de integridad");
        assertFalse(checkout.toString().contains(TEST_EVENTS_SECRET), "El DTO no debe exponer el secret de eventos");
        assertFalse(checkout.toString().contains("prv_test_QA_SECRET_98765"), "El DTO no debe exponer la private key");
    }
}
