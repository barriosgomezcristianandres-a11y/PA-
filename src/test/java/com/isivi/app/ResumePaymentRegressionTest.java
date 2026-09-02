package com.isivi.app;

import com.isivi.app.controller.CategoriaProductoController;
import com.isivi.app.controller.PagoController;
import com.isivi.app.controller.ProductoController;
import com.isivi.app.controller.ReservaController;
import com.isivi.app.controller.ServicioController;
import com.isivi.app.dto.PagoCheckoutResponse;
import com.isivi.app.model.*;
import com.isivi.app.repository.*;
import com.isivi.app.service.ReservaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.ArrayList;

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
public class ResumePaymentRegressionTest {

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private PagoController pagoController;

    @Autowired
    private ServicioController servicioController;

    @Autowired
    private ProductoController productoController;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private KitRepository kitRepository;

    @Autowired
    private CategoriaProductoController categoriaProductoController;

    @Autowired
    private CategoriaProductoRepository categoriaProductoRepository;

    private Servicio servicioPrueba;
    private Producto productoPrueba;
    private LocalDate fechaPrueba;
    private final String horaPrueba = "09:30 AM";
    private final ZoneId zonaBogota = ZoneId.of("America/Bogota");

    @BeforeEach
    void setUp() {
        reservaRepository.deleteAll();
        servicioRepository.deleteAll();
        productoRepository.deleteAll();
        kitRepository.deleteAll();
        categoriaProductoRepository.deleteAll();

        servicioPrueba = new Servicio("Tratamiento Capilar", "Tratamientos", 100000.0, "60 min", "Desc", "");
        servicioPrueba = servicioRepository.save(servicioPrueba);

        productoPrueba = new Producto("Shampoo Artesanal", 50000.0, "Desc", "", true, 20);
        productoPrueba = productoRepository.save(productoPrueba);

        fechaPrueba = LocalDate.now(zonaBogota).plusDays(2);
        while (fechaPrueba.getDayOfWeek() == DayOfWeek.MONDAY) {
            fechaPrueba = fechaPrueba.plusDays(1);
        }

        reservaService.setPagoExpiracionMinutos(15);
        reservaController.setClock(Clock.system(zonaBogota));
    }

    @AfterEach
    void tearDown() {
        reservaController.setClock(Clock.system(zonaBogota));
        reservaRepository.deleteAll();
        servicioRepository.deleteAll();
        productoRepository.deleteAll();
        kitRepository.deleteAll();
    }

    @Test
    @DisplayName("1. Integridad de isivi.js: verificación estática de sintaxis y balance de llaves")
    void testIsiviJsSyntaxAndIntegrity() throws Exception {
        ClassPathResource resource = new ClassPathResource("static/js/isivi.js");
        assertTrue(resource.exists(), "static/js/isivi.js debe existir");

        String jsContent;
        try (InputStream is = resource.getInputStream()) {
            jsContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertFalse(jsContent.isBlank(), "isivi.js no debe estar vacío");

        // Contar balance de llaves
        int openBraces = 0;
        int closeBraces = 0;
        int openParens = 0;
        int closeParens = 0;
        int openBrackets = 0;
        int closeBrackets = 0;

        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inBacktick = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;

        char[] chars = jsContent.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            char next = (i + 1 < chars.length) ? chars[i + 1] : '\0';

            if (inLineComment) {
                if (c == '\n') inLineComment = false;
                continue;
            }
            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false;
                    i++;
                }
                continue;
            }

            if (!inSingleQuote && !inDoubleQuote && !inBacktick) {
                if (c == '/' && next == '/') {
                    inLineComment = true;
                    i++;
                    continue;
                }
                if (c == '/' && next == '*') {
                    inBlockComment = true;
                    i++;
                    continue;
                }
            }

            if (c == '\\' && (inSingleQuote || inDoubleQuote || inBacktick)) {
                i++; // saltar caracter escapado
                continue;
            }

            if (c == '\'' && !inDoubleQuote && !inBacktick) {
                inSingleQuote = !inSingleQuote;
                continue;
            }
            if (c == '"' && !inSingleQuote && !inBacktick) {
                inDoubleQuote = !inDoubleQuote;
                continue;
            }
            if (c == '`' && !inSingleQuote && !inDoubleQuote) {
                inBacktick = !inBacktick;
                continue;
            }

