package com.isivi.app;

import com.isivi.app.controller.DashboardController;
import com.isivi.app.controller.ReservaController;
import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Reserva;
import com.isivi.app.model.Servicio;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.repository.ServicioRepository;
import com.isivi.app.service.ReservaService;
import com.isivi.app.util.HorarioUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AppointmentCompletionTest {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private DashboardController dashboardController;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    private LocalDate hoy;
    private final ZoneId zoneBogota = ZoneId.of("America/Bogota");

    @BeforeEach
    public void setup() {
        hoy = LocalDate.now(zoneBogota);
    }

    @Test
    @DisplayName("1. HorarioUtil: Parseo correcto de duraciones y cálculo de horas de finalización")
    public void testDurationParsingAndEndTimes() {
        assertEquals(60, HorarioUtil.parseDurationMinutes("60 min"));
        assertEquals(90, HorarioUtil.parseDurationMinutes("1h 30min"));
        assertEquals(120, HorarioUtil.parseDurationMinutes("2 horas"));
        assertEquals(45, HorarioUtil.parseDurationMinutes("45"));
        assertEquals(60, HorarioUtil.parseDurationMinutes(null)); // Default 60

        assertEquals(LocalTime.of(10, 30), HorarioUtil.calculateEndTime("09:00", 90));
        assertEquals(LocalTime.of(12, 0), HorarioUtil.calculateEndTime("10:00", 120));
        assertEquals(LocalTime.of(14, 45), HorarioUtil.calculateEndTime("14:00", 45));
    }


    @Test
    @DisplayName("2. Determinación de cita finalizada vs en curso según reloj y duración")
    public void testAppointmentCompletionLogic() {
        // Simular que son las 11:30 AM
        ZonedDateTime fixedDateTime = ZonedDateTime.of(hoy, LocalTime.of(11, 30), zoneBogota);
        Clock fixedClock = Clock.fixed(fixedDateTime.toInstant(), zoneBogota);

        // Cita 1: 09:00 AM con 60 min -> Termina 10:00 AM -> Ya está finalizada a las 11:30 AM
        assertTrue(HorarioUtil.isAppointmentCompleted(hoy, "09:00", 60, fixedClock), "Cita terminada a las 10:00 debe considerarse finalizada");
        assertFalse(HorarioUtil.isAppointmentInProgress(hoy, "09:00", 60, fixedClock));

        // Cita 2: 11:00 AM con 60 min -> Termina 12:00 PM -> Está EN CURSO a las 11:30 AM
        assertFalse(HorarioUtil.isAppointmentCompleted(hoy, "11:00", 60, fixedClock));
        assertTrue(HorarioUtil.isAppointmentInProgress(hoy, "11:00", 60, fixedClock), "Cita de 11:00 a 12:00 debe estar en curso a las 11:30");

        // Cita 3: 02:00 PM con 60 min -> Aún no inicia a las 11:30 AM
        assertFalse(HorarioUtil.isAppointmentCompleted(hoy, "14:00", 60, fixedClock));
        assertFalse(HorarioUtil.isAppointmentInProgress(hoy, "14:00", 60, fixedClock));
    }

    @Test
    @DisplayName("3. Citas de hoy: las citas finalizadas no deben saturar la agenda activa")
    public void testCompletedAppointmentsExclusionFromActiveAgenda() {
        ZonedDateTime fixedDateTime = ZonedDateTime.of(hoy, LocalTime.of(11, 30), zoneBogota);
        Clock fixedClock = Clock.fixed(fixedDateTime.toInstant(), zoneBogota);
        dashboardController.setClock(fixedClock);

        try {
            // Guardar servicio con 45 min
            Servicio corte = new Servicio();
            corte.setNombre("Corte Exprés " + System.currentTimeMillis());
            corte.setCategoria("corte");
            corte.setPrecio(35000.0);
            corte.setDuracion("45 min");
            corte = servicioRepository.save(corte);

            // Limpiar posible slot 06:00 previo en hoy
            reservaRepository.findByFechaCita(hoy).stream()
                    .filter(r -> "06:00".equals(r.getHoraCita()))
                    .forEach(reservaRepository::delete);

            // Crear cita confirmada en un horario ya pasado de hoy (06:00 AM)
            Reserva pastBooking = new Reserva();
            pastBooking.setCodigoReserva("ISV-PAST-" + System.currentTimeMillis());
            pastBooking.setNombreCliente("Cliente Madrugador");
            pastBooking.setTelefono("3005556677");
            pastBooking.setFechaCita(hoy);
            pastBooking.setHoraCita("06:00");
            pastBooking.setEstado("Confirmado");
            pastBooking.setEstadoPago("APROBADO");
            pastBooking.setAnticipo(10000.0);

            ItemReserva item = new ItemReserva(corte.getId(), "servicio", 1);
            item.setNombre(corte.getNombre());
            item.setPrecioUnitario(35000.0);
            item.setSubtotal(35000.0);
            pastBooking.getItemsInventario().add(item);
            pastBooking.getItems().add(corte.getNombre());

            Reserva savedBooking = reservaRepository.save(pastBooking);

            // Verificar cálculo de duración desde servicio
            int duracion = reservaService.calcularDuracionMinutos(savedBooking);
            assertEquals(45, duracion);

            // Al consultar dashboard, esta cita de las 06:00 AM (45 min) ya finalizó
            // y no debe aparecer en proximasCitas
            ResponseEntity<Map<String, Object>> dashResp = dashboardController.obtenerResumen();
            Map<String, Object> dash = dashResp.getBody();
            assertNotNull(dash);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> proximas = (List<Map<String, Object>>) dash.get("proximasCitas");
            assertNotNull(proximas);

            final String targetId = savedBooking.getId();
            boolean inUpcoming = proximas.stream().anyMatch(m -> targetId.equals(m.get("id")));
            assertFalse(inUpcoming, "Una cita de 06:00 AM con 45 min de duración no debe figurar en próximas citas pendientes");
        } finally {
            dashboardController.setClock(Clock.system(zoneBogota));
        }
    }
}
