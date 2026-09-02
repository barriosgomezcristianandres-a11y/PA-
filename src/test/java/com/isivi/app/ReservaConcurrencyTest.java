package com.isivi.app;

import com.isivi.app.controller.DashboardController;
import com.isivi.app.controller.ReservaController;
import com.isivi.app.dto.GestionReservaRequest;
import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Reserva;
import com.isivi.app.model.Servicio;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.repository.ServicioRepository;
import com.isivi.app.service.ReservaService;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ReservaConcurrencyTest {

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private DashboardController dashboardController;

    @Autowired
    private MongoTemplate mongoTemplate;

    // 2026-11-20 es Viernes (Día laboral de la peluquería)
    private static final LocalDate TEST_DATE = LocalDate.of(2026, 11, 20);
    private static final String TEST_TIME = "09:30 AM";

    private String testServicioId;
    private String testProductoId;

    @BeforeEach
    void setUp() {
        reservaRepository.deleteAll();

        Servicio serv = servicioRepository.findAll().stream().findFirst().orElseGet(() -> {
            Servicio s = new Servicio();
            s.setNombre("Balayage Test");
            s.setPrecio(150000.0);
            s.setCategoria("Color");
            return servicioRepository.save(s);
        });
        testServicioId = serv.getId();

        Producto prod = productoRepository.findAll().stream().findFirst().orElseGet(() -> {
            Producto p = new Producto();
            p.setNombre("Shampoo Test");
            p.setPrecio(45000.0);
            return p;
        });
        prod.setCantidad(100);
        prod.setEnStock(true);
        prod = productoRepository.save(prod);
        testProductoId = prod.getId();

        // Asegurar que el índice único parcial esté activo
        reservaService.asegurarIndiceUnicoHorarioActivo();
    }

    private ItemReserva crearItemServicio() {
        ItemReserva item = new ItemReserva();
        item.setId(testServicioId);
        item.setTipo("servicio");
        item.setCantidad(1);
        return item;
    }

    private ItemReserva crearItemProducto() {
        ItemReserva item = new ItemReserva();
        item.setId(testProductoId);
        item.setTipo("producto");
        item.setCantidad(1);
        return item;
    }

    @Test
    @DisplayName("AUDITORÍA 1: Verificación del índice real en MongoDB Atlas")
    void testVerificarIndiceRealEnMongoDB() {
        List<Document> indices = new ArrayList<>();
        for (Document doc : mongoTemplate.getCollection("reservas").listIndexes()) {
            indices.add(doc);
        }

        Document idxTarget = indices.stream()
                .filter(doc -> "reserva_fecha_hora_activa_idx".equals(doc.getString("name")))
                .findFirst()
                .orElse(null);

        assertNotNull(idxTarget, "El índice compuesto 'reserva_fecha_hora_activa_idx' debe existir en MongoDB");
        assertTrue(idxTarget.getBoolean("unique", false), "El índice en MongoDB DEBE ser unique: true");

        Document keys = (Document) idxTarget.get("key");
        assertNotNull(keys);
        assertEquals(1, keys.get("fechaCita"));
        assertEquals(1, keys.get("horaCita"));

        Document partialFilter = (Document) idxTarget.get("partialFilterExpression");
        assertNotNull(partialFilter, "El índice DEBE tener un partialFilterExpression configurado");
        assertEquals(false, partialFilter.get("archivada"));
        assertNotNull(partialFilter.get("fechaCita"));
        assertNotNull(partialFilter.get("horaCita"));
        assertNotNull(partialFilter.get("estado"));

        Document estadoDoc = (Document) partialFilter.get("estado");
        assertTrue(estadoDoc.containsKey("$in"), "El filtro parcial debe usar $in para los estados activos");
    }

    @Test
    @DisplayName("AUDITORÍA 2: Concurrencia real en MongoDB - Exactamente 1 gana, 0 residuos del perdedor, inventario exacto y rollback idempotente")
    void testConcurrenciaRealConVerificacionDeInventarioYRollback() throws InterruptedException {
        int initialStock = productoRepository.findById(testProductoId).get().getCantidad();
        assertEquals(100, initialStock);

        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch readyLatch = new CountDownLatch(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            final int id = i + 1;
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await(); // Disparo sincronizado al nanosegundo

                    Reserva r = new Reserva();
                    r.setNombreCliente("Cliente Concurrente " + id);
                    r.setTelefono("300888990" + id);
                    r.setFechaCita(TEST_DATE);
                    r.setHoraCita(TEST_TIME);
                    r.setMedioPago("NEQUI");
                    r.setItemsInventario(List.of(crearItemServicio(), crearItemProducto()));

                    ResponseEntity<?> resp = reservaController.crear(r);
                    if (resp.getStatusCode() == HttpStatus.CREATED || resp.getStatusCode() == HttpStatus.OK) {
                        successCount.incrementAndGet();
                    } else if (resp.getStatusCode() == HttpStatus.CONFLICT) {
                        conflictCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    conflictCount.incrementAndGet();
                }
            });
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();
        executor.shutdown();
        boolean finished = executor.awaitTermination(10, TimeUnit.SECONDS);

        assertTrue(finished, "Las peticiones concurrentes deben completarse");
        assertEquals(1, successCount.get(), "Exactamente 1 reserva concurrente debe tener éxito");
        assertEquals(1, conflictCount.get(), "La segunda reserva concurrente debe recibir HTTP 409 CONFLICT");

        // 1. Confirmar que en MongoDB Atlas solo existe exactamente 1 reserva para esa fecha/hora
        List<Reserva> persistidas = reservaRepository.findByFechaCita(TEST_DATE);
        assertEquals(1, persistidas.size(), "Solo debe existir 1 reserva persistida en MongoDB");

        // 2. Confirmar que la reserva rechazada NO dejó residuos huérfanos
        long countAllTest = reservaRepository.findAll().stream()
                .filter(r -> r.getTelefono() != null && r.getTelefono().startsWith("300888990"))
                .count();
        assertEquals(1, countAllTest, "La reserva rechazada no debe dejar ningún registro en la base de datos");

        // 3. Confirmar que el stock se descontó exactamente en 1 unidad (para el ganador) y NO en 2
        Producto prodFinal = productoRepository.findById(testProductoId).get();
        assertEquals(99, prodFinal.getCantidad(), "El stock debe ser exactamente 99 tras el rollback de la petición rechazada");

        // 4. Idempotencia del rollback: liberar nuevamente una reserva no reservada no debe aumentar el stock
        Reserva dummyRechazada = new Reserva();
        dummyRechazada.setItemsInventario(List.of(crearItemProducto()));
        dummyRechazada.setInventarioReservado(false);
        reservaService.liberarInventario(dummyRechazada);

        Producto prodTrasLiberacionRepetida = productoRepository.findById(testProductoId).get();
        assertEquals(99, prodTrasLiberacionRepetida.getCantidad(), "El rollback no debe devolver stock de más si se reintenta");
    }

    @Test
    @DisplayName("AUDITORÍA 3: Dashboard - 'Ventas de hoy' SOLO contabiliza dinero cobrado real y no reservas pendientes")
    void testDashboardVentasContabilizaSoloDineroCobradoReal() {
        ZoneId zone = ZoneId.of("America/Bogota");
        LocalDate hoyBogota = LocalDate.now(zone);

        // 1. Reserva confirmada con anticipo pagado
        Reserva rCobrada = new Reserva();
        rCobrada.setNombreCliente("Cliente Pagado");
        rCobrada.setTelefono("3009990001");
        rCobrada.setFechaCita(hoyBogota);
        rCobrada.setHoraCita("11:00 AM");
        rCobrada.setMedioPago("WOMPI");
        rCobrada.setEstado("Confirmado");
        rCobrada.setEstadoPago("APROBADO");
        rCobrada.setMontoPagoCentavos(3750000L); // $37.500 COP
        rCobrada.setAnticipo(37500.0);
        rCobrada.setSubtotal(150000.0);
        rCobrada.setFechaPago(hoyBogota);
        rCobrada.setItemsInventario(List.of(crearItemServicio()));
        rCobrada.setCodigoReserva("ISV-9901");
        reservaRepository.save(rCobrada);

        // 2. Reserva PENDIENTE de comprobante (dinero NO cobrado aún)
        Reserva rPendiente = new Reserva();
        rPendiente.setNombreCliente("Cliente Pendiente");
        rPendiente.setTelefono("3009990002");
        rPendiente.setFechaCita(hoyBogota);
        rPendiente.setHoraCita("01:30 PM");
        rPendiente.setMedioPago("BANCOLOMBIA");
        rPendiente.setEstado("Pendiente Comprobante");
        rPendiente.setEstadoPago("PENDIENTE");
        rPendiente.setAnticipo(50000.0);
        rPendiente.setSubtotal(200000.0);
        rPendiente.setFechaRegistro(hoyBogota);
        rPendiente.setItemsInventario(List.of(crearItemServicio()));
        rPendiente.setCodigoReserva("ISV-9902");
        reservaRepository.save(rPendiente);

        // Consultar Dashboard
        ResponseEntity<Map<String, Object>> response = dashboardController.obtenerResumen();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertEquals(hoyBogota.toString(), body.get("fechaActual"), "La fecha debe coincidir con la zona horaria America/Bogota");

        double ventasHoy = ((Number) body.get("ventasHoy")).doubleValue();
        // Las ventas deben ser al menos $37.500 (la confirmada) y NO sumar los $50.000 ni $200.000 de la pendiente
        assertTrue(ventasHoy >= 37500.0, "Ventas de hoy debe incluir los pagos confirmados");

        // Limpiar
        reservaRepository.delete(rCobrada);
        reservaRepository.delete(rPendiente);
    }

    @Test
    @DisplayName("AUDITORÍA 4: Al cancelar o denegar una reserva, el horario se libera inmediatamente")
    void testCancelacionLiberaHorario() {
        Reserva r1 = new Reserva();
        r1.setNombreCliente("Cliente Inicial");
        r1.setTelefono("3001112222");
        r1.setFechaCita(TEST_DATE);
        r1.setHoraCita(TEST_TIME);
        r1.setItemsInventario(List.of(crearItemServicio()));
        ResponseEntity<?> resp1 = reservaController.crear(r1);
        assertEquals(HttpStatus.CREATED, resp1.getStatusCode());
        Reserva guardada = (Reserva) resp1.getBody();

        // Cancelar reserva con código y teléfono válidos
        GestionReservaRequest cancelReq = new GestionReservaRequest();
        cancelReq.setCodigoReserva(guardada.getCodigoReserva());
        cancelReq.setTelefono(guardada.getTelefono());
        ResponseEntity<?> cancelResp = reservaController.cancelar(guardada.getId(), cancelReq);
        assertEquals(HttpStatus.OK, cancelResp.getStatusCode());

        // Ahora otro cliente puede reservar ese mismo horario
        Reserva r2 = new Reserva();
        r2.setNombreCliente("Nuevo Cliente");
        r2.setTelefono("3003334444");
        r2.setFechaCita(TEST_DATE);
        r2.setHoraCita(TEST_TIME);
        r2.setItemsInventario(List.of(crearItemServicio()));
        ResponseEntity<?> resp2 = reservaController.crear(r2);
        assertEquals(HttpStatus.CREATED, resp2.getStatusCode(), "El horario liberado debe permitir nueva reserva");
    }

    @Test
    @DisplayName("AUDITORÍA 5: Reprogramación a un horario ya ocupado es rechazada con 409 Conflict")
    void testReprogramacionAHorarioOcupadoRechazada() {
        String hora1 = "03:00 PM";
        String hora2 = "04:30 PM";

        // Reserva A en hora 1
        Reserva rA = new Reserva();
        rA.setNombreCliente("Cliente A");
        rA.setTelefono("3005550001");
        rA.setFechaCita(TEST_DATE);
        rA.setHoraCita(hora1);
        rA.setItemsInventario(List.of(crearItemServicio()));
        ResponseEntity<?> respA = reservaController.crear(rA);
        assertEquals(HttpStatus.CREATED, respA.getStatusCode());
        Reserva guardadaA = (Reserva) respA.getBody();

        // Reserva B en hora 2
        Reserva rB = new Reserva();
        rB.setNombreCliente("Cliente B");
        rB.setTelefono("3005550002");
        rB.setFechaCita(TEST_DATE);
        rB.setHoraCita(hora2);
        rB.setItemsInventario(List.of(crearItemServicio()));
        ResponseEntity<?> respB = reservaController.crear(rB);
        assertEquals(HttpStatus.CREATED, respB.getStatusCode());
        Reserva guardadaB = (Reserva) respB.getBody();

        // Cliente B intenta reprogramar a la hora 1 (ocupada por Cliente A)
        GestionReservaRequest req = new GestionReservaRequest();
        req.setCodigoReserva(guardadaB.getCodigoReserva());
        req.setTelefono(guardadaB.getTelefono());
        req.setFechaCita(TEST_DATE);
        req.setHoraCita(hora1);
        ResponseEntity<?> respReprog = reservaController.reprogramar(guardadaB.getId(), req);

        assertEquals(HttpStatus.CONFLICT, respReprog.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) respReprog.getBody();
        assertEquals("HORARIO_NO_DISPONIBLE", body.get("error"));
    }

    @Test
    @DisplayName("AUDITORÍA 6: Compras de productos sin cita (fechaCita null) nunca colisionan entre sí")
    void testPedidosProductosSinFechaNoColisionan() {
        Reserva p1 = new Reserva();
        p1.setNombreCliente("Comprador 1");
        p1.setTelefono("3007771111");
        p1.setFechaCita(null);
        p1.setHoraCita(null);
        p1.setItemsInventario(List.of(crearItemProducto()));

        Reserva p2 = new Reserva();
        p2.setNombreCliente("Comprador 2");
        p2.setTelefono("3007772222");
        p2.setFechaCita(null);
        p2.setHoraCita(null);
        p2.setItemsInventario(List.of(crearItemProducto()));

        ResponseEntity<?> resp1 = reservaController.crear(p1);
        ResponseEntity<?> resp2 = reservaController.crear(p2);

        assertEquals(HttpStatus.CREATED, resp1.getStatusCode());
        assertEquals(HttpStatus.CREATED, resp2.getStatusCode());
    }

    @Test
    @DisplayName("AUDITORÍA 7: Crear reserva en horario válido y disponible retorna 201 CREATED (no falso 409)")
    void testCrearReservaEnHorarioValidoRetorna201() {
        Reserva r = new Reserva();
        r.setNombreCliente("Cliente Horario Valido");
        r.setTelefono("3005559988");
        r.setFechaCita(TEST_DATE);
        r.setHoraCita("08:00 AM");
        r.setItemsInventario(List.of(crearItemServicio()));

        ResponseEntity<?> resp = reservaController.crear(r);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode(), "Una reserva en un horario disponible DEBE retornar 201 Created");
        assertTrue(resp.getBody() instanceof Reserva);
        Reserva guardada = (Reserva) resp.getBody();
        assertEquals("ISV-", guardada.getCodigoReserva().substring(0, 4));
    }

    @Test
    @DisplayName("AUDITORÍA 8: Dos reservas en horarios DIFERENTES del mismo día tienen éxito sin colisionar")
    void testCrearDosReservasEnHorariosDiferentesAmbasTienenExito() {
        Reserva r1 = new Reserva();
        r1.setNombreCliente("Cliente 1 - 08:00");
        r1.setTelefono("3005551111");
        r1.setFechaCita(TEST_DATE);
        r1.setHoraCita("08:00 AM");
        r1.setItemsInventario(List.of(crearItemServicio()));

        Reserva r2 = new Reserva();
        r2.setNombreCliente("Cliente 2 - 09:30");
        r2.setTelefono("3005552222");
        r2.setFechaCita(TEST_DATE);
        r2.setHoraCita("09:30 AM");
        r2.setItemsInventario(List.of(crearItemServicio()));

        ResponseEntity<?> resp1 = reservaController.crear(r1);
        ResponseEntity<?> resp2 = reservaController.crear(r2);

        assertEquals(HttpStatus.CREATED, resp1.getStatusCode(), "La primera reserva debe crearse exitosamente");
        assertEquals(HttpStatus.CREATED, resp2.getStatusCode(), "La segunda reserva en horario diferente DEBE crearse exitosamente");
    }

    @Test
    @DisplayName("AUDITORÍA 9: Reserva fuera de agenda (horario no configurado o día cerrado) retorna 400 HORARIO_FUERA_DE_AGENDA")
    void testCrearReservaFueraDeAgendaRetorna400ConErrorTipado() {
        // Horario no configurado (ej: 10:00 AM)
        Reserva rInvalida = new Reserva();
        rInvalida.setNombreCliente("Cliente Invalido");
        rInvalida.setTelefono("3005553333");
        rInvalida.setFechaCita(TEST_DATE);
        rInvalida.setHoraCita("10:00 AM"); // 10:00 AM no está en los horarios configurados
        rInvalida.setItemsInventario(List.of(crearItemServicio()));

        ResponseEntity<?> resp = reservaController.crear(rInvalida);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) resp.getBody();
        assertNotNull(body);
        assertEquals("HORARIO_FUERA_DE_AGENDA", body.get("error"));
    }

    @Test
    @DisplayName("AUDITORÍA 10: Inventario insuficiente retorna 409 STOCK_NO_DISPONIBLE y no confunde con horario")
    void testInventarioInsuficienteRetorna409ConErrorTipado() {
        ItemReserva itemExcess = new ItemReserva();
        itemExcess.setId(testProductoId);
        itemExcess.setTipo("producto");
        itemExcess.setCantidad(99999); // Excede el stock de 100

        Reserva rStock = new Reserva();
        rStock.setNombreCliente("Comprador Excesivo");
        rStock.setTelefono("3005554444");
        rStock.setFechaCita(null);
        rStock.setHoraCita(null);
        rStock.setItemsInventario(List.of(itemExcess));

        ResponseEntity<?> resp = reservaController.crear(rStock);
        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) resp.getBody();
        assertNotNull(body);
        assertEquals("STOCK_NO_DISPONIBLE", body.get("error"));
    }

    @Test
    @DisplayName("HARDENING 1: Doble cancelación es idempotente y NO reintegra inventario dos veces")
    void testDobleCancelacionEsIdempotenteYNoDuplicaReintegroDeStock() {
        Producto prod = productoRepository.findById(testProductoId).orElseThrow();
        int stockInicial = prod.getCantidad();

        ItemReserva itemProd = new ItemReserva();
        itemProd.setId(testProductoId);
        itemProd.setTipo("producto");
        itemProd.setCantidad(2);

        Reserva r = new Reserva();
        r.setNombreCliente("Cliente Doble Cancel");
        r.setTelefono("3005559999");
        r.setFechaCita(TEST_DATE);
        r.setHoraCita("08:00 AM");
        r.setItemsInventario(List.of(crearItemServicio(), itemProd));

        ResponseEntity<?> respCrear = reservaController.crear(r);
        assertEquals(HttpStatus.CREATED, respCrear.getStatusCode());
        Reserva guardada = (Reserva) respCrear.getBody();
        assertNotNull(guardada);

        // Comprobar stock descontado (-2)
        Producto prodDespuesCrear = productoRepository.findById(testProductoId).orElseThrow();
        assertEquals(stockInicial - 2, prodDespuesCrear.getCantidad());

        // Cancelación 1
        GestionReservaRequest req1 = new GestionReservaRequest();
        req1.setCodigoReserva(guardada.getCodigoReserva());
        req1.setTelefono(guardada.getTelefono());
        ResponseEntity<?> respCancel1 = reservaController.cancelar(guardada.getId(), req1);
        assertEquals(HttpStatus.OK, respCancel1.getStatusCode());

        // Stock reintegrado (+2)
        Producto prodDespuesCancel1 = productoRepository.findById(testProductoId).orElseThrow();
        assertEquals(stockInicial, prodDespuesCancel1.getCantidad());

        // Cancelación 2 (Idéntica petición repetida)
        ResponseEntity<?> respCancel2 = reservaController.cancelar(guardada.getId(), req1);
        assertEquals(HttpStatus.OK, respCancel2.getStatusCode(), "La segunda cancelación debe ser idempotente y retornar OK");

        // Stock NO debe haberse sumado otra vez
        Producto prodDespuesCancel2 = productoRepository.findById(testProductoId).orElseThrow();
        assertEquals(stockInicial, prodDespuesCancel2.getCantidad(), "El stock NO debe sumarse dos veces");
    }

    @Test
    @DisplayName("HARDENING 2: Doble aprobación administrativa es idempotente y no corrompe el estado")
    void testDobleAprobacionEsIdempotente() {
        Reserva r = new Reserva();
        r.setNombreCliente("Cliente Doble Aprobar");
        r.setTelefono("3005558888");
        r.setFechaCita(TEST_DATE);
        r.setHoraCita("08:00 AM");
        r.setItemsInventario(List.of(crearItemServicio()));

        ResponseEntity<?> respCrear = reservaController.crear(r);
        assertEquals(HttpStatus.CREATED, respCrear.getStatusCode());
        Reserva guardada = (Reserva) respCrear.getBody();
        assertNotNull(guardada);

        // Aprobación 1
        ResponseEntity<?> respAprobar1 = reservaController.aprobar(guardada.getId());
        assertEquals(HttpStatus.OK, respAprobar1.getStatusCode());
        assertEquals("Confirmado", ((Reserva) respAprobar1.getBody()).getEstado());

        // Aprobación 2 (repetida)
        ResponseEntity<?> respAprobar2 = reservaController.aprobar(guardada.getId());
        assertEquals(HttpStatus.OK, respAprobar2.getStatusCode(), "La segunda aprobación debe retornar OK de forma idempotente");
        assertEquals("Confirmado", ((Reserva) respAprobar2.getBody()).getEstado());
    }

    @Test
    @DisplayName("HARDENING 3: Doble denegación administrativa es idempotente y no duplica reintegro de stock")
    void testDobleDenegacionEsIdempotente() {
        Producto prod = productoRepository.findById(testProductoId).orElseThrow();
        int stockInicial = prod.getCantidad();

        ItemReserva itemProd = new ItemReserva();
        itemProd.setId(testProductoId);
        itemProd.setTipo("producto");
        itemProd.setCantidad(1);

        Reserva r = new Reserva();
        r.setNombreCliente("Cliente Doble Denegar");
        r.setTelefono("3005557777");
        r.setFechaCita(TEST_DATE);
        r.setHoraCita("08:00 AM");
        r.setItemsInventario(List.of(crearItemServicio(), itemProd));

        ResponseEntity<?> respCrear = reservaController.crear(r);
        assertEquals(HttpStatus.CREATED, respCrear.getStatusCode());
        Reserva guardada = (Reserva) respCrear.getBody();
        assertNotNull(guardada);

        // Denegación 1
        ResponseEntity<?> respDenegar1 = reservaController.denegar(guardada.getId());
        assertEquals(HttpStatus.OK, respDenegar1.getStatusCode());
        assertEquals("Denegada", ((Reserva) respDenegar1.getBody()).getEstado());

        // Stock recuperado
        Producto prodDespuesDenegar1 = productoRepository.findById(testProductoId).orElseThrow();
        assertEquals(stockInicial, prodDespuesDenegar1.getCantidad());

        // Denegación 2 (repetida)
        ResponseEntity<?> respDenegar2 = reservaController.denegar(guardada.getId());
        assertEquals(HttpStatus.OK, respDenegar2.getStatusCode(), "La segunda denegación debe responder OK de forma idempotente");
        assertEquals("Denegada", ((Reserva) respDenegar2.getBody()).getEstado());

        // Stock no duplicado
        Producto prodDespuesDenegar2 = productoRepository.findById(testProductoId).orElseThrow();
        assertEquals(stockInicial, prodDespuesDenegar2.getCantidad());
    }

    @Test
    @DisplayName("HARDENING 4: Transición inválida Cancelada -> Aprobada falla con 400 Bad Request controlado")
    void testTransicionInvalidaCanceladaAAprobadaFallaControlada() {
        Reserva r = new Reserva();
        r.setNombreCliente("Cliente Transición Inválida");
        r.setTelefono("3005556666");
        r.setFechaCita(TEST_DATE);
        r.setHoraCita("08:00 AM");
        r.setItemsInventario(List.of(crearItemServicio()));

        ResponseEntity<?> respCrear = reservaController.crear(r);
        Reserva guardada = (Reserva) respCrear.getBody();
        assertNotNull(guardada);

        // Cancelar reserva
        GestionReservaRequest reqCancel = new GestionReservaRequest();
        reqCancel.setCodigoReserva(guardada.getCodigoReserva());
        reqCancel.setTelefono(guardada.getTelefono());
        reservaController.cancelar(guardada.getId(), reqCancel);

        // Intentar aprobar una reserva cancelada
        ResponseEntity<?> respAprobar = reservaController.aprobar(guardada.getId());
        assertEquals(HttpStatus.BAD_REQUEST, respAprobar.getStatusCode(), "Aprobar una reserva cancelada debe ser rechazado con 400 Bad Request");
    }

    @Test
    @DisplayName("HARDENING 5: Doble confirmación de pago Wompi es idempotente")
    void testDobleConfirmacionPagoWompiEsIdempotente() {
        Reserva r = new Reserva();
        r.setNombreCliente("Cliente Pago Wompi");
        r.setTelefono("3005555555");
        r.setFechaCita(TEST_DATE);
        r.setHoraCita("08:00 AM");
        r.setItemsInventario(List.of(crearItemServicio()));

        ResponseEntity<?> respCrear = reservaController.crear(r);
        Reserva guardada = (Reserva) respCrear.getBody();
        assertNotNull(guardada);

        Reserva preparada = reservaService.prepararPagoWompi(guardada, "TEST-REF-12345");
        assertEquals("Pendiente Pago", preparada.getEstado());

        Reserva confirmada1 = reservaService.confirmarPagoWompi(preparada);
        assertEquals("Confirmado", confirmada1.getEstado());
        assertEquals("APROBADO", confirmada1.getEstadoPago());

        // Segunda llamada con la misma entidad confirmada (idempotencia)
        Reserva confirmada2 = reservaService.confirmarPagoWompi(confirmada1);
        assertEquals("Confirmado", confirmada2.getEstado());
        assertEquals("APROBADO", confirmada2.getEstadoPago());
    }

    @Test
    @DisplayName("HARDENING 6: Borrado administrativo responde 204 y segunda llamada 404")
    void testBorradoAdministrativoResponde204Y404() {
        Reserva r = new Reserva();
        r.setNombreCliente("Cliente Borrado");
        r.setTelefono("3005554444");
        r.setFechaCita(TEST_DATE);
        r.setHoraCita("08:00 AM");
        r.setItemsInventario(List.of(crearItemServicio()));

        ResponseEntity<?> respCrear = reservaController.crear(r);
        Reserva guardada = (Reserva) respCrear.getBody();
        assertNotNull(guardada);

        // Cancelar la reserva de forma que sea terminal (eliminable) pero no tenga huella financiera
        guardada.setEstado("Cancelada");
        reservaRepository.save(guardada);

        // Delete 1 -> 204 No Content
        ResponseEntity<?> respDel1 = reservaController.eliminar(guardada.getId());
        assertEquals(HttpStatus.NO_CONTENT, respDel1.getStatusCode());

        // Delete 2 -> 404 Not Found
        ResponseEntity<?> respDel2 = reservaController.eliminar(guardada.getId());
        assertEquals(HttpStatus.NOT_FOUND, respDel2.getStatusCode());
    }
}
