package com.isivi.app;

import com.isivi.app.controller.AgendaController;
import com.isivi.app.controller.ReservaController;
import com.isivi.app.model.BloqueoRecurrente;
import com.isivi.app.model.ExcepcionAgenda;
import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Reserva;
import com.isivi.app.model.Servicio;
import com.isivi.app.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RecurringAgendaBlockTest {

    @Autowired
    private AgendaController agendaController;

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private BloqueoRecurrenteRepository bloqueoRecurrenteRepository;

    @Autowired
    private ExcepcionAgendaRepository excepcionAgendaRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    private final ZoneId zonaBogota = ZoneId.of("America/Bogota");
    private Servicio servicio;

    @BeforeEach
    void setUp() {
        bloqueoRecurrenteRepository.deleteAll();
        excepcionAgendaRepository.deleteAll();
        reservaRepository.deleteAll();
        servicioRepository.deleteAll();
        servicio = servicioRepository.save(new Servicio("Corte", "Peluquería", 50000.0, "30 min", "Desc", ""));
    }

    @AfterEach
    void tearDown() {
        bloqueoRecurrenteRepository.deleteAll();
        excepcionAgendaRepository.deleteAll();
        reservaRepository.deleteAll();
        servicioRepository.deleteAll();
    }

    @Test
    @DisplayName("1. Bloqueo recurrente bloquea slot en el día configurado y no en otros días")
    void testBloqueoRecurrenteDisponibilidad() {
        // Bloquear todos los viernes (5) a las 06:00 PM
        BloqueoRecurrente recurrente = new BloqueoRecurrente(5, List.of("06:00 PM"), LocalDate.now(zonaBogota), null, "Mantenimiento semanal", "admin");
        bloqueoRecurrenteRepository.save(recurrente);

        // Buscar el próximo viernes y el próximo sábado
        LocalDate viernes = LocalDate.now(zonaBogota);
        while (viernes.getDayOfWeek() != DayOfWeek.FRIDAY) {
            viernes = viernes.plusDays(1);
        }
        LocalDate sabado = viernes.plusDays(1);

        List<String> ocupadosViernes = reservaController.disponibilidad(viernes.toString());
        assertTrue(ocupadosViernes.contains("06:00 PM"), "El viernes debe tener 06:00 PM bloqueado por la regla recurrente");

        List<String> ocupadosSabado = reservaController.disponibilidad(sabado.toString());
        assertFalse(ocupadosSabado.contains("06:00 PM"), "El sábado NO debe tener 06:00 PM bloqueado por la regla del viernes");
    }

    @Test
    @DisplayName("2. Detección de conflicto: no invalida reservas existentes silenciosamente")
    void testConflictoConReservasExistentes() {
        LocalDate proximoViernes = LocalDate.now(zonaBogota);
        while (proximoViernes.getDayOfWeek() != DayOfWeek.FRIDAY) {
            proximoViernes = proximoViernes.plusDays(1);
        }

        // Crear una reserva existente en ese viernes
        Reserva req = new Reserva();
        req.setNombreCliente("Marcela Torres");
        req.setTelefono("3154443322");
        req.setMedioPago("TRANSFERENCIA");
        req.setFechaCita(proximoViernes);
        req.setHoraCita("06:00 PM");
        req.setItemsInventario(List.of(new ItemReserva(servicio.getId(), "servicio", 1)));

        ResponseEntity<?> respCrear = reservaController.crear(req);
        assertEquals(HttpStatus.CREATED, respCrear.getStatusCode());

        // Intentar crear un bloqueo recurrente para todos los viernes a las 06:00 PM sin forzar
        ResponseEntity<?> respBloqueo = agendaController.guardarBloqueoRecurrente(Map.of(
                "diaSemana", 5,
                "horarios", List.of("06:00 PM"),
                "fechaInicio", LocalDate.now(zonaBogota).toString(),
                "motivo", "Reunión de equipo",
                "forzar", false
        ), null);

        assertEquals(HttpStatus.CONFLICT, respBloqueo.getStatusCode(), "Debe advertir sobre la reserva existente con 409 Conflict");
    }

    @Test
    @DisplayName("3. Pausar bloqueo recurrente libera el horario de inmediato")
    void testPausarBloqueoRecurrente() {
        BloqueoRecurrente recurrente = new BloqueoRecurrente(5, List.of("04:30 PM"), LocalDate.now(zonaBogota), null, "Pausa", "admin");
        recurrente = bloqueoRecurrenteRepository.save(recurrente);

        LocalDate viernes = LocalDate.now(zonaBogota);
        while (viernes.getDayOfWeek() != DayOfWeek.FRIDAY) {
            viernes = viernes.plusDays(1);
        }

        assertTrue(reservaController.disponibilidad(viernes.toString()).contains("04:30 PM"));

        // Pausar recurrente
        agendaController.actualizarBloqueoRecurrente(recurrente.getId(), Map.of("activo", false));

        assertFalse(reservaController.disponibilidad(viernes.toString()).contains("04:30 PM"), "Al pausar, el horario debe quedar libre");
    }
}
