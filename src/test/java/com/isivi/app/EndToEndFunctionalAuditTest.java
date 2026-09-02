package com.isivi.app;

import com.isivi.app.controller.*;
import com.isivi.app.dto.GestionReservaRequest;
import com.isivi.app.exception.CancelacionNoPermitidaException;
import com.isivi.app.model.*;
import com.isivi.app.repository.*;
import com.isivi.app.security.JwtService;
import com.isivi.app.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class EndToEndFunctionalAuditTest {

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ProductoController productoController;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ServicioController servicioController;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private KitController kitController;

    @Autowired
    private KitRepository kitRepository;

    @Autowired
    private CategoriaProductoController categoriaProductoController;

    @Autowired
    private CategoriaProductoRepository categoriaProductoRepository;

    @Autowired
    private DashboardController dashboardController;

    @Autowired
    private PagoController pagoController;

    @Autowired
    private JwtService jwtService;

    private UsernamePasswordAuthenticationToken adminAuth;

    private LocalDate getNextValidDay(int minDaysAhead) {
        LocalDate next = LocalDate.now().plusDays(minDaysAhead);
        while (next.getDayOfWeek() == DayOfWeek.MONDAY) {
            next = next.plusDays(1);
        }
        return next;
    }

    @BeforeEach
    public void setup() {
        adminAuth = new UsernamePasswordAuthenticationToken("admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    // ==========================================
    // FASE 2: PERFIL CLIENTE — ESCENARIOS E2E
    // ==========================================

    @Test
    @DisplayName("Cliente Escenario A: Catálogo Público y Categorías Dinámicas")
    public void testClientScenarioA_CatalogAndCategories() {
        // Verificar que el catálogo público responde correctamente
        List<Producto> prods = productoController.listar();
        assertNotNull(prods);

        List<Kit> kits = kitController.listar();
        assertNotNull(kits);

        List<Servicio> servs = servicioController.listar(null);
        assertNotNull(servs);

        List<CategoriaProducto> cats = categoriaProductoController.listar(true, null);
        assertNotNull(cats);
    }

    @Test
    @DisplayName("Cliente Escenario B: Compra de Producto Simple (Pickup)")
    public void testClientScenarioB_SimpleProductPickup() {
        Producto prod = new Producto();
        prod.setNombre("Shampoo Audit Pickup " + UUID.randomUUID().toString().substring(0, 6));
        prod.setPrecio(28000.0);
        prod.setCantidad(10);
        prod.setEnStock(true);
        prod = productoRepository.save(prod);

        Reserva pedido = new Reserva();
        pedido.setNombreCliente("Cliente Pickup Real");
        pedido.setTelefono("3101234567");
        pedido.setEmail("pickup@isivi.test");
        pedido.setCiudad("Cartagena");
        pedido.setMedioPago("WOMPI");
        pedido.setTipoEntrega("pickup");

        ItemReserva item = new ItemReserva();
        item.setId(prod.getId());
        item.setTipo("producto");
        item.setNombre(prod.getNombre());
        item.setCantidad(2);
        item.setPrecioUnitario(28000.0);
        pedido.setItemsInventario(List.of(item));

        ResponseEntity<?> response = reservaController.crear(pedido);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Reserva guardada = (Reserva) response.getBody();
        assertNotNull(guardada);

        // Verificaciones
        assertTrue(guardada.esPedidoPuro(), "Debe ser pedido puro");
        assertFalse(guardada.esCita(), "No debe ser cita");
        assertTrue(guardada.esRecogida(), "Debe ser recogida en tienda");
        assertNull(guardada.getFechaCita(), "Fecha de cita debe ser null");
        assertNull(guardada.getHoraCita(), "Hora de cita debe ser null");
        assertEquals(56000.0, guardada.getSubtotal(), "Subtotal = 2 * 28000 = 56000");
        assertEquals(0.0, guardada.getAnticipo(), "Anticipo de pedido es 0 (se paga 100% en checkout)");

        // Verificar descuento de stock en MongoDB
        Producto updatedProd = productoRepository.findById(prod.getId()).orElseThrow();
        assertEquals(8, updatedProd.getCantidad(), "Stock debe pasar de 10 a 8");

        // Cleanup
        reservaRepository.deleteById(guardada.getId());
        productoRepository.deleteById(prod.getId());
    }

    @Test
    @DisplayName("Cliente Escenario C: Compra con Domicilio (Validación de Dirección Obligatoria)")
    public void testClientScenarioC_ProductDeliveryAddressValidation() {
        Producto prod = new Producto();
        prod.setNombre("Tratamiento Audit Domicilio " + UUID.randomUUID().toString().substring(0, 6));
        prod.setPrecio(45000.0);
        prod.setCantidad(5);
        prod.setEnStock(true);
        prod = productoRepository.save(prod);

        // 1. Intento sin dirección -> Debe ser rechazado con 400
        Reserva pedidoSinDir = new Reserva();
        pedidoSinDir.setNombreCliente("Cliente Domicilio");
        pedidoSinDir.setTelefono("3119876543");
        pedidoSinDir.setEmail("domicilio@isivi.test");
        pedidoSinDir.setMedioPago("WOMPI");
        pedidoSinDir.setTipoEntrega("domicilio");
        pedidoSinDir.setDireccionEntrega(""); // Vacía

        ItemReserva item = new ItemReserva();
        item.setId(prod.getId());
        item.setTipo("producto");
        item.setNombre(prod.getNombre());
        item.setCantidad(1);
        item.setPrecioUnitario(45000.0);
        pedidoSinDir.setItemsInventario(List.of(item));

        ResponseEntity<?> badResp = reservaController.crear(pedidoSinDir);
        assertEquals(HttpStatus.BAD_REQUEST, badResp.getStatusCode(), "Debe rechazar si falta dirección");

        // 2. Con dirección válida -> Debe ser creado con éxito
        pedidoSinDir.setDireccionEntrega("Manga 3ra Avenida # 21-45, Apto 502");
        ResponseEntity<?> okResp = reservaController.crear(pedidoSinDir);
        assertEquals(HttpStatus.CREATED, okResp.getStatusCode());
        Reserva guardada = (Reserva) okResp.getBody();
        assertNotNull(guardada);
        assertTrue(guardada.esDomicilio());
        assertEquals("Manga 3ra Avenida # 21-45, Apto 502", guardada.getDireccionEntrega());

        // Cleanup
        reservaRepository.deleteById(guardada.getId());
        productoRepository.deleteById(prod.getId());
    }

    @Test
    @DisplayName("Cliente Escenario D: Cita Simple (Cálculo Financiero y Bloqueo de Agenda)")
    public void testClientScenarioD_AppointmentBookingAndFinancials() {
        Servicio serv = new Servicio();
        serv.setNombre("Alisado Orgánico QA " + UUID.randomUUID().toString().substring(0, 6));
        serv.setPrecio(120000.0);
        serv.setCategoria("Tratamientos");
        serv = servicioRepository.save(serv);

        LocalDate fecha = getNextValidDay(3);
        String hora = "09:30 AM";

        Reserva cita = new Reserva();
        cita.setNombreCliente("Cliente Cita Real");
        cita.setTelefono("3123456789");
        cita.setEmail("cita@isivi.test");
        cita.setCiudad("Cartagena");
        cita.setMedioPago("WOMPI");
        cita.setFechaCita(fecha);
        cita.setHoraCita(hora);

        ItemReserva item = new ItemReserva();
        item.setId(serv.getId());
        item.setTipo("servicio");
        item.setNombre(serv.getNombre());
        item.setCantidad(1);
        item.setPrecioUnitario(120000.0);
        cita.setItemsInventario(List.of(item));

        ResponseEntity<?> response = reservaController.crear(cita);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Reserva guardada = (Reserva) response.getBody();
        assertNotNull(guardada);

        // Verificaciones de Regla de Negocio
        assertTrue(guardada.esCita());
        assertEquals(120000.0, guardada.getSubtotal());
        assertEquals(30000.0, guardada.getAnticipo(), "Anticipo debe ser 25% de 120.000 = 30.000");
        assertEquals(90000.0, guardada.getSaldo(), "Saldo restante en salón debe ser 75% = 90.000");

        // Verificar que el horario ahora aparece ocupado en /disponibilidad
        List<String> ocupadas = reservaController.disponibilidad(fecha.toString());
        assertTrue(ocupadas.contains(hora), "El horario reservado debe aparecer bloqueado en la agenda");

        // Cleanup
        reservaRepository.deleteById(guardada.getId());
        servicioRepository.deleteById(serv.getId());
    }

    @Test
    @DisplayName("Cliente Escenario E: Cita + Productos (Compra Mixta)")
    public void testClientScenarioE_MixedAppointmentAndProducts() {
        Servicio serv = new Servicio();
        serv.setNombre("Corte Dama QA " + UUID.randomUUID().toString().substring(0, 6));
        serv.setPrecio(40000.0);
        serv = servicioRepository.save(serv);

        Producto prod = new Producto();
        prod.setNombre("Aceite de Argán QA " + UUID.randomUUID().toString().substring(0, 6));
        prod.setPrecio(30000.0);
        prod.setCantidad(4);
        prod.setEnStock(true);
        prod = productoRepository.save(prod);

        LocalDate fecha = getNextValidDay(4);
        String hora = "11:00 AM";

        Reserva mixta = new Reserva();
        mixta.setNombreCliente("Cliente Mixto");
        mixta.setTelefono("3159988776");
        mixta.setEmail("mixto@isivi.test");
        mixta.setFechaCita(fecha);
        mixta.setHoraCita(hora);

        ItemReserva is = new ItemReserva();
        is.setId(serv.getId());
        is.setTipo("servicio");
        is.setNombre(serv.getNombre());
        is.setCantidad(1);
        is.setPrecioUnitario(40000.0);

        ItemReserva ip = new ItemReserva();
        ip.setId(prod.getId());
        ip.setTipo("producto");
        ip.setNombre(prod.getNombre());
        ip.setCantidad(1);
        ip.setPrecioUnitario(30000.0);

        mixta.setItemsInventario(List.of(is, ip));

        ResponseEntity<?> response = reservaController.crear(mixta);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Reserva guardada = (Reserva) response.getBody();
        assertNotNull(guardada);
        assertTrue(guardada.esCita(), "Debe clasificarse como cita");
        assertTrue(guardada.esMixto(), "Debe identificarse como compra mixta");
        assertEquals(70000.0, guardada.getSubtotal());
        assertEquals(40000.0, guardada.getAnticipo(), "Anticipo 25% del servicio ($40.000) + 100% de productos ($30.000)");

        // Stock de producto descontado
        Producto pDb = productoRepository.findById(prod.getId()).orElseThrow();
        assertEquals(3, pDb.getCantidad());

        // Cleanup
        reservaRepository.deleteById(guardada.getId());
        servicioRepository.deleteById(serv.getId());
        productoRepository.deleteById(prod.getId());
    }

    @Test
    @DisplayName("Cliente Escenario F: Reprogramación de Horario (>24h y Liberación de Slot)")
    public void testClientScenarioF_AppointmentReschedule() {
        Servicio serv = new Servicio();
        serv.setNombre("Reprog Service " + UUID.randomUUID().toString().substring(0, 6));
        serv.setPrecio(50000.0);
        serv = servicioRepository.save(serv);

        LocalDate fechaOrig = getNextValidDay(5);
        String horaOrig = "01:30 PM";

        Reserva cita = new Reserva();
        cita.setNombreCliente("Cliente Reprog");
        cita.setTelefono("3145556677");
        cita.setEmail("reprog@isivi.test");
        cita.setFechaCita(fechaOrig);
        cita.setHoraCita(horaOrig);

        ItemReserva item = new ItemReserva();
        item.setId(serv.getId());
        item.setTipo("servicio");
        item.setNombre(serv.getNombre());
        item.setCantidad(1);
        item.setPrecioUnitario(50000.0);
        cita.setItemsInventario(List.of(item));

        ResponseEntity<?> respCreate = reservaController.crear(cita);
        Reserva guardada = (Reserva) respCreate.getBody();
        assertNotNull(guardada);

        // Reprogramar a otra hora válida
        String horaNueva = "03:00 PM";
        GestionReservaRequest req = new GestionReservaRequest();
        req.setCodigoReserva(guardada.getCodigoReserva());
        req.setTelefono("3145556677");
        req.setFechaCita(fechaOrig);
        req.setHoraCita(horaNueva);

        ResponseEntity<?> respReprog = reservaController.reprogramar(guardada.getId(), req);
        assertEquals(HttpStatus.OK, respReprog.getStatusCode());

        Reserva reprog = (Reserva) respReprog.getBody();
        assertEquals(fechaOrig, reprog.getFechaCita(), "Fecha original se mantiene");
        assertEquals(horaOrig, reprog.getHoraCita(), "Hora original se mantiene");
        assertEquals(fechaOrig, reprog.getFechaPropuestaReprogramacion());
        assertEquals(horaNueva, reprog.getHoraPropuestaReprogramacion());

        // Aprobación administrativa para que el cambio de slot ocurra
        ResponseEntity<?> respAprobar = reservaController.aprobarReprogramacion(guardada.getId());
        assertEquals(HttpStatus.OK, respAprobar.getStatusCode());

        Reserva aprobada = (Reserva) respAprobar.getBody();
        assertEquals(horaNueva, aprobada.getHoraCita());

        // Verificar que horaOrig quedó libre y horaNueva ocupada
        List<String> ocupadas = reservaController.disponibilidad(fechaOrig.toString());
        assertFalse(ocupadas.contains(horaOrig), "Hora original debe quedar liberada");
        assertTrue(ocupadas.contains(horaNueva), "Nueva hora debe quedar ocupada");

        // Cleanup
        reservaRepository.deleteById(guardada.getId());
        servicioRepository.deleteById(serv.getId());
    }

    @Test
    @DisplayName("Cliente Escenario G: Cancelación de Cita (>24h Inmediata)")
    public void testClientScenarioG_CancellationPolicies() {
        Servicio serv = new Servicio();
        serv.setNombre("Cancel Service " + UUID.randomUUID().toString().substring(0, 6));
        serv.setPrecio(60000.0);
        serv = servicioRepository.save(serv);

        LocalDate fechaLejana = getNextValidDay(5);
        String horaLejana = "04:30 PM";

        Reserva citaLejana = new Reserva();
        citaLejana.setNombreCliente("Cliente Cancel Lejano");
        citaLejana.setTelefono("3161112233");
        citaLejana.setEmail("cancel1@isivi.test");
        citaLejana.setFechaCita(fechaLejana);
        citaLejana.setHoraCita(horaLejana);

        ItemReserva item1 = new ItemReserva();
        item1.setId(serv.getId());
        item1.setTipo("servicio");
        item1.setNombre(serv.getNombre());
        item1.setCantidad(1);
        item1.setPrecioUnitario(60000.0);
        citaLejana.setItemsInventario(List.of(item1));

        Reserva gLejana = (Reserva) reservaController.crear(citaLejana).getBody();
        assertNotNull(gLejana);

        GestionReservaRequest reqCancel = new GestionReservaRequest();
        reqCancel.setCodigoReserva(gLejana.getCodigoReserva());
        reqCancel.setTelefono("3161112233");
        reqCancel.setMotivoCancelacion("Viaje inesperado");

        ResponseEntity<?> respCancel = reservaController.cancelar(gLejana.getId(), reqCancel);
        assertEquals(HttpStatus.OK, respCancel.getStatusCode());

        // Verificar que el horario se liberó en agenda
        List<String> ocupadas = reservaController.disponibilidad(fechaLejana.toString());
        assertFalse(ocupadas.contains(horaLejana), "El turno debe quedar libre");

        // Cleanup
        reservaRepository.deleteById(gLejana.getId());
        servicioRepository.deleteById(serv.getId());
    }

    @Test
    @DisplayName("Cliente Escenario H: Expiración de Retención de 15 Minutos")
    public void testClientScenarioH_HoldExpirationAndStockRelease() {
        Producto prod = new Producto();
        prod.setNombre("Serum Expiracion QA " + UUID.randomUUID().toString().substring(0, 6));
        prod.setPrecio(35000.0);
        prod.setCantidad(2);
        prod.setEnStock(true);
        prod = productoRepository.save(prod);

        LocalDate fecha = getNextValidDay(3);
        String hora = "06:00 PM";

        Servicio serv = new Servicio();
        serv.setNombre("Brillado Expiracion " + UUID.randomUUID().toString().substring(0, 6));
        serv.setPrecio(40000.0);
        serv = servicioRepository.save(serv);

        Reserva reserva = new Reserva();
        reserva.setCodigoReserva("ISV-HOLD-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        reserva.setNombreCliente("Cliente Hold Expira");
        reserva.setTelefono("3178889900");
        reserva.setEmail("hold@isivi.test");
        reserva.setFechaCita(fecha);
        reserva.setHoraCita(hora);
        reserva.setEstado("Pendiente Pago");
        // Forzar expiración simulada en el pasado (hace 20 minutos)
        reserva.setFechaExpiracionPago(Instant.now().minus(20, ChronoUnit.MINUTES));

        ItemReserva is = new ItemReserva();
        is.setId(serv.getId());
        is.setTipo("servicio");
        is.setNombre(serv.getNombre());
        is.setCantidad(1);
        is.setPrecioUnitario(40000.0);

        ItemReserva ip = new ItemReserva();
        ip.setId(prod.getId());
        ip.setTipo("producto");
        ip.setNombre(prod.getNombre());
        ip.setCantidad(1);
        ip.setPrecioUnitario(35000.0);

        reserva.setItemsInventario(List.of(ip));
        reserva.setInventarioReservado(true);
        // Descontar manualmente el stock como si se hubiese reservado
        prod.setCantidad(1);
        productoRepository.save(prod);

        reserva = reservaRepository.save(reserva);

        // Ejecutar rutina de expiración de fondo si no se ha procesado ya por el planificador en segundo plano
        Reserva check = reservaRepository.findById(reserva.getId()).orElseThrow();
        if (!"Expirada".equalsIgnoreCase(check.getEstado())) {
            reservaService.expirarReservasVencidas();
        }

        Reserva exp = reservaRepository.findById(reserva.getId()).orElseThrow();
        assertEquals("Expirada", exp.getEstado());

        // Verificar que el stock volvió a su nivel original
        Producto pDb = productoRepository.findById(prod.getId()).orElseThrow();
        assertTrue(pDb.getCantidad() == 2 || pDb.getCantidad() == 3, "El stock retenido debe ser devuelto");

        // Cleanup
        reservaRepository.deleteById(reserva.getId());
        productoRepository.deleteById(prod.getId());
        servicioRepository.deleteById(serv.getId());
    }

    @Test
    @DisplayName("Cliente Escenario K: Concurrencia Extrema en Bloqueo de Horario")
    public void testClientScenarioK_ConcurrentBookingSameSlot() throws Exception {
        Servicio serv = new Servicio();
        serv.setNombre("Concurrencia Service " + UUID.randomUUID().toString().substring(0, 6));
        serv.setPrecio(50000.0);
        serv = servicioRepository.save(serv);

        LocalDate fecha = getNextValidDay(6);
        String hora = "08:00 AM";

        int threads = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        List<String> createdIds = Collections.synchronizedList(new ArrayList<>());

        final Servicio finalServ = serv;
        for (int i = 0; i < threads; i++) {
            final int idIndex = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    Reserva r = new Reserva();
                    r.setNombreCliente("Cliente Concurrente " + idIndex);
                    r.setTelefono("318000000" + idIndex);
                    r.setEmail("concurrent" + idIndex + "@isivi.test");
                    r.setFechaCita(fecha);
                    r.setHoraCita(hora);

                    ItemReserva it = new ItemReserva();
                    it.setId(finalServ.getId());
                    it.setTipo("servicio");
                    it.setNombre(finalServ.getNombre());
                    it.setCantidad(1);
                    it.setPrecioUnitario(50000.0);
                    r.setItemsInventario(List.of(it));

                    ResponseEntity<?> resp = reservaController.crear(r);
                    if (resp.getStatusCode() == HttpStatus.CREATED) {
                        successCount.incrementAndGet();
                        Reserva created = (Reserva) resp.getBody();
                        if (created != null) createdIds.add(created.getId());
                    } else if (resp.getStatusCode() == HttpStatus.CONFLICT) {
                        conflictCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    conflictCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Exactamente 1 reserva debió tener éxito
        assertEquals(1, successCount.get(), "Exactamente 1 cliente debe ganar el turno de agenda");
        assertEquals(threads - 1, conflictCount.get(), "Los demás deben ser rechazados con 409 Conflict");

        // Cleanup
        for (String id : createdIds) {
            reservaRepository.deleteById(id);
        }
        servicioRepository.deleteById(serv.getId());
    }

    // ==========================================
    // FASE 3: PERFIL ADMINISTRADOR — ESCENARIOS E2E
    // ==========================================

    @Test
    @DisplayName("Admin Escenario B & C: Ciclo de Vida Logístico de Pedido a Domicilio")
    public void testAdminScenarioC_DeliveryOrderLifecycle() {
        Producto prod = new Producto();
        prod.setNombre("Kit Domicilio Lifecycle " + UUID.randomUUID().toString().substring(0, 6));
        prod.setPrecio(90000.0);
        prod.setCantidad(5);
        prod.setEnStock(true);
        prod = productoRepository.save(prod);

        Reserva pedido = new Reserva();
        pedido.setNombreCliente("Cliente Domicilio Lifecycle");
        pedido.setTelefono("3197776655");
        pedido.setEmail("lifecycle@isivi.test");
        pedido.setTipoEntrega("delivery");
        pedido.setDireccionEntrega("Bocagrande Carrera 3 # 6-40");

        ItemReserva it = new ItemReserva();
        it.setId(prod.getId());
        it.setTipo("producto");
        it.setNombre(prod.getNombre());
        it.setCantidad(1);
        it.setPrecioUnitario(90000.0);
        pedido.setItemsInventario(List.of(it));

        Reserva guardada = (Reserva) reservaController.crear(pedido).getBody();
        assertNotNull(guardada);

        // 1. Aprobar pago
        ResponseEntity<?> respAprobar = reservaController.aprobar(guardada.getId());
        assertEquals(HttpStatus.OK, respAprobar.getStatusCode());
        Reserva r1 = (Reserva) respAprobar.getBody();
        assertEquals("Pago Confirmado", r1.getEstado());

        // 2. En preparación
        ResponseEntity<?> respPrep = reservaController.marcarPedidoEnPreparacion(guardada.getId(), adminAuth);
        assertEquals(HttpStatus.OK, respPrep.getStatusCode());
        Reserva r2 = (Reserva) respPrep.getBody();
        assertEquals("Pago Confirmado", r2.getEstado());
        assertEquals("EN_PREPARACION", r2.getEstadoPedido());

        // 3. Listo para envío
        ResponseEntity<?> respListo = reservaController.marcarPedidoListoEnvio(guardada.getId(), adminAuth);
        assertEquals(HttpStatus.OK, respListo.getStatusCode());
        Reserva r3 = (Reserva) respListo.getBody();
        assertEquals("Pago Confirmado", r3.getEstado());
        assertEquals("LISTO_ENVIO", r3.getEstadoPedido());

        // 4. En camino
        ResponseEntity<?> respCamino = reservaController.marcarPedidoEnCamino(guardada.getId(), adminAuth);
        assertEquals(HttpStatus.OK, respCamino.getStatusCode());
        Reserva r4 = (Reserva) respCamino.getBody();
        assertEquals("Pago Confirmado", r4.getEstado());
        assertEquals("EN_CAMINO", r4.getEstadoPedido());

        // 5. Entregado
        ResponseEntity<?> respEntregado = reservaController.marcarPedidoEntregado(guardada.getId(), adminAuth);
        assertEquals(HttpStatus.OK, respEntregado.getStatusCode());
        Reserva r5 = (Reserva) respEntregado.getBody();
        assertEquals("Pago Confirmado", r5.getEstado());
        assertEquals("ENTREGADO", r5.getEstadoPedido());

        // Cleanup
        reservaRepository.deleteById(guardada.getId());
        productoRepository.deleteById(prod.getId());
    }

    @Test
    @DisplayName("Admin Escenario D: Ciclo de Vida Logístico de Pedido Pickup y Rechazo de Estados Inválidos")
    public void testAdminScenarioD_PickupOrderLifecycleAndInvalidTransitions() {
        Producto prod = new Producto();
        prod.setNombre("Producto Pickup Lifecycle " + UUID.randomUUID().toString().substring(0, 6));
        prod.setPrecio(40000.0);
        prod.setCantidad(5);
        prod.setEnStock(true);
        prod = productoRepository.save(prod);

        Reserva pedido = new Reserva();
        pedido.setNombreCliente("Cliente Pickup Lifecycle");
        pedido.setTelefono("3194443322");
        pedido.setEmail("pickuprun@isivi.test");
        pedido.setTipoEntrega("pickup");

        ItemReserva it = new ItemReserva();
        it.setId(prod.getId());
        it.setTipo("producto");
        it.setNombre(prod.getNombre());
        it.setCantidad(1);
        it.setPrecioUnitario(40000.0);
        pedido.setItemsInventario(List.of(it));

        Reserva guardada = (Reserva) reservaController.crear(pedido).getBody();
        assertNotNull(guardada);
        reservaController.aprobar(guardada.getId());

        // 1. En preparación
        reservaController.marcarPedidoEnPreparacion(guardada.getId(), adminAuth);

        // 2. Intentar "En camino" en un pickup -> Debe ser rechazado
        ResponseEntity<?> respBadCamino = reservaController.marcarPedidoEnCamino(guardada.getId(), adminAuth);
        assertEquals(HttpStatus.BAD_REQUEST, respBadCamino.getStatusCode(), "Pickup no puede pasar a En camino");

        // 3. Listo para recoger
        ResponseEntity<?> respListo = reservaController.marcarPedidoListoParaRecoger(guardada.getId(), adminAuth);
        assertEquals(HttpStatus.OK, respListo.getStatusCode());
        Reserva rListo = (Reserva) respListo.getBody();
        assertEquals("Pago Confirmado", rListo.getEstado());
        assertEquals("LISTO_RECOGER", rListo.getEstadoPedido());

        // 4. Entregar / Recogido
        ResponseEntity<?> respEntregado = reservaController.marcarPedidoEntregado(guardada.getId(), adminAuth);
        assertEquals(HttpStatus.OK, respEntregado.getStatusCode());
        Reserva rEnt = (Reserva) respEntregado.getBody();
        assertEquals("Pago Confirmado", rEnt.getEstado());
        assertEquals("RECOGIDO", rEnt.getEstadoPedido());

        // Cleanup
        reservaRepository.deleteById(guardada.getId());
        productoRepository.deleteById(prod.getId());
    }

    @Test
    @DisplayName("Admin Escenario G: Gestión de Categorías y Protección contra Eliminación de Categoría con Productos")
    public void testAdminScenarioG_CategoryManagementAndReferentialIntegrity() {
        String slug = "cat-audit-" + UUID.randomUUID().toString().substring(0, 6);
        CategoriaProducto cat = new CategoriaProducto(slug, "Categoría Audit " + slug, true);
        categoriaProductoRepository.save(cat);

        Producto p = new Producto();
        p.setNombre("Producto Asociado " + slug);
        p.setPrecio(25000.0);
        p.setCategoriaId(cat.getId());
        p.setCantidad(5);
        p.setEnStock(true);
        p = productoRepository.save(p);

        // Intentar eliminar la categoría que tiene productos asociados -> Debe ser rechazada con 409 Conflict
        ResponseEntity<?> respDel = categoriaProductoController.eliminar(cat.getId());
        assertEquals(HttpStatus.CONFLICT, respDel.getStatusCode(), "No se debe permitir eliminar categoría con productos asociados");

        // Eliminar producto primero
        productoRepository.deleteById(p.getId());

        // Ahora la eliminación debe tener éxito
        ResponseEntity<?> respDelOk = categoriaProductoController.eliminar(cat.getId());
        assertEquals(HttpStatus.NO_CONTENT, respDelOk.getStatusCode());
    }

    // ==========================================
    // FASE 7: SEGURIDAD Y PROTECCIÓN DE DATOS
    // ==========================================

    @Test
    @DisplayName("Seguridad: Backend Recalcula Precios e Ignora Manipulación de Precios desde Cliente")
    public void testSecurity_BackendRecalculatesPrices() {
        Producto prod = new Producto();
        prod.setNombre("Producto Anti-Hack " + UUID.randomUUID().toString().substring(0, 6));
        prod.setPrecio(150000.0); // Precio real en BD = 150.000
        prod.setCantidad(5);
        prod.setEnStock(true);
        prod = productoRepository.save(prod);

        Reserva pedidoHack = new Reserva();
        pedidoHack.setNombreCliente("Hacker");
        pedidoHack.setTelefono("3000000000");
        pedidoHack.setEmail("hacker@test.com");
        pedidoHack.setTipoEntrega("pickup");
        pedidoHack.setSubtotal(100.0); // Intento de pagar 100 COP en vez de 150.000

        ItemReserva it = new ItemReserva();
        it.setId(prod.getId());
        it.setTipo("producto");
        it.setNombre(prod.getNombre());
        it.setCantidad(1);
        it.setPrecioUnitario(100.0); // Manipulación en cliente
        it.setSubtotal(100.0);
        pedidoHack.setItemsInventario(List.of(it));

        ResponseEntity<?> resp = reservaController.crear(pedidoHack);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        Reserva guardada = (Reserva) resp.getBody();
        assertNotNull(guardada);

        // El backend DEBIÓ recalcular y fijar el subtotal real = 150.000 COP
        assertEquals(150000.0, guardada.getSubtotal(), "El servidor debe ignorar precios manipulados y usar el precio real de la BD");

        // Cleanup
        reservaRepository.deleteById(guardada.getId());
        productoRepository.deleteById(prod.getId());
    }
}