            if (!inSingleQuote && !inDoubleQuote && !inBacktick) {
                if (c == '{') openBraces++;
                else if (c == '}') closeBraces++;
                else if (c == '(') openParens++;
                else if (c == ')') closeParens++;
                else if (c == '[') openBrackets++;
                else if (c == ']') closeBrackets++;
            }
        }

        assertEquals(openBraces, closeBraces, "Las llaves {} en isivi.js deben estar perfectamente balanceadas");
        assertEquals(openParens, closeParens, "Los paréntesis () en isivi.js deben estar perfectamente balanceadas");
        assertEquals(openBrackets, closeBrackets, "Los corchetes [] en isivi.js deben estar perfectamente balanceados");
    }

    @Test
    @DisplayName("2. Servicios y Productos responden 200 y contienen datos activos")
    void testPublicEndpointsDataAvailable() {
        List<Servicio> listaServicios = servicioController.listar(null);
        assertNotNull(listaServicios);
        assertEquals(1, listaServicios.size());

        List<Producto> listaProductos = productoController.listar();
        assertNotNull(listaProductos);
        assertEquals(1, listaProductos.size());
    }


    @Test
    @DisplayName("3. Retomar pago: flujo completo con Wompi activo y titular legítimo")
    void testRetomarPagoFlujoCompleto() {
        Reserva req = new Reserva();
        req.setNombreCliente("Laura Restrepo");
        req.setTelefono("3112223344");
        req.setEmail("laura.restrepo@example.com");
        req.setMedioPago("WOMPI");
        req.setFechaCita(fechaPrueba);
        req.setHoraCita(horaPrueba);
        req.setItemsInventario(List.of(
                new ItemReserva(servicioPrueba.getId(), "servicio", 1)
        ));

        ResponseEntity<?> respCrear = reservaController.crear(req);
        assertEquals(HttpStatus.CREATED, respCrear.getStatusCode());
        Reserva creada = (Reserva) respCrear.getBody();
        assertNotNull(creada);

        // Cliente inicia Wompi
        ResponseEntity<?> respPrep = pagoController.preparar(creada.getId());
        assertEquals(HttpStatus.OK, respPrep.getStatusCode());

        // Cliente regresa y retoma con su teléfono
        ResponseEntity<?> respRetomar = pagoController.retomar(creada.getId(), Map.of("telefono", "3112223344"));
        assertEquals(HttpStatus.OK, respRetomar.getStatusCode());
        PagoCheckoutResponse checkout = (PagoCheckoutResponse) respRetomar.getBody();
        assertNotNull(checkout);
        assertNotNull(checkout.firmaIntegridad());
    }

    @Test
    @DisplayName("4. Seguridad y Privacidad: Rechazo 403 Forbidden para accesos no autorizados a reservas retenidas")
    void testSeguridadAntiApropiacion() {
        Reserva req = new Reserva();
        req.setNombreCliente("Cliente Privado");
        req.setTelefono("3009998877");
        req.setMedioPago("WOMPI");
        req.setFechaCita(fechaPrueba);
        req.setHoraCita(horaPrueba);
        req.setItemsInventario(List.of(new ItemReserva(servicioPrueba.getId(), "servicio", 1)));

        ResponseEntity<?> respCrear = reservaController.crear(req);
        Reserva creada = (Reserva) respCrear.getBody();
        pagoController.preparar(creada.getId());

        // Tercero intenta retomar con teléfono incorrecto
        ResponseEntity<?> respRetomar = pagoController.retomar(creada.getId(), Map.of("telefono", "3100001122"));
        assertEquals(HttpStatus.FORBIDDEN, respRetomar.getStatusCode());
    }

    @Test
    @DisplayName("5. Expiración segura: Reserva expirada rechaza retoma y libera el turno")
    void testExpiracionSeguraYLiberacion() {
        Reserva req = new Reserva();
        req.setNombreCliente("Cliente Lento");
        req.setTelefono("3154445566");
        req.setMedioPago("WOMPI");
        req.setFechaCita(fechaPrueba);
        req.setHoraCita(horaPrueba);
        req.setItemsInventario(List.of(new ItemReserva(servicioPrueba.getId(), "servicio", 1)));

        ResponseEntity<?> respCrear = reservaController.crear(req);
        Reserva creada = (Reserva) respCrear.getBody();
        pagoController.preparar(creada.getId());

        // Forzar expiración
        creada = reservaRepository.findById(creada.getId()).orElseThrow();
        creada.setFechaExpiracionPago(Instant.now().minus(10, ChronoUnit.MINUTES));
        reservaRepository.save(creada);

        // Intentar retomar -> 400 Reserva Expirada
        ResponseEntity<?> respRetomar = pagoController.retomar(creada.getId(), Map.of("telefono", "3154445566"));
        assertEquals(HttpStatus.BAD_REQUEST, respRetomar.getStatusCode());

        // Un nuevo cliente ahora puede reservar ese turno libremente
        Reserva reqNuevo = new Reserva();
        reqNuevo.setNombreCliente("Nuevo Cliente");
        reqNuevo.setTelefono("3189990011");
        reqNuevo.setMedioPago("WOMPI");
        reqNuevo.setFechaCita(fechaPrueba);
        reqNuevo.setHoraCita(horaPrueba);
        reqNuevo.setItemsInventario(List.of(new ItemReserva(servicioPrueba.getId(), "servicio", 1)));

        ResponseEntity<?> respNuevo = reservaController.crear(reqNuevo);
        assertEquals(HttpStatus.CREATED, respNuevo.getStatusCode());
    }

    @Test
    @DisplayName("6. Inventario Atómico: compra del último artículo cambia enStock = false en el mismo update")
    void testDescuentoDeStockACeroYAgotado() {
        Producto prod = new Producto("Ultimo Item", 40000.0, "Desc", "", true, 1);
        prod = productoRepository.save(prod);

        Reserva req = new Reserva();
        req.setNombreCliente("Cliente Ultimo");
        req.setTelefono("3119998877");
        req.setMedioPago("WOMPI");
        req.setFechaCita(null);
        req.setHoraCita(null);
        req.setItemsInventario(List.of(new ItemReserva(prod.getId(), "producto", 1)));

        ResponseEntity<?> response = reservaController.crear(req);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Reserva creada = (Reserva) response.getBody();
        assertNotNull(creada);

        // Al crear, se descuenta el stock
        Producto actualizado = productoRepository.findById(prod.getId()).orElseThrow();
        assertEquals(0, actualizado.getCantidad());
        assertFalse(actualizado.getEnStock(), "enStock debe ser false atómicamente");
    }

    @Test
    @DisplayName("7. Concurrencia: compra simultánea de la última unidad por dos clientes")
    void testConcurrenciaUltimoProducto() throws Exception {
        Producto prod = new Producto("Concurrente Item", 40000.0, "Desc", "", true, 1);
        prod = productoRepository.save(prod);

        Reserva reqA = new Reserva();
        reqA.setNombreCliente("Cliente A");
        reqA.setTelefono("3117770001");
        reqA.setMedioPago("WOMPI");
        reqA.setItemsInventario(List.of(new ItemReserva(prod.getId(), "producto", 1)));

        Reserva reqB = new Reserva();
        reqB.setNombreCliente("Cliente B");
        reqB.setTelefono("3117770002");
        reqB.setMedioPago("WOMPI");
        reqB.setItemsInventario(List.of(new ItemReserva(prod.getId(), "producto", 1)));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

        Future<ResponseEntity<?>> futureA = executor.submit(() -> {
            try {
                latch.await();
                return reservaController.crear(reqA);
            } catch (Exception ex) {
                return ResponseEntity.status(500).body(Map.of("mensaje", ex.getMessage()));
            }
        });

        Future<ResponseEntity<?>> futureB = executor.submit(() -> {
            try {
                latch.await();
                return reservaController.crear(reqB);
            } catch (Exception ex) {
                return ResponseEntity.status(500).body(Map.of("mensaje", ex.getMessage()));
            }
        });

        latch.countDown();
        ResponseEntity<?> respA = futureA.get(5, TimeUnit.SECONDS);
        ResponseEntity<?> respB = futureB.get(5, TimeUnit.SECONDS);

        executor.shutdown();

        int codeA = respA.getStatusCode().value();
        int codeB = respB.getStatusCode().value();

        assertTrue((codeA == 201 && codeB == 409) || (codeA == 409 && codeB == 201),
                "Exactamente uno debe ser 201 (éxito) y el otro 409 (conflicto de stock)");

        ResponseEntity<?> errorResp = (codeA == 409) ? respA : respB;
        Map<?, ?> body = (Map<?, ?>) errorResp.getBody();
        assertNotNull(body);
        assertEquals("STOCK_NO_DISPONIBLE", body.get("error"));
        assertEquals("El producto acaba de agotarse. Actualizamos tu carrito.", body.get("mensaje"));

        Producto finalProd = productoRepository.findById(prod.getId()).orElseThrow();
        assertEquals(0, finalProd.getCantidad());
        assertFalse(finalProd.getEnStock());
    }

    @Test
    @DisplayName("8. Reintentar Pago: pedido puro rechazado -> stock liberado -> retomar pago -> éxito")
    void testReintentarPagoPedidoPuro() {
        Producto prod = new Producto("Reintento Item", 15000.0, "Desc", "", true, 1);
        prod = productoRepository.save(prod);

        Reserva req = new Reserva();
        req.setNombreCliente("Cliente Reintento");
        req.setTelefono("3115554433");
        req.setEmail("reintento@test.com");
        req.setCiudad("Cartagena");
        req.setMedioPago("WOMPI");
        req.setTipoEntrega("pickup");
        req.setItemsInventario(List.of(new ItemReserva(prod.getId(), "producto", 1)));

        ResponseEntity<?> respCrear = reservaController.crear(req);
        assertEquals(HttpStatus.CREATED, respCrear.getStatusCode());
        Reserva creada = (Reserva) respCrear.getBody();
        assertNotNull(creada);

        // Preparamos pago Wompi
        reservaService.prepararPagoWompi(creada, "ref_wompi_test_123");
        Producto despuesPrep = productoRepository.findById(prod.getId()).orElseThrow();
        assertEquals(0, despuesPrep.getCantidad());
        assertFalse(despuesPrep.getEnStock());

        // Simulamos Webhook DECLINED
        // Como es pedido puro, pasa a PENDIENTE_PAGO y libera el stock
        creada = reservaRepository.findById(creada.getId()).orElseThrow();
        creada.setTransaccionWompiId("tx_wompi_declined");
        creada.setEstadoPago("RECHAZADO");
        creada.setEstado(ReservaService.PENDIENTE_PAGO);
        reservaService.liberarInventario(creada);
        reservaRepository.save(creada);

        Producto despuesDeclined = productoRepository.findById(prod.getId()).orElseThrow();
        assertEquals(1, despuesDeclined.getCantidad());
        assertTrue(despuesDeclined.getEnStock());

        // Retomamos el pago
        ResponseEntity<?> respRetomar = pagoController.retomar(creada.getId(), Map.of("telefono", "3115554433"));
        assertEquals(HttpStatus.OK, respRetomar.getStatusCode());

        // Al retomar, se debe re-reservar el stock
        Producto despuesRetomar = productoRepository.findById(prod.getId()).orElseThrow();
        assertEquals(0, despuesRetomar.getCantidad());
        assertFalse(despuesRetomar.getEnStock());
    }

    @Test
    @DisplayName("9. Reintentar Pago consumido: Cliente A pago rechazado -> stock liberado -> Cliente B lo compra -> Cliente A reintenta -> falla")
    void testReintentoPagoConsumidoPorOtro() {
        Producto prod = new Producto("Item Compartido", 25000.0, "Desc", "", true, 1);
        prod = productoRepository.save(prod);

        Reserva reqA = new Reserva();
        reqA.setNombreCliente("Cliente A");
        reqA.setTelefono("3116661122");
        reqA.setEmail("a@test.com");
        reqA.setCiudad("Cartagena");
        reqA.setMedioPago("WOMPI");
        reqA.setItemsInventario(List.of(new ItemReserva(prod.getId(), "producto", 1)));

        ResponseEntity<?> respA = reservaController.crear(reqA);
        Reserva creadaA = (Reserva) respA.getBody();

        reservaService.prepararPagoWompi(creadaA, "ref_wompi_A");

        // Rechazo pago Cliente A -> libera stock
        creadaA = reservaRepository.findById(creadaA.getId()).orElseThrow();
        creadaA.setEstadoPago("RECHAZADO");
        creadaA.setEstado(ReservaService.PENDIENTE_PAGO);
        reservaService.liberarInventario(creadaA);
        reservaRepository.save(creadaA);

        // Cliente B compra el item
        Reserva reqB = new Reserva();
        reqB.setNombreCliente("Cliente B");
        reqB.setTelefono("3116663344");
        reqB.setEmail("b@test.com");
        reqB.setCiudad("Cartagena");
        reqB.setMedioPago("WOMPI");
        reqB.setItemsInventario(List.of(new ItemReserva(prod.getId(), "producto", 1)));

        ResponseEntity<?> respB = reservaController.crear(reqB);
        assertEquals(HttpStatus.CREATED, respB.getStatusCode());

        // Cliente A reintenta retomar pago -> debe fallar con 400 Bad Request
        // indicando que el producto ya no está disponible.
        ResponseEntity<?> respRetomar = pagoController.retomar(creadaA.getId(), Map.of("telefono", "3116661122"));
        assertEquals(HttpStatus.BAD_REQUEST, respRetomar.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) respRetomar.getBody();
        assertNotNull(body);
        assertEquals("El producto ya no está disponible.", body.get("mensaje"));
    }

    @Test
    @DisplayName("10. Trazado y Demostración: Flujo completo de pago Wompi DECLINED con stock = 1")
    void testTraceDeFlujoCompletoConWompiDeclined() {
        System.out.println("=== INICIO TRAZA COMPLETA WOMPI DECLINED ===");
        
        // 1. Crear producto con stock inicial = 1
        Producto prod = new Producto("Item Demo Traza", 50000.0, "Desc", "", true, 1);
        prod = productoRepository.save(prod);
        System.out.println("[STOCK INICIAL] Nombre: " + prod.getNombre() + " | Stock: " + prod.getCantidad() + " | enStock: " + prod.getEnStock());
        assertEquals(1, prod.getCantidad());
        assertTrue(prod.getEnStock());

        // 2. Crear pedido puro
        Reserva req = new Reserva();
        req.setNombreCliente("Cliente Demo Traza");
        req.setTelefono("3159990022");
        req.setEmail("demo@traza.com");
        req.setCiudad("Cartagena");
        req.setMedioPago("WOMPI");
        req.setItemsInventario(List.of(new ItemReserva(prod.getId(), "producto", 1)));

        System.out.println("[PASO 1: POST /api/reservas] Creando reserva...");
        ResponseEntity<?> respCrear = reservaController.crear(req);
        assertEquals(HttpStatus.CREATED, respCrear.getStatusCode());
        Reserva creada = (Reserva) respCrear.getBody();
        assertNotNull(creada);
        System.out.println("Reserva creada -> ID: " + creada.getId() + " | Código: " + creada.getCodigoReserva() + " | Estado: " + creada.getEstado() + " | esPedidoPuro: " + creada.esPedidoPuro());
        System.out.println("inventarioReservado tras crear: " + creada.getInventarioReservado());
        assertTrue(creada.getInventarioReservado(), "El inventario debe marcarse como reservado");

        // Verificar stock en base de datos tras creación del pedido (debe ser 0)
        Producto despuesCrear = productoRepository.findById(prod.getId()).orElseThrow();
        System.out.println("[STOCK TRAS CHECKOUT] Cantidad: " + despuesCrear.getCantidad() + " | enStock: " + despuesCrear.getEnStock());
        assertEquals(0, despuesCrear.getCantidad());
        assertFalse(despuesCrear.getEnStock());

        // 3. Preparar pago Wompi
        System.out.println("[PASO 2: POST /api/pagos/wompi/preparar] Generando referencia Wompi...");
        ResponseEntity<?> respPrep = pagoController.preparar(creada.getId());
        assertEquals(HttpStatus.OK, respPrep.getStatusCode());
        creada = reservaRepository.findById(creada.getId()).orElseThrow();
        System.out.println("Referencia Wompi: " + creada.getReferenciaWompi() + " | Monto: " + creada.getMontoPagoCentavos() + " centavos");

        // 4. Simular webhook Wompi DECLINED
        System.out.println("[PASO 3: POST /api/pagos/wompi/webhook] Recibiendo estado DECLINED...");
        
        long timestamp = Instant.now().getEpochSecond();
        String secret = "test_events_QA_SECRET_456"; // Coincide con events-secret en TestPropertySource
        String txId = "tx_test_declined_traza";

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.node.ObjectNode evento = mapper.createObjectNode();
        evento.put("event", "transaction.updated");
        evento.put("timestamp", timestamp);
        evento.put("environment", "test");

        com.fasterxml.jackson.databind.node.ObjectNode dataNode = evento.putObject("data");
        com.fasterxml.jackson.databind.node.ObjectNode txNode = dataNode.putObject("transaction");
        txNode.put("id", txId);
        txNode.put("status", "DECLINED");
        txNode.put("amount_in_cents", creada.getMontoPagoCentavos());
        txNode.put("reference", creada.getReferenciaWompi());
        txNode.put("currency", "COP");
        txNode.put("payment_method_type", "CARD");

        com.fasterxml.jackson.databind.node.ObjectNode signature = evento.putObject("signature");
        com.fasterxml.jackson.databind.node.ArrayNode props = signature.putArray("properties");
        props.add("transaction.id");
        props.add("transaction.status");
        props.add("transaction.amount_in_cents");

        String concat = txId + "DECLINED" + creada.getMontoPagoCentavos() + timestamp + secret;
        signature.put("checksum", sha256(concat));

        // Llamamos al webhook
        ResponseEntity<?> respWebhook = pagoController.webhook(evento, null);
        assertEquals(HttpStatus.OK, respWebhook.getStatusCode());

        // Verificar cambios en el pedido
        creada = reservaRepository.findById(creada.getId()).orElseThrow();
        System.out.println("[WEBHOOK PROCESADO] Estado Pedido: " + creada.getEstado() + " | Estado Pago: " + creada.getEstadoPago() + " | inventarioReservado: " + creada.getInventarioReservado());
        assertEquals(ReservaService.PENDIENTE_PAGO, creada.getEstado());
        assertEquals("RECHAZADO", creada.getEstadoPago());
        assertFalse(creada.getInventarioReservado(), "inventarioReservado debe ser false");

        // Verificar restauración del stock en base de datos (debe ser 1, enStock = true)
        Producto despuesWebhook = productoRepository.findById(prod.getId()).orElseThrow();
        System.out.println("[STOCK RESTAURADO EN BD] Cantidad: " + despuesWebhook.getCantidad() + " | enStock: " + despuesWebhook.getEnStock());
        assertEquals(1, despuesWebhook.getCantidad());
        assertTrue(despuesWebhook.getEnStock());

        // 5. Simular webhook duplicado para probar idempotencia de liberación
        System.out.println("[PASO 4: Webhook DECLINED Duplicado] Recibiendo mismo evento...");
        ResponseEntity<?> respWebhookDup = pagoController.webhook(evento, null);
        assertEquals(HttpStatus.OK, respWebhookDup.getStatusCode());

        // El stock debe permanecer en 1 (no convertirse en 2)
        Producto despuesWebhookDup = productoRepository.findById(prod.getId()).orElseThrow();
        System.out.println("[STOCK TRAS DUPLICADO] Cantidad: " + despuesWebhookDup.getCantidad() + " | enStock: " + despuesWebhookDup.getEnStock());
        assertEquals(1, despuesWebhookDup.getCantidad(), "La cantidad no debe incrementarse en eventos duplicados");
        assertTrue(despuesWebhookDup.getEnStock());

        System.out.println("=== FIN TRAZA COMPLETA WOMPI DECLINED ===");
    }

    @Test
    @DisplayName("11. Deletion Security: Deleting confirmed/paid reservation should be blocked (400 Bad Request)")
    void testEliminarReservaConfirmadaBloqueado() {
        Reserva res = new Reserva();
        res.setNombreCliente("Cliente Inmune");
        res.setTelefono("3119999999");
        res.setEstado("Confirmado");
        res = reservaRepository.save(res);

        ResponseEntity<?> resp = reservaController.eliminar(res.getId());
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        
        // Confirmar que sigue existiendo en DB
        assertTrue(reservaRepository.findById(res.getId()).isPresent());
    }

    @Test
    @DisplayName("12. Deletion Security: Deleting unconfirmed reservation without financial foot print should delete physically")
    void testEliminarReservaSinHuellaFinancieraFisico() {
        Reserva res = new Reserva();
        res.setNombreCliente("Cliente Borrable");
        res.setTelefono("3118888888");
        res.setEstado(ReservaService.PENDIENTE_PAGO);
        res = reservaRepository.save(res);

        ResponseEntity<?> resp = reservaController.eliminar(res.getId());
        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        
        // Confirmar que fue eliminado
        assertFalse(reservaRepository.findById(res.getId()).isPresent());
    }

    @Test
    @DisplayName("13. Deletion Security: Deleting unconfirmed reservation with financial footprint should archive instead of delete physically")
    void testEliminarReservaConHuellaFinancieraArchivar() {
        Reserva res = new Reserva();
        res.setNombreCliente("Cliente Financiero");
        res.setTelefono("3117777777");
        res.setEstado(ReservaService.PENDIENTE_PAGO);
        res.setMedioPago("WOMPI");
        res.setReferenciaWompi("ISV-FINANCIERO-REF");
        res = reservaRepository.save(res);

        ResponseEntity<?> resp = reservaController.eliminar(res.getId());
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        
        // Confirmar que sigue existiendo en DB pero archivada
        Optional<Reserva> opt = reservaRepository.findById(res.getId());
        assertTrue(opt.isPresent());
        Reserva guardada = opt.get();
        assertTrue(guardada.getArchivada());
        assertNotNull(guardada.getFechaArchivado());
    }

    @Test
    @DisplayName("14. Diagnostic: Inspect ISV-1352")
    void testInspectISV1352() {
        System.out.println("=== DIAGNOSTIC ALL RESERVATIONS ===");
        List<Reserva> list = reservaRepository.findAll();
        System.out.println("Total reservations in DB: " + list.size());
        for (Reserva r : list) {
            System.out.println("Code: " + r.getCodigoReserva() + " | State: " + r.getEstado() + " | StatePago: " + r.getEstadoPago() + " | Med: " + r.getMedioPago());
        }
        System.out.println("=== END DIAGNOSTIC ===");
    }

    @Test
    @DisplayName("15. Expiración de Transferencia: Reserva en Pendiente Comprobante expira después de 24 horas y libera stock")
    void testExpiracionPendienteComprobante() {
        Producto prod = new Producto("Item Transf", 40000.0, "Desc", "", true, 1);
        prod = productoRepository.save(prod);
        
        long oldTimestamp = Instant.now().minusSeconds(2 * 24 * 3600).getEpochSecond();
        String hexTimestamp = Long.toHexString(oldTimestamp);
        String customId = hexTimestamp + "1234567890123456";
        
        Reserva req = new Reserva();
        req.setId(customId);
        req.setNombreCliente("Cliente Transferencia");
        req.setTelefono("3151112233");
        req.setMedioPago("TRANSFERENCIA");
        req.setItemsInventario(List.of(new ItemReserva(prod.getId(), "producto", 1)));
        
        ResponseEntity<?> respCrear = reservaController.crear(req);
        assertEquals(HttpStatus.CREATED, respCrear.getStatusCode());
        Reserva creada = (Reserva) respCrear.getBody();
        assertNotNull(creada);
        assertEquals(ReservaService.PENDIENTE, creada.getEstado());
        assertTrue(creada.getInventarioReservado());
        
        Producto despuesCrear = productoRepository.findById(prod.getId()).orElseThrow();
        assertEquals(0, despuesCrear.getCantidad());
        assertFalse(despuesCrear.getEnStock());
        
        assertTrue(reservaService.estaExpirada(creada));
        
        reservaService.expirarReservasVencidas();
        
        Reserva exp = reservaRepository.findById(customId).orElseThrow();
        assertEquals(ReservaService.EXPIRADA, exp.getEstado());
        assertFalse(exp.getInventarioReservado());
        
        Producto finalProd = productoRepository.findById(prod.getId()).orElseThrow();
        assertEquals(1, finalProd.getCantidad());
        assertTrue(finalProd.getEnStock());
        
        reservaRepository.delete(exp);
        productoRepository.delete(finalProd);
    }

    @Test
    @DisplayName("16. Idempotencia de Expiración: Correr expirarReservasVencidas múltiples veces no incrementa de más el stock")
    void testIdempotenciaExpiracion() {
        Producto prod = new Producto("Item Idemp", 30000.0, "Desc", "", true, 1);
        prod = productoRepository.save(prod);
        
        long oldTimestamp = Instant.now().minusSeconds(2 * 24 * 3600).getEpochSecond();
        String hexTimestamp = Long.toHexString(oldTimestamp);
        String customId = hexTimestamp + "1234567890123457";
        
        Reserva res = new Reserva();
        res.setId(customId);
        res.setNombreCliente("Cliente Idempotente");
        res.setTelefono("3152223344");
        res.setMedioPago("TRANSFERENCIA");
        res.setItemsInventario(List.of(new ItemReserva(prod.getId(), "producto", 1)));
        
        ResponseEntity<?> respCrear = reservaController.crear(res);
        assertEquals(HttpStatus.CREATED, respCrear.getStatusCode());
        Reserva creada = (Reserva) respCrear.getBody();
        assertNotNull(creada);
        assertEquals(ReservaService.PENDIENTE, creada.getEstado());
        assertTrue(creada.getInventarioReservado());
        
        // Forzar stock de partida a 0 en caso de que no haya quedado (debería ser 0)
        Producto despuesCrear = productoRepository.findById(prod.getId()).orElseThrow();
        assertEquals(0, despuesCrear.getCantidad());
        
        reservaService.expirarReservasVencidas();
        
        Producto prodP1 = productoRepository.findById(prod.getId()).orElseThrow();
        assertEquals(1, prodP1.getCantidad());
        assertTrue(prodP1.getEnStock());
        
        reservaService.expirarReservasVencidas();
        
        Producto prodP2 = productoRepository.findById(prod.getId()).orElseThrow();
        assertEquals(1, prodP2.getCantidad());
        
        reservaRepository.delete(creada);
        productoRepository.delete(prodP2);
    }

    @Test
    @DisplayName("17. Categoria Conflict: Deleting a category with products returns 409 Conflict")
    void testDeleteCategoryConflict() throws Exception {
        // 1. Crear categoría
        CategoriaProducto cat = new CategoriaProducto();
        cat.setNombre("Cat Conflictiva");
        cat.setActivo(true);
        cat = categoriaProductoRepository.save(cat);
        
        // 2. Crear producto asociado
        Producto prod = new Producto("Item Conf", 5000.0, "Desc", "img", true, 5);
        prod.setCategoriaId(cat.getId());
        prod = productoRepository.save(prod);
        
        // 3. Intentar eliminar categoría -> 409 Conflict
        ResponseEntity<?> resp = categoriaProductoController.eliminar(cat.getId());
        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
        
        // Limpiar
        productoRepository.delete(prod);
        categoriaProductoRepository.delete(cat);
    }

    @Test
    @DisplayName("18. Preparar constraints: pending payments cannot be prepared, Wompi APPROVED allows preparation")
    void testPrepararStateConstraints() throws Exception {
        // 1. Crear producto con stock
        Producto prod = new Producto("Item Prep", 10000.0, "Desc", "img", true, 5);
        prod = productoRepository.save(prod);
        
        // 2. Crear reserva en PENDIENTE
        Reserva res = new Reserva();
        res.setNombreCliente("Cliente Prep");
        res.setTelefono("573000000000");
        res.setTipoEntrega("pickup");
        res.setItemsInventario(List.of(new ItemReserva(prod.getId(), "producto", 1)));
        
        res = reservaService.prepararNuevaReserva(res);
        res.setCodigoReserva("ISV-PREP-01");
        res = reservaRepository.save(res);
        
        final String resId = res.getId();
        
        // Intentar preparar reserva en PENDIENTE -> debe fallar
        assertThrows(IllegalStateException.class, () -> {
            reservaService.marcarPedidoEnPreparacion(reservaRepository.findById(resId).orElseThrow(), "admin");
        });
        
        // Cambiar a PENDIENTE_PAGO
        Reserva res2 = reservaRepository.findById(resId).orElseThrow();
        res2 = reservaService.prepararPagoWompi(res2, "ref-prep-01");
        
        final String resId2 = res2.getId();
        // Intentar preparar reserva en PENDIENTE_PAGO -> debe fallar
        assertThrows(IllegalStateException.class, () -> {
            reservaService.marcarPedidoEnPreparacion(reservaRepository.findById(resId2).orElseThrow(), "admin");
        });
        
        // Confirmar pago (APPROVED)
        Reserva res3 = reservaRepository.findById(resId2).orElseThrow();
        res3 = reservaService.confirmarPagoWompi(res3);
        
        // Ahora sí debe permitir preparar
        Reserva preparada = reservaService.marcarPedidoEnPreparacion(res3, "admin");
        assertEquals("EN_PREPARACION", preparada.getEstadoPedido());
        assertEquals("Pago Confirmado", preparada.getEstado());
        
        // Limpiar
        reservaRepository.delete(preparada);
        productoRepository.delete(prod);
    }

    @Test
    @DisplayName("Validación del Ciclo de Retención Temporal, Modificaciones Atómicas y Concurrencia (V2)")
    public void testHoldTemporalDeProductosYModificaciones() {
        Producto pA = new Producto("Producto A", 20000.0, "Desc A", "", true, 2);
        pA = productoRepository.save(pA);

        Producto pB = new Producto("Producto B", 15000.0, "Desc B", "", true, 1);
        pB = productoRepository.save(pB);

        Reserva res = new Reserva();
        res.setNombreCliente("Juan");
        res.setTelefono("3001234567");
        res.setEmail("juan@mail.com");
        res.setSubtotal(20000.0);
        res.setAnticipo(0.0);
        res.setSaldo(20000.0);
        res.setEstado("Pendiente Pago");
        res.setItems(List.of("Producto A"));
        
        ItemReserva itemA = new ItemReserva(pA.getId(), "producto", 1);
        itemA.setNombre("Producto A");
        res.setItemsInventario(List.of(itemA));
        res = reservaRepository.save(res);

        reservaService.reservarInventario(res);
        res = reservaRepository.save(res);
        res = reservaRepository.findById(res.getId()).orElseThrow();
        assertTrue(res.getInventarioReservado());

        Producto pA_post = productoRepository.findById(pA.getId()).orElseThrow();
        assertEquals(1, pA_post.getCantidad());

        ItemReserva itemB = new ItemReserva(pB.getId(), "producto", 1);
        itemB.setNombre("Producto B");
        List<ItemReserva> nuevosItems = List.of(itemA, itemB);

        reservaService.actualizarItemsDeReserva(res, nuevosItems);
        res = reservaRepository.findById(res.getId()).orElseThrow();

        Producto pA_post2 = productoRepository.findById(pA.getId()).orElseThrow();
        Producto pB_post2 = productoRepository.findById(pB.getId()).orElseThrow();
        assertEquals(1, pA_post2.getCantidad());
        assertEquals(0, pB_post2.getCantidad());

        ItemReserva itemB_excesivo = new ItemReserva(pB.getId(), "producto", 2);
        itemB_excesivo.setNombre("Producto B");
        List<ItemReserva> nuevosItemsFallidos = List.of(itemA, itemB_excesivo);

        final String resId = res.getId();
        assertThrows(IllegalStateException.class, () -> {
            reservaService.actualizarItemsDeReserva(reservaRepository.findById(resId).orElseThrow(), nuevosItemsFallidos);
        });

        assertEquals(1, productoRepository.findById(pA.getId()).orElseThrow().getCantidad());
        assertEquals(0, productoRepository.findById(pB.getId()).orElseThrow().getCantidad());

        res = reservaService.confirmarPagoWompi(res);
        assertEquals("Pago Confirmado", res.getEstado());
        assertEquals("APROBADO", res.getEstadoPago());

        assertEquals(1, productoRepository.findById(pA.getId()).orElseThrow().getCantidad());
        assertEquals(0, productoRepository.findById(pB.getId()).orElseThrow().getCantidad());

        res.setEstado("Pendiente Pago");
        res.setEstadoPago(null);
        res = reservaRepository.save(res);
        
        reservaService.liberarInventario(res);
        
        assertEquals(2, productoRepository.findById(pA.getId()).orElseThrow().getCantidad());
        assertEquals(1, productoRepository.findById(pB.getId()).orElseThrow().getCantidad());
    }

    private String sha256(String valor) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(valor.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }
}
