package com.isivi.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isivi.app.controller.DashboardController;
import com.isivi.app.controller.PagoController;
import com.isivi.app.controller.ReservaController;
import com.isivi.app.model.*;
import com.isivi.app.repository.*;
import com.isivi.app.service.ReservaService;
import com.isivi.app.util.HorarioUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ReservaExpirationAndConcurrencyTest {

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private PagoController pagoController;

    @Autowired
    private DashboardController dashboardController;

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
    private MongoTemplate mongoTemplate;

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

        fechaPrueba = LocalDate.now(zonaBogota).plusDays(3);
        while (fechaPrueba.getDayOfWeek() == DayOfWeek.MONDAY) {
            fechaPrueba = fechaPrueba.plusDays(1);
        }

        servicioPrueba = new Servicio("Corte y Barba Imperial", "corte", 80000.0, "45 min", "Servicio VIP", "");
        servicioPrueba = servicioRepository.save(servicioPrueba);

        productoPrueba = new Producto("Pomada Mate ISIVI", 45000.0, "Fijación premium", "", true, 10);
        productoPrueba = productoRepository.save(productoPrueba);


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

    private Reserva crearSolicitudCliente(String nombre, String telefono, String hora) {
        Reserva r = new Reserva();
        r.setNombreCliente(nombre);
        r.setTelefono(telefono);
        r.setCiudad("Cartagena");
        r.setMedioPago("WOMPI");
        r.setFechaCita(fechaPrueba);
        r.setHoraCita(hora);
        r.setItemsInventario(List.of(
                crearItem(servicioPrueba.getId(), "servicio", 1, servicioPrueba.getNombre(), servicioPrueba.getPrecio())
        ));
        return r;
    }

    private ItemReserva crearItem(String id, String tipo, int cant, String nombre, double precio) {
        ItemReserva item = new ItemReserva();
        item.setId(id);
        item.setTipo(tipo);
        item.setCantidad(cant);
        item.setNombre(nombre);
        item.setPrecioUnitario(precio);
        item.setSubtotal(precio * cant);
        return item;
    }

    @Test
    @DisplayName("1. Retención vigente bloquea a otro cliente antes de expirar")
    void testRetencionVigenteBloquea() {
        Instant baseTime = Instant.now();
        Clock clockFijo = Clock.fixed(baseTime, zonaBogota);
        reservaController.setClock(clockFijo);

        // Cliente A crea reserva y prepara pago Wompi
        Reserva reqA = crearSolicitudCliente("Carlos Mendoza", "3001112233", horaPrueba);
        ResponseEntity<?> respA = reservaController.crear(reqA);
        assertEquals(HttpStatus.CREATED, respA.getStatusCode());
        Reserva reservaA = (Reserva) respA.getBody();
        assertNotNull(reservaA);

        reservaService.prepararPagoWompi(reservaA, "ISV-WOMPI-001");

        // Cliente B intenta reservar el mismo horario 5 minutos después (dentro de los 15 min)
        Clock clock5m = Clock.fixed(baseTime.plus(Duration.ofMinutes(5)), zonaBogota);
        reservaController.setClock(clock5m);

        Reserva reqB = crearSolicitudCliente("Andres Lopez", "3004445566", horaPrueba);
        ResponseEntity<?> respB = reservaController.crear(reqB);
        assertEquals(HttpStatus.CONFLICT, respB.getStatusCode(), "Cliente B debe recibir HTTP 409 mientras la retención está vigente");
    }

    @Test
    @DisplayName("2. Retención expirada libera horario automáticamente para Cliente B")
    void testRetencionExpiradaLiberaHorario() {
        Instant baseTime = Instant.now();
        Clock clockInicio = Clock.fixed(baseTime, zonaBogota);
        reservaController.setClock(clockInicio);

        // Cliente A crea reserva y pasa a Pendiente Pago
        Reserva reqA = crearSolicitudCliente("Carlos Mendoza", "3001112233", horaPrueba);
        ResponseEntity<?> respA = reservaController.crear(reqA);
        Reserva reservaA = (Reserva) respA.getBody();
        assertNotNull(reservaA);
        reservaService.prepararPagoWompi(reservaA, "ISV-WOMPI-002");

        // Adelantar reloj 16 minutos (retención de 15m vencida)
        Clock clock16m = Clock.fixed(baseTime.plus(Duration.ofMinutes(16)), zonaBogota);
        reservaController.setClock(clock16m);

        // Disponibilidad en tiempo real debe reportar que 09:30 AM ya no está bloqueada
        List<String> ocupadas = reservaController.disponibilidad(fechaPrueba.toString());
        assertFalse(ocupadas.contains(horaPrueba), "El horario no debe figurar como ocupado tras expirar la retención");

        // Cliente B ahora puede reservar exitosamente 09:30 AM
        Reserva reqB = crearSolicitudCliente("Andres Lopez", "3004445566", horaPrueba);
        ResponseEntity<?> respB = reservaController.crear(reqB);
        assertEquals(HttpStatus.CREATED, respB.getStatusCode(), "Cliente B debe poder reservar una vez expirada la retención");
        Reserva reservaB = (Reserva) respB.getBody();
        assertNotNull(reservaB);
        assertEquals(horaPrueba, reservaB.getHoraCita());
    }

    @Test
    @DisplayName("3. Disponibilidad detecta expiración en tiempo real sin scheduler")
    void testDisponibilidadDetectaExpiracionSinScheduler() {
        Instant baseTime = Instant.now();
        reservaController.setClock(Clock.fixed(baseTime, zonaBogota));

        Reserva reqA = crearSolicitudCliente("Laura Restrepo", "3007778899", horaPrueba);
        ResponseEntity<?> respA = reservaController.crear(reqA);
        Reserva reservaA = (Reserva) respA.getBody();
        assertNotNull(reservaA);
        reservaService.prepararPagoWompi(reservaA, "ISV-WOMPI-003");

        // Mientras vigente: ocupada
        List<String> antes = reservaController.disponibilidad(fechaPrueba.toString());
        assertTrue(antes.contains(horaPrueba), "Debe figurar ocupada mientras esté vigente");

        // Al pasar 15 minutos y 1 segundo: disponible inmediatamente
        reservaController.setClock(Clock.fixed(baseTime.plus(Duration.ofSeconds(901)), zonaBogota));
        List<String> despues = reservaController.disponibilidad(fechaPrueba.toString());
        assertFalse(despues.contains(horaPrueba), "Debe figurar libre de inmediato sin esperar a un cron");
    }

    @Test
    @DisplayName("4. APPROVED antes de expirar convierte la reserva en confirmada definitiva permanente")
    void testApprovedAntesDeExpirarPermaneceConfirmado() {
        Instant baseTime = Instant.now();
        reservaController.setClock(Clock.fixed(baseTime, zonaBogota));

        Reserva reqA = crearSolicitudCliente("Santiago Arias", "3009990011", horaPrueba);
        ResponseEntity<?> respA = reservaController.crear(reqA);
        Reserva reservaA = (Reserva) respA.getBody();
        assertNotNull(reservaA);
        reservaA = reservaService.prepararPagoWompi(reservaA, "ISV-WOMPI-004");

        // A los 5 minutos Wompi confirma APPROVED
        reservaController.setClock(Clock.fixed(baseTime.plus(Duration.ofMinutes(5)), zonaBogota));
        Reserva confirmada = reservaService.confirmarPagoWompi(reservaA);
        assertEquals("Confirmado", confirmada.getEstado());
        assertEquals("APROBADO", confirmada.getEstadoPago());
        assertNull(confirmada.getFechaExpiracionPago(), "Una reserva confirmada no tiene fecha de expiración");

        // Avanzar el reloj 45 minutos (mucho más allá de los 15m iniciales)
        reservaController.setClock(Clock.fixed(baseTime.plus(Duration.ofMinutes(45)), zonaBogota));

        // El horario debe seguir permanentemente ocupado
        List<String> ocupadas = reservaController.disponibilidad(fechaPrueba.toString());
        assertTrue(ocupadas.contains(horaPrueba), "Horario confirmado debe mantenerse ocupado permanentemente");

        // Cliente B recibe 409
        Reserva reqB = crearSolicitudCliente("Mateo Gomez", "3002223344", horaPrueba);
        ResponseEntity<?> respB = reservaController.crear(reqB);
        assertEquals(HttpStatus.CONFLICT, respB.getStatusCode());
    }

    @Test
    @DisplayName("5. DECLINED libera horario e inventario inmediatamente")
    void testDeclinedLiberaInmediatamente() {
        Instant baseTime = Instant.now();
        reservaController.setClock(Clock.fixed(baseTime, zonaBogota));

        // Reserva con producto para validar inventario
        Reserva req = new Reserva();
        req.setNombreCliente("Felipe Calderon");
        req.setTelefono("3005556677");
        req.setCiudad("Cartagena");
        req.setMedioPago("WOMPI");
        req.setFechaCita(fechaPrueba);
        req.setHoraCita(horaPrueba);
        req.setItemsInventario(List.of(
                crearItem(productoPrueba.getId(), "producto", 2, productoPrueba.getNombre(), productoPrueba.getPrecio()),
                crearItem(servicioPrueba.getId(), "servicio", 1, servicioPrueba.getNombre(), servicioPrueba.getPrecio())
        ));

        ResponseEntity<?> resp = reservaController.crear(req);
        Reserva reserva = (Reserva) resp.getBody();
        assertNotNull(reserva);
        assertTrue(reserva.getInventarioReservado());

        // Stock original era 10, ahora debe ser 8
        Producto pEnDb = productoRepository.findById(productoPrueba.getId()).orElseThrow();
        assertEquals(8, pEnDb.getCantidad());

        // Al denegar (DECLINED)
        reservaService.denegar(reserva);

        // Inventario devuelto a 10
        Producto pDevuelto = productoRepository.findById(productoPrueba.getId()).orElseThrow();
        assertEquals(10, pDevuelto.getCantidad());

        // Horario libre inmediatamente
        List<String> ocupadas = reservaController.disponibilidad(fechaPrueba.toString());
        assertFalse(ocupadas.contains(horaPrueba), "El horario debe quedar libre de inmediato tras DECLINED");
    }

    @Test
    @DisplayName("6. Preparar pago de una reserva expirada es rechazado de forma segura")
    void testPrepararPagoReservaExpiradaRechazado() {
        Instant baseTime = Instant.now();
        reservaController.setClock(Clock.fixed(baseTime, zonaBogota));

        Reserva req = crearSolicitudCliente("Diana Morales", "3008889900", horaPrueba);
        ResponseEntity<?> resp = reservaController.crear(req);
        Reserva reserva = (Reserva) resp.getBody();
        assertNotNull(reserva);
        reservaService.prepararPagoWompi(reserva, "ISV-WOMPI-EXP-01");

        // Adelantar reloj 20 minutos
        reservaController.setClock(Clock.fixed(baseTime.plus(Duration.ofMinutes(20)), zonaBogota));

        // Intentar preparar pago nuevamente
        ResponseEntity<?> respPrep = pagoController.preparar(reserva.getId());
        assertEquals(HttpStatus.BAD_REQUEST, respPrep.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) respPrep.getBody();
        assertNotNull(body);
        assertEquals("RESERVA_EXPIRADA", body.get("error"));
    }

    @Test
    @DisplayName("7. Idempotencia en liberación de inventario ante múltiples ejecuciones de expiración")
    void testIdempotenciaLiberacionInventario() {
        Reserva req = new Reserva();
        req.setNombreCliente("Juan Camilo");
        req.setTelefono("3003334455");
        req.setCiudad("Cartagena");
        req.setMedioPago("WOMPI");
        req.setFechaCita(fechaPrueba);
        req.setHoraCita(horaPrueba);
        req.setItemsInventario(List.of(
                crearItem(productoPrueba.getId(), "producto", 3, productoPrueba.getNombre(), productoPrueba.getPrecio())
        ));

        ResponseEntity<?> resp = reservaController.crear(req);
        Reserva reserva = (Reserva) resp.getBody();
        assertNotNull(reserva);

        // Stock baja de 10 a 7
        assertEquals(7, productoRepository.findById(productoPrueba.getId()).orElseThrow().getCantidad());

        // Ejecutar marcarExpirada varias veces seguidas
        reservaService.marcarExpirada(reserva);
        reservaService.marcarExpirada(reserva);
        reservaService.marcarExpirada(reserva);

        // Stock debe ser exactamente 10 (nunca 13 o 16)
        assertEquals(10, productoRepository.findById(productoPrueba.getId()).orElseThrow().getCantidad(), "El stock debe devolverse exactamente una vez");
    }

    @Test
    @DisplayName("8. Concurrencia real multithread: 2 clientes intentan el mismo turno al mismo instante")
    void testConcurrenciaMultithreadDobleReserva() throws InterruptedException {
        int hilos = 8;
        ExecutorService executor = Executors.newFixedThreadPool(hilos);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(hilos);

        AtomicInteger exitosos = new AtomicInteger(0);
        AtomicInteger conflictos = new AtomicInteger(0);

        for (int i = 0; i < hilos; i++) {
            final int idCliente = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    Reserva req = crearSolicitudCliente("Cliente " + idCliente, "300100200" + idCliente, horaPrueba);
                    ResponseEntity<?> r = reservaController.crear(req);
                    if (r.getStatusCode() == HttpStatus.CREATED) {
                        exitosos.incrementAndGet();
                    } else if (r.getStatusCode() == HttpStatus.CONFLICT) {
                        conflictos.incrementAndGet();
                    }
                } catch (Exception ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(1, exitosos.get(), "Exactamente 1 cliente debe obtener la reserva en concurrencia");
        assertEquals(hilos - 1, conflictos.get(), "Los demás clientes deben recibir HTTP 409");
    }

    @Test
    @DisplayName("9. Flujo QA-HOLD-001: Cliente A cancela en pasarela, Cliente B bloqueado y luego liberado tras expiración")
    void testFlujoQaHold001() {
        Instant t0 = Instant.now();
        reservaController.setClock(Clock.fixed(t0, zonaBogota));

        // Cliente A inicia reserva QA-HOLD-001
        Reserva reqA = crearSolicitudCliente("Cliente QA A", "3009998888", horaPrueba);
        ResponseEntity<?> respA = reservaController.crear(reqA);
        assertEquals(HttpStatus.CREATED, respA.getStatusCode());
        Reserva resA = (Reserva) respA.getBody();
        assertNotNull(resA);
        resA.setCodigoReserva("QA-HOLD-001");
        reservaRepository.save(resA);

        reservaService.prepararPagoWompi(resA, "ISV-QA-HOLD-001");

        // Cliente A cancela o abandona pasarela.
        // Inmediatamente (t0 + 1 minuto): Cliente B consulta e intenta reservar 09:30 AM
        reservaController.setClock(Clock.fixed(t0.plus(Duration.ofMinutes(1)), zonaBogota));

        List<String> ocupadasT1 = reservaController.disponibilidad(fechaPrueba.toString());
        assertTrue(ocupadasT1.contains(horaPrueba), "El horario debe seguir temporalmente bloqueado");

        Reserva reqB = crearSolicitudCliente("Cliente QA B", "3007776666", horaPrueba);
        ResponseEntity<?> respB1 = reservaController.crear(reqB);
        assertEquals(HttpStatus.CONFLICT, respB1.getStatusCode(), "Cliente B debe recibir 409 mientras la retención de Cliente A sigue vigente");

        // Al cumplirse 16 minutos (expiración de los 15 minutos):
        reservaController.setClock(Clock.fixed(t0.plus(Duration.ofMinutes(16)), zonaBogota));

        List<String> ocupadasT16 = reservaController.disponibilidad(fechaPrueba.toString());
        assertFalse(ocupadasT16.contains(horaPrueba), "El horario debe estar disponible tras vencer la retención");

        ResponseEntity<?> respB2 = reservaController.crear(reqB);
        assertEquals(HttpStatus.CREATED, respB2.getStatusCode(), "Cliente B ahora reserva exitosamente");

        // La reserva anterior QA-HOLD-001 sigue existiendo en MongoDB con estado Expirada (sin pérdida de datos)
        Reserva resAEnDb = reservaRepository.findById(resA.getId()).orElseThrow();
        assertEquals("Expirada", resAEnDb.getEstado());
        assertEquals("QA-HOLD-001", resAEnDb.getCodigoReserva());
    }

    @Test
    @DisplayName("10. DashboardController excluye reservas expiradas de citas y pendientes")
    void testDashboardExcluyeExpiradas() {
        Instant baseTime = Instant.now();
        reservaController.setClock(Clock.fixed(baseTime, zonaBogota));

        // Reserva para fechaPrueba
        Reserva reqFutura = new Reserva();
        reqFutura.setNombreCliente("Dashboard Test");
        reqFutura.setTelefono("3001234567");
        reqFutura.setCiudad("Cartagena");
        reqFutura.setMedioPago("TRANSFERENCIA");
        reqFutura.setFechaCita(fechaPrueba);
        reqFutura.setHoraCita(horaPrueba);
        reqFutura.setItemsInventario(List.of(crearItem(servicioPrueba.getId(), "servicio", 1, servicioPrueba.getNombre(), servicioPrueba.getPrecio())));
        
        ResponseEntity<?> resp = reservaController.crear(reqFutura);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        Reserva r = (Reserva) resp.getBody();
        assertNotNull(r);

        // Con retención activa: figura en pendientes
        ResponseEntity<Map<String, Object>> dashResp1 = dashboardController.obtenerResumen();
        Map<String, Object> body1 = dashResp1.getBody();
        assertNotNull(body1);
        long pend1 = (long) body1.get("pendientes");
        assertTrue(pend1 >= 1);

        // Expirar reserva
        reservaService.marcarExpirada(r);

        // Ya no debe contar en pendientes ni en citas activas de hoy
        ResponseEntity<Map<String, Object>> dashResp2 = dashboardController.obtenerResumen();
        Map<String, Object> body2 = dashResp2.getBody();
        assertNotNull(body2);
        Object agendaObj = body2.get("agendaHoy");
        if (agendaObj instanceof List<?> list) {
            assertTrue(list.stream().noneMatch(item -> {
                if (item instanceof Reserva res) return r.getId().equals(res.getId());
                if (item instanceof Map<?, ?> map) return r.getId().equals(map.get("id"));
                return false;
            }), "Cita expirada no debe figurar en agenda activa");
        }
    }
}

