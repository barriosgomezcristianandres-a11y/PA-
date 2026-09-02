package com.isivi.app;

import com.isivi.app.controller.DashboardController;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UpcomingAppointmentsTest {

    @Autowired
    private DashboardController dashboardController;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReservaService reservaService;

    private static final AtomicInteger counter = new AtomicInteger((int) (System.currentTimeMillis() % 50000) + 2000);
    private ZoneId zonaBogota = ZoneId.of("America/Bogota");

    private LocalDate nuevoDiaAislado() {
        LocalDate dia = LocalDate.of(2042, 1, 1).plusDays(counter.addAndGet(50));
        LocalDateTime mediodia = LocalDateTime.of(dia, LocalTime.of(12, 0));
        Clock fixedClock = Clock.fixed(mediodia.atZone(zonaBogota).toInstant(), zonaBogota);
        dashboardController.setClock(fixedClock);
        reservaService.setClock(fixedClock);
        return dia;
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() {
        dashboardController.setClock(Clock.system(zonaBogota));
        reservaService.setClock(Clock.system(zonaBogota));
    }

    private Reserva crearCita(String nombre, LocalDate fecha, String hora, String estado) {
        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-UP-" + UUID.randomUUID().toString().substring(0, 8));
        r.setNombreCliente(nombre);
        r.setTelefono("300" + (1000000 + counter.incrementAndGet()));
        r.setFechaCita(fecha);
        r.setHoraCita(hora);
        r.setEstado(estado);
        r.setItems(List.of("Corte y Estilo"));
        return reservaRepository.save(r);
    }

    @Test
    @DisplayName("1. Próxima Cita Hoy: Cálculo de tiempo restante y formato de minutos")
    public void testProximaCitaHoy() {
        LocalDate hoy = nuevoDiaAislado();
        // Reloj a las 12:00 PM. Cita a las 01:30 PM (90 min = 1 h 30 min)
        crearCita("Cliente 1:30pm", hoy, "01:30 PM", ReservaService.CONFIRMADO);
        crearCita("Cliente 04:00pm", hoy, "04:00 PM", ReservaService.CONFIRMADO);

        ResponseEntity<Map<String, Object>> resp = dashboardController.obtenerResumen();
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);

        @SuppressWarnings("unchecked")
        Map<String, Object> proxima = (Map<String, Object>) body.get("proximaCita");
        assertNotNull(proxima);
        assertEquals("Cliente 1:30pm", proxima.get("nombreCliente"));
        assertEquals("01:30 PM", proxima.get("horaCita"));
        assertEquals(90L, ((Number) proxima.get("minutosRestantes")).longValue());
        assertEquals("Faltan 1 h 30 min", proxima.get("tiempoRestanteTexto"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> proximas = (List<Map<String, Object>>) body.get("proximasCitas");
        assertEquals(2, proximas.size());
        assertEquals("Cliente 1:30pm", proximas.get(0).get("nombreCliente"));
        assertEquals("Cliente 04:00pm", proximas.get(1).get("nombreCliente"));
    }

    @Test
    @DisplayName("2. Formato de cuenta regresiva para menos de 1 min, minutos, horas y días")
    public void testFormatoTiempoRestante() {
        assertEquals("Faltan menos de 1 min", DashboardController.formatearTiempoRestante(0));
        assertEquals("Faltan 15 min", DashboardController.formatearTiempoRestante(15));
        assertEquals("Faltan 1 h 32 min", DashboardController.formatearTiempoRestante(92));
        assertEquals("Faltan 2 h", DashboardController.formatearTiempoRestante(120));
        assertEquals("Faltan 1 día 5 h", DashboardController.formatearTiempoRestante(1440 + 300));
        assertEquals("Faltan 3 días", DashboardController.formatearTiempoRestante(1440 * 3));
    }

    @Test
    @DisplayName("3. Exclusión estricta de citas Canceladas, Denegadas, Expiradas o Pasadas")
    public void testExclusionDeCitasNoActivas() {
        LocalDate hoy = nuevoDiaAislado();
        // Cita pasada hoy a las 10:00 AM (reloj está a las 12:00 PM)
        crearCita("Cliente Pasado", hoy, "10:00 AM", ReservaService.CONFIRMADO);
        // Cita futura cancelada
        crearCita("Cliente Cancelado", hoy, "02:00 PM", ReservaService.CANCELADO);
        // Cita futura denegada
        crearCita("Cliente Denegado", hoy, "03:00 PM", ReservaService.DENEGADO);
        // Cita futura expirada
        crearCita("Cliente Expirado", hoy, "05:00 PM", ReservaService.EXPIRADA);
        // Cita futura activa
        Reserva activa = crearCita("Cliente Activo", hoy, "06:00 PM", ReservaService.CONFIRMADO);

        ResponseEntity<Map<String, Object>> resp = dashboardController.obtenerResumen();
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);

        @SuppressWarnings("unchecked")
        Map<String, Object> proxima = (Map<String, Object>) body.get("proximaCita");
        assertNotNull(proxima);
        assertEquals(activa.getCodigoReserva(), proxima.get("codigoReserva"));
        assertEquals("Cliente Activo", proxima.get("nombreCliente"));
    }

    @Test
    @DisplayName("4. Transición dinámica: Al cancelar una cita próxima, la siguiente ocupa su lugar sin duplicación")
    public void testCancelacionPromueveSiguienteCita() {
        LocalDate hoy = nuevoDiaAislado();
        Reserva primera = crearCita("Primera Cita", hoy, "02:00 PM", ReservaService.CONFIRMADO);
        Reserva segunda = crearCita("Segunda Cita", hoy, "04:30 PM", ReservaService.CONFIRMADO);

        // Antes de cancelar: la primera es 02:00 PM
        ResponseEntity<Map<String, Object>> resp1 = dashboardController.obtenerResumen();
        Map<String, Object> proxima1 = (Map<String, Object>) resp1.getBody().get("proximaCita");
        assertEquals(primera.getCodigoReserva(), proxima1.get("codigoReserva"));

        // Administrador cancela la primera cita
        reservaService.cancelarAdministrativa(primera, "Cancelada por fuerza mayor");

        // Después de cancelar: la segunda pasa inmediatamente a ser la próxima
        ResponseEntity<Map<String, Object>> resp2 = dashboardController.obtenerResumen();
        Map<String, Object> proxima2 = (Map<String, Object>) resp2.getBody().get("proximaCita");
        assertNotNull(proxima2);
        assertEquals(segunda.getCodigoReserva(), proxima2.get("codigoReserva"));
        assertEquals("Segunda Cita", proxima2.get("nombreCliente"));
    }
}
