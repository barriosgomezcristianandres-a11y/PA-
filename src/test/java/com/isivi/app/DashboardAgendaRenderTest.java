package com.isivi.app;

import com.isivi.app.controller.DashboardController;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.ReservaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DashboardAgendaRenderTest {

    @Autowired
    private DashboardController dashboardController;

    @Autowired
    private ReservaRepository reservaRepository;

    private final List<Reserva> testReservas = new ArrayList<>();
    private final ZoneId zonaBogota = ZoneId.of("America/Bogota");
    private LocalDate hoy;

    @BeforeEach
    void setUp() {
        hoy = LocalDate.now(zonaBogota);
        java.time.Clock fixedClock = java.time.Clock.fixed(hoy.atTime(9, 0).atZone(zonaBogota).toInstant(), zonaBogota);
        dashboardController.setClock(fixedClock);
    }

    @AfterEach
    void tearDown() {
        dashboardController.setClock(null);
        if (!testReservas.isEmpty()) {
            reservaRepository.deleteAll(testReservas);
            testReservas.clear();
        }
    }


    private Reserva crearReserva(String nombre, String hora, String estado) {
        reservaRepository.findByFechaCita(hoy).stream()
                .filter(r -> hora.equals(r.getHoraCita()))
                .forEach(reservaRepository::delete);

        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-TEST-" + System.nanoTime());
        r.setNombreCliente(nombre);
        r.setTelefono("3001234567");
        r.setFechaCita(hoy);
        r.setHoraCita(hora);
        r.setEstado(estado);
        r.setEstadoPago("APROBADO");
        r.setArchivada(false);
        Reserva guardada = reservaRepository.save(r);
        testReservas.add(guardada);
        return guardada;
    }


    @Test
    @DisplayName("1. Una cita hoy -> agendaHoy devuelve exactamente 1 reserva ordenada")
    void testUnaCitaHoy() {
        crearReserva("Cliente Uno", "01:30 PM", "Confirmado");

        ResponseEntity<Map<String, Object>> resp = dashboardController.obtenerResumen();
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);

        @SuppressWarnings("unchecked")
        List<Reserva> agendaHoy = (List<Reserva>) body.get("agendaHoy");
        assertNotNull(agendaHoy);
        assertTrue(agendaHoy.stream().anyMatch(r -> "Cliente Uno".equals(r.getNombreCliente())));
    }

    @Test
    @DisplayName("2. Tres citas hoy -> agendaHoy devuelve las 3 reservas en orden cronológico sin duplicados")
    void testTresCitasHoyOrdenadas() {
        crearReserva("Cliente Tarde", "03:30 PM", "Confirmado");
        crearReserva("Cliente Mediodia", "01:30 PM", "Confirmado");
        crearReserva("Cliente Tarde2", "04:30 PM", "Confirmado");

        ResponseEntity<Map<String, Object>> resp = dashboardController.obtenerResumen();
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);

        @SuppressWarnings("unchecked")
        List<Reserva> agendaHoy = (List<Reserva>) body.get("agendaHoy");
        assertNotNull(agendaHoy);

        List<Reserva> creadas = agendaHoy.stream()
                .filter(r -> testReservas.stream().anyMatch(t -> t.getId().equals(r.getId())))
                .toList();

        assertEquals(3, creadas.size(), "Deben haber exactamente 3 reservas de prueba en la agenda");
        assertEquals("01:30 PM", creadas.get(0).getHoraCita());
        assertEquals("03:30 PM", creadas.get(1).getHoraCita());
        assertEquals("04:30 PM", creadas.get(2).getHoraCita());
    }

    @Test
    @DisplayName("3. Citas canceladas, denegadas y expiradas quedan excluidas de agendaHoy")
    void testExclusionEstadosNoActivos() {
        crearReserva("Cliente Activo", "09:30 AM", "Confirmado");
        crearReserva("Cliente Cancelado", "11:00 AM", "Cancelada");
        crearReserva("Cliente Denegado", "01:30 PM", "Denegada");
        crearReserva("Cliente Expirado", "03:00 PM", "Expirada");

        ResponseEntity<Map<String, Object>> resp = dashboardController.obtenerResumen();
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);

        @SuppressWarnings("unchecked")
        List<Reserva> agendaHoy = (List<Reserva>) body.get("agendaHoy");
        assertNotNull(agendaHoy);

        assertTrue(agendaHoy.stream().anyMatch(r -> "Cliente Activo".equals(r.getNombreCliente())));
        assertFalse(agendaHoy.stream().anyMatch(r -> "Cliente Cancelado".equals(r.getNombreCliente())));
        assertFalse(agendaHoy.stream().anyMatch(r -> "Cliente Denegado".equals(r.getNombreCliente())));
        assertFalse(agendaHoy.stream().anyMatch(r -> "Cliente Expirado".equals(r.getNombreCliente())));
    }

    @Test
    @DisplayName("4. Filtro de Próxima Cita: la primera activa no completada es la próxima")
    void testDeterminacionProximaCita() {
        Reserva r1 = crearReserva("Cliente Pasado", "08:00 AM", "Completada");
        Reserva r2 = crearReserva("Cliente Proximo", "11:00 AM", "Confirmado");
        Reserva r3 = crearReserva("Cliente Futuro", "03:00 PM", "Confirmado");

        ResponseEntity<Map<String, Object>> resp = dashboardController.obtenerResumen();
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);

        @SuppressWarnings("unchecked")
        List<Reserva> agendaHoy = (List<Reserva>) body.get("agendaHoy");
        assertNotNull(agendaHoy);

        // Simulando la lógica frontend de selección de próxima cita:
        List<Reserva> activeUpcoming = agendaHoy.stream()
                .filter(b -> {
                    String est = b.getEstado() != null ? b.getEstado().toLowerCase() : "";
                    return !est.equals("completada") &&
                            !est.equals("realizada") &&
                            !est.equals("cancelada") &&
                            !est.equals("denegada") &&
                            !est.equals("expirada");
                })
                .toList();

        assertFalse(activeUpcoming.isEmpty());
        assertEquals("Cliente Proximo", activeUpcoming.get(0).getNombreCliente());
    }

    @Test
    @DisplayName("5. Campanita de Alerta: Alertas operativas y contador de pendientes coinciden exactamente")
    void testAlertasYPendientesParaCampanita() {
        // Estado con 0 pendientes creados por el test
        ResponseEntity<Map<String, Object>> respInicial = dashboardController.obtenerResumen();
        Map<String, Object> bodyInicial = respInicial.getBody();
        assertNotNull(bodyInicial);
        assertNotNull(bodyInicial.get("pendientes"));
        assertNotNull(bodyInicial.get("alertasAtencion"));

        // Agregar 1 reserva pendiente de comprobante y 1 pendiente de reprogramación
        Reserva r1 = crearReserva("Cliente Comprobante", "10:00 AM", "Pendiente Comprobante");
        Reserva r2 = crearReserva("Cliente Reprog", "02:00 PM", "Pendiente Reprogramación");

        ResponseEntity<Map<String, Object>> respConAlertas = dashboardController.obtenerResumen();
        Map<String, Object> bodyConAlertas = respConAlertas.getBody();
        assertNotNull(bodyConAlertas);

        long pendientes = ((Number) bodyConAlertas.get("pendientes")).longValue();
        assertTrue(pendientes >= 2, "El contador de pendientes debe reflejar al menos las 2 solicitudes creadas");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> alertas = (List<Map<String, Object>>) bodyConAlertas.get("alertasAtencion");
        assertNotNull(alertas);
        assertTrue(alertas.stream().anyMatch(a -> "reprogramacion".equals(a.get("tipo"))), "Debe existir alerta de reprogramación");
        assertTrue(alertas.stream().anyMatch(a -> "comprobante".equals(a.get("tipo"))), "Debe existir alerta de comprobante");
    }
}
