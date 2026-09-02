package com.isivi.app;

import com.isivi.app.controller.DashboardController;
import com.isivi.app.controller.ReservaController;
import com.isivi.app.dto.GestionReservaRequest;
import com.isivi.app.exception.CancelacionNoPermitidaException;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CancellationPolicyTest {

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

    private static final AtomicInteger testCounter = new AtomicInteger((int) (System.currentTimeMillis() % 50000) + 1000);
    private ZoneId zonaBogota = ZoneId.of("America/Bogota");
    private LocalDate hoy;

    @BeforeEach
    public void setup() {
        // Usar una fecha virtual futura garantizada sin colisión de índice
        hoy = LocalDate.of(2035, 1, 1).plusDays(testCounter.addAndGet(50));
        LocalDateTime mediodiaHoy = LocalDateTime.of(hoy, LocalTime.of(12, 0));
        reservaService.setClock(Clock.fixed(mediodiaHoy.atZone(zonaBogota).toInstant(), zonaBogota));
        reservaService.setCancelacionHorasMinimas(24);
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() {
        reservaService.setClock(Clock.system(zonaBogota));
    }

    private String generarHorarioUnico() {
        int val = testCounter.incrementAndGet();
        int hora = (val / 60) % 12;
        int min = val % 60;
        return String.format("%02d:%02d AM", hora == 0 ? 12 : hora, min);
    }

    @Test
    @DisplayName("1. 48 Horas Restantes (> 24h) -> Cancelación permitida directamente")
    public void testCancelacion48HorasPermitida() {
        String code = "ISV-48H-" + UUID.randomUUID().toString().substring(0, 8);
        Reserva r = new Reserva();
        r.setCodigoReserva(code);
        r.setNombreCliente("Cliente 48h");
        r.setTelefono("3005551111");
        r.setFechaCita(hoy.plusDays(2)); // Pasado mañana a las 12:00 PM (48 horas exactas > 24h)
        r.setHoraCita("12:00 PM");
        r.setEstado(ReservaService.CONFIRMADO);
        r.setEstadoPago("APROBADO");
        r.setAnticipo(30000.0);
        final Reserva guardada = reservaRepository.save(r);

        Map<String, Object> resultado = reservaService.procesarCancelacionCliente(guardada, "Cambio de planes");

        assertEquals(ReservaService.CANCELADO, resultado.get("estado"));
        assertEquals("CANCELADA", resultado.get("tipoProceso"));
        Reserva procesada = (Reserva) resultado.get("reserva");
        assertEquals("CLIENTE", procesada.getCanceladaPor());
        assertEquals("Cambio de planes", procesada.getMotivoCancelacion());
        assertNotNull(procesada.getFechaCancelacion());
        assertFalse(procesada.getNotificacionCancelacionVista());
    }

    @Test
    @DisplayName("2. 25 Horas Restantes (> 24h) -> Cancelación permitida directamente")
    public void testCancelacion25HorasPermitida() {
        String code = "ISV-25H-" + UUID.randomUUID().toString().substring(0, 8);
        Reserva r = new Reserva();
        r.setCodigoReserva(code);
        r.setNombreCliente("Cliente 25h");
        r.setTelefono("3005552222");
        r.setFechaCita(hoy.plusDays(1)); // Mañana a la 1:00 PM (25 horas > 24h)
        r.setHoraCita("01:00 PM");
        r.setEstado(ReservaService.CONFIRMADO);
        r.setEstadoPago("APROBADO");
        r.setAnticipo(35000.0);
        final Reserva guardada = reservaRepository.save(r);

        Map<String, Object> resultado = reservaService.procesarCancelacionCliente(guardada, "Problema de horario");

        assertEquals(ReservaService.CANCELADO, resultado.get("estado"));
        assertEquals("CANCELADA", resultado.get("tipoProceso"));
        Reserva procesada = (Reserva) resultado.get("reserva");
        assertEquals(ReservaService.CANCELADO, procesada.getEstado());
        assertEquals("CLIENTE", procesada.getCanceladaPor());
    }

    @Test
    @DisplayName("3. 24 Horas Exactas (<= 24h) -> Cancelación BLOQUEADA (CancelacionNoPermitidaException)")
    public void testCancelacion24HorasExactasBloqueada() {
        String code = "ISV-24H-" + UUID.randomUUID().toString().substring(0, 8);
        Reserva r = new Reserva();
        r.setCodigoReserva(code);
        r.setNombreCliente("Cliente 24h Exactas");
        r.setTelefono("3005553333");
        r.setFechaCita(hoy.plusDays(1)); // Mañana a las 12:00 PM (24 horas exactas <= 24h)
        r.setHoraCita("12:00 PM");
        r.setEstado(ReservaService.CONFIRMADO);
        r.setEstadoPago("APROBADO");
        r.setAnticipo(40000.0);
        final Reserva guardada = reservaRepository.save(r);

        CancelacionNoPermitidaException ex = assertThrows(CancelacionNoPermitidaException.class, () -> {
            reservaService.procesarCancelacionCliente(guardada, "Quiero cancelar");
        });
        assertEquals(24, ex.getHorasMinimas());
        assertTrue(ex.getMessage().contains("24 horas"));

        // Verificar que la reserva sigue en Confirmado y NO se liberó
        Reserva reconsultada = reservaRepository.findById(guardada.getId()).orElseThrow();
        assertEquals(ReservaService.CONFIRMADO, reconsultada.getEstado());
    }

    @Test
    @DisplayName("4. 23 Horas Restantes (<= 24h) -> Cancelación BLOQUEADA")
    public void testCancelacion23HorasBloqueada() {
        String code = "ISV-23H-" + UUID.randomUUID().toString().substring(0, 8);
        Reserva r = new Reserva();
        r.setCodigoReserva(code);
        r.setNombreCliente("Cliente 23h");
        r.setTelefono("3005554444");
        r.setFechaCita(hoy.plusDays(1)); // Mañana a las 11:00 AM (23 horas <= 24h)
        r.setHoraCita("11:00 AM");
        r.setEstado(ReservaService.CONFIRMADO);
        r.setEstadoPago("APROBADO");
        final Reserva guardada = reservaRepository.save(r);

        assertThrows(CancelacionNoPermitidaException.class, () -> {
            reservaService.procesarCancelacionCliente(guardada, "No puedo ir");
        });
    }

    @Test
    @DisplayName("5. 12 Horas Restantes (<= 24h) -> Cancelación BLOQUEADA")
    public void testCancelacion12HorasBloqueada() {
        String code = "ISV-12H-" + UUID.randomUUID().toString().substring(0, 8);
        Reserva r = new Reserva();
        r.setCodigoReserva(code);
        r.setNombreCliente("Cliente 12h");
        r.setTelefono("3005555555");
        r.setFechaCita(hoy.plusDays(1)); // Mañana a las 12:00 AM (12 horas <= 24h)
        r.setHoraCita("12:00 AM");
        r.setEstado(ReservaService.CONFIRMADO);
        final Reserva guardada = reservaRepository.save(r);

        assertThrows(CancelacionNoPermitidaException.class, () -> {
            reservaService.procesarCancelacionCliente(guardada, "Emergencia");
        });
    }

    @Test
    @DisplayName("6. 2 Horas Restantes (<= 24h) -> Cancelación BLOQUEADA")
    public void testCancelacion2HorasBloqueada() {
        String code = "ISV-2H-" + UUID.randomUUID().toString().substring(0, 8);
        Reserva r = new Reserva();
        r.setCodigoReserva(code);
        r.setNombreCliente("Cliente 2h");
        r.setTelefono("3005556666");
        r.setFechaCita(hoy); // Mismo día hoy a las 2:00 PM (2 horas restantes <= 24h)
        r.setHoraCita("02:00 PM");
        r.setEstado(ReservaService.CONFIRMADO);
        final Reserva guardada = reservaRepository.save(r);

        assertThrows(CancelacionNoPermitidaException.class, () -> {
            reservaService.procesarCancelacionCliente(guardada, "Se me hizo tarde");
        });
    }

    @Test
    @DisplayName("7. Cita Pasada no puede cancelarse")
    public void testCitaPasadaNoSePuedeCancelar() {
        String code = "ISV-PAST-" + UUID.randomUUID().toString().substring(0, 8);
        Reserva r = new Reserva();
        r.setCodigoReserva(code);
        r.setNombreCliente("Cliente Ayer");
        r.setTelefono("3005557777");
        r.setFechaCita(hoy.minusDays(5)); // Cita pasada
        r.setHoraCita(generarHorarioUnico());
        r.setEstado(ReservaService.CONFIRMADO);
        final Reserva guardada = reservaRepository.save(r);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            reservaService.procesarCancelacionCliente(guardada, "Quiero cancelar");
        });
        assertTrue(ex.getMessage().contains("Esta cita ya pasó"));
    }

    @Test
    @DisplayName("8. Cita ya cancelada es idempotente")
    public void testCitaYaCanceladaEsIdempotente() {
        String code = "ISV-ALREADY-" + UUID.randomUUID().toString().substring(0, 8);
        Reserva r = new Reserva();
        r.setCodigoReserva(code);
        r.setNombreCliente("Cliente Reincidente");
        r.setTelefono("3005558888");
        r.setFechaCita(hoy.plusDays(15));
        r.setHoraCita(generarHorarioUnico());
        r.setEstado(ReservaService.CANCELADO);
        r.setCanceladaPor("CLIENTE");
        final Reserva guardada = reservaRepository.save(r);

        Map<String, Object> res = reservaService.procesarCancelacionCliente(guardada, "Otro motivo");
        assertEquals(ReservaService.CANCELADO, res.get("estado"));
        assertTrue(res.get("mensaje").toString().contains("ya fue cancelada"));
    }

    @Test
    @DisplayName("9. Seguridad: Endpoint PATCH /cancelar devuelve 403 ante credenciales incorrectas")
    public void testSeguridadCancelacionCliente403() {
        String code = "ISV-SEC-" + UUID.randomUUID().toString().substring(0, 8);
        Reserva r = new Reserva();
        r.setCodigoReserva(code);
        r.setNombreCliente("Cliente Seguro");
        r.setTelefono("3001234567");
        r.setFechaCita(hoy.plusDays(45));
        r.setHoraCita(generarHorarioUnico());
        r.setEstado(ReservaService.CONFIRMADO);
        final Reserva guardada = reservaRepository.save(r);

        GestionReservaRequest badRequest = new GestionReservaRequest();
        badRequest.setCodigoReserva(code);
        badRequest.setTelefono("3009999999"); // Teléfono equivocado

        ResponseEntity<?> response = reservaController.cancelar(guardada.getId(), badRequest);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("10. Endpoint PATCH /cancelar devuelve HTTP 409 con CANCELACION_NO_PERMITIDA dentro de 24h")
    public void testEndpointDevuelve409ConflictDentroDe24h() {
        String code = "ISV-409-" + UUID.randomUUID().toString().substring(0, 8);
        Reserva r = new Reserva();
        r.setCodigoReserva(code);
        r.setNombreCliente("Cliente 409");
        r.setTelefono("3001112233");
        r.setFechaCita(hoy.plusDays(1)); // Mañana a las 11:00 AM (23 horas <= 24h)
        r.setHoraCita(generarHorarioUnico());
        r.setEstado(ReservaService.CONFIRMADO);
        final Reserva guardada = reservaRepository.save(r);

        GestionReservaRequest req = new GestionReservaRequest();
        req.setCodigoReserva(code);
        req.setTelefono("3001112233");
        req.setMotivoCancelacion("Cambio de planes");

        ResponseEntity<?> response = reservaController.cancelar(guardada.getId(), req);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("CANCELACION_NO_PERMITIDA", body.get("code"));
        assertEquals(24, body.get("horasMinimas"));
    }

    @Test
    @DisplayName("11. Pendiente Pago con retención se cancela inmediatamente")
    public void testPendientePagoCancelacionDirecta() {
        String code = "ISV-PENDPAGO-" + UUID.randomUUID().toString().substring(0, 8);
        Reserva r = new Reserva();
        r.setCodigoReserva(code);
        r.setNombreCliente("Cliente Pendiente");
        r.setTelefono("3005559990");
        r.setFechaCita(hoy.plusDays(20));
        r.setHoraCita(generarHorarioUnico());
        r.setEstado(ReservaService.PENDIENTE_PAGO);
        r.setEstadoPago("PENDIENTE");
        final Reserva guardada = reservaRepository.save(r);

        Map<String, Object> res = reservaService.procesarCancelacionCliente(guardada, null);
        assertEquals(ReservaService.CANCELADO, res.get("estado"));
        assertEquals("CANCELADA", res.get("tipoProceso"));
    }

    @Test
    @DisplayName("12. Elegir otro horario libera retención provisional sin emitir alerta de cancelación")
    public void testLiberarRetencionProvisionalNoEmiteAlarma() {
        String code = "ISV-HOLD-" + UUID.randomUUID().toString().substring(0, 8);
        Reserva r = new Reserva();
        r.setCodigoReserva(code);
        r.setNombreCliente("Cliente Hold");
        r.setTelefono("3005559991");
        r.setFechaCita(hoy.plusDays(25));
        r.setHoraCita(generarHorarioUnico());
        r.setEstado(ReservaService.PENDIENTE_PAGO);
        r.setEstadoPago("PENDIENTE");
        final Reserva guardada = reservaRepository.save(r);

        Reserva liberada = reservaService.liberarRetencionProvisional(guardada, "3005559991", code);
        assertEquals(ReservaService.CANCELADO, liberada.getEstado());
        assertEquals("LIBERACION_PROVISIONAL", liberada.getCanceladaPor());
        assertTrue(liberada.getNotificacionCancelacionVista()); // No genera alerta
    }

    @Test
    @DisplayName("13. Cancelación Administrativa Directa")
    public void testCancelacionAdministrativaDirecta() {
        String code = "ISV-ADMINDIR-" + UUID.randomUUID().toString().substring(0, 8);
        Reserva r = new Reserva();
        r.setCodigoReserva(code);
        r.setNombreCliente("Cliente Forzado");
        r.setTelefono("3005559992");
        r.setFechaCita(hoy.plusDays(40));
        r.setHoraCita(generarHorarioUnico());
        r.setEstado(ReservaService.CONFIRMADO);
        final Reserva guardada = reservaRepository.save(r);

        Reserva cancelada = reservaService.cancelarAdministrativa(guardada, "Cierre por fuerza mayor");
        assertEquals(ReservaService.CANCELADO, cancelada.getEstado());
        assertEquals("ADMIN", cancelada.getCanceladaPor());
        assertEquals("Cierre por fuerza mayor", cancelada.getMotivoCancelacion());
        assertNotNull(cancelada.getFechaCancelacion());
        assertTrue(cancelada.getNotificacionCancelacionVista());
    }

    @Test
    @DisplayName("14. Idempotencia en liberación de inventario tras cancelación")
    public void testInventarioIdempotenteEnCancelacion() {
        String code = "ISV-INV-" + UUID.randomUUID().toString().substring(0, 8);

        Producto p = new Producto();
        p.setNombre("Producto Test Stock " + code);
        p.setPrecio(50000.0);
        p.setCantidad(10);
        p.setEnStock(true);
        p = productoRepository.save(p);

        Reserva r = new Reserva();
        r.setCodigoReserva(code);
        r.setNombreCliente("Cliente Inventario");
        r.setTelefono("3007778888");
        r.setFechaCita(hoy.plusDays(50));
        r.setHoraCita(generarHorarioUnico());
        r.setEstado(ReservaService.PENDIENTE);
        ItemReserva item = new ItemReserva();
        item.setId(p.getId());
        item.setTipo("producto");
        item.setCantidad(2);
        item.setPrecioUnitario(50000.0);
        r.setItemsInventario(List.of(item));
        r.setInventarioReservado(true);
        p.setCantidad(8); // Ya se reservaron 2
        productoRepository.save(p);
        final Reserva guardada = reservaRepository.save(r);

        // Primera cancelación
        reservaService.cancelar(guardada);
        Producto after1 = productoRepository.findById(p.getId()).orElseThrow();
        assertEquals(10, after1.getCantidad()); // Devuelve los 2

        // Segunda cancelación (idempotente)
        reservaService.cancelar(guardada);
        Producto after2 = productoRepository.findById(p.getId()).orElseThrow();
        assertEquals(10, after2.getCantidad()); // No se vuelven a sumar
    }

    @Test
    @DisplayName("15. Dashboard: Cancelaciones de clientes no vistas se reflejan sin contar como ventas")
    public void testDashboardReflejaCancelacionesNoVistas() {
        String suffix = "-dash-" + UUID.randomUUID().toString().substring(0, 8);

        // 1 Cancelada no vista
        Reserva r2 = new Reserva();
        r2.setCodigoReserva("ISV-CAN" + suffix);
        r2.setNombreCliente("Cliente Cancelado Dash");
        r2.setTelefono("3001110002");
        r2.setFechaCita(hoy.plusDays(60));
        r2.setHoraCita(generarHorarioUnico());
        r2.setEstado(ReservaService.CANCELADO);
        r2.setCanceladaPor("CLIENTE");
        r2.setFechaCancelacion(Instant.now());
        r2.setNotificacionCancelacionVista(false);
        r2.setAnticipo(50000.0);
        reservaRepository.save(r2);

        ResponseEntity<Map<String, Object>> res = dashboardController.obtenerResumen();
        Map<String, Object> body = res.getBody();
        assertNotNull(body);

        long cancelsNoVistas = ((Number) body.get("cancelacionesNoVistas")).longValue();
        assertTrue(cancelsNoVistas >= 1);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> alertas = (List<Map<String, Object>>) body.get("alertasAtencion");
        assertTrue(alertas.stream().anyMatch(a -> "cancelacion_cliente".equals(a.get("tipo"))));
    }
}
