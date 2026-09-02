package com.isivi.app;

import com.isivi.app.controller.ReservaController;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ReservationHistoryTest {

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReservaService reservaService;

    private static final AtomicInteger counter = new AtomicInteger(800);
    private ZoneId zonaBogota = ZoneId.of("America/Bogota");
    private LocalDate hoy;

    @BeforeEach
    public void setup() {
        hoy = LocalDate.of(2037, 8, 1).plusDays(counter.addAndGet(10));
        LocalDateTime mediodiaHoy = LocalDateTime.of(hoy, LocalTime.of(12, 0));
        Clock fixedClock = Clock.fixed(mediodiaHoy.atZone(zonaBogota).toInstant(), zonaBogota);
        reservaService.setClock(fixedClock);
        reservaController.setClock(fixedClock);
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() {
        reservaService.setClock(Clock.system(zonaBogota));
        reservaController.setClock(Clock.system(zonaBogota));
    }

    private String generarHorarioUnico() {
        int val = counter.incrementAndGet();
        int hora = (val / 60) % 12;
        int min = val % 60;
        return String.format("%02d:%02d AM", hora == 0 ? 12 : hora, min);
    }

    @Test
    @DisplayName("1. Cancelación de Cliente Futura: Aparece inmediatamente en historial=true y no en historial=false")
    public void testCancelacionClienteFuturaEnHistorial() {
        String code = "ISV-HIST-" + UUID.randomUUID().toString().substring(0, 8);
        Reserva r = new Reserva();
        r.setCodigoReserva(code);
        r.setNombreCliente("Cliente Historial Futuro");
        r.setTelefono("300" + (2000000 + counter.incrementAndGet()));
        r.setFechaCita(hoy.plusDays(15)); // Cita en 15 días (>24h)
        r.setHoraCita(generarHorarioUnico());
        r.setEstado(ReservaService.CONFIRMADO);
        r.setEstadoPago("APROBADO");
        r.setAnticipo(30000.0);
        r.setArchivada(false); // No archivada porque su fecha aún no ha pasado
        Reserva guardada = reservaRepository.save(r);

        // Cliente cancela directamente con motivo
        Map<String, Object> cancelResult = reservaService.procesarCancelacionCliente(guardada, "Viaje de trabajo imprevisto");
        assertEquals(ReservaService.CANCELADO, cancelResult.get("estado"));

        // 1. Debe aparecer en historial=true
        List<Reserva> historial = reservaController.listar(null, true);
        assertTrue(historial.stream().anyMatch(h -> code.equals(h.getCodigoReserva())), "La reserva cancelada debe estar presente en historial=true");

        Reserva canceladaEnHistorial = historial.stream().filter(h -> code.equals(h.getCodigoReserva())).findFirst().orElseThrow();
        assertEquals(ReservaService.CANCELADO, canceladaEnHistorial.getEstado());
        assertEquals("CLIENTE", canceladaEnHistorial.getCanceladaPor());
        assertEquals("Viaje de trabajo imprevisto", canceladaEnHistorial.getMotivoCancelacion());
        assertNotNull(canceladaEnHistorial.getFechaCancelacion());

        // 2. NO debe aparecer en activas (historial=false)
        List<Reserva> activas = reservaController.listar(null, false);
        assertFalse(activas.stream().anyMatch(a -> code.equals(a.getCodigoReserva())), "La reserva cancelada NO debe estar en activas");

        // 3. El documento sigue existiendo en MongoDB (inmutable, no se borró)
        assertTrue(reservaRepository.findById(guardada.getId()).isPresent());
    }

    @Test
    @DisplayName("2. Cancelación por Administrador: Registra canceladaPor=ADMIN y motivo en historial")
    public void testCancelacionAdminEnHistorial() {
        String code = "ISV-ADM-" + UUID.randomUUID().toString().substring(0, 8);
        Reserva r = new Reserva();
        r.setCodigoReserva(code);
        r.setNombreCliente("Cliente Cancelado Por Admin");
        r.setTelefono("300" + (2000000 + counter.incrementAndGet()));
        r.setFechaCita(hoy.plusDays(3));
        r.setHoraCita(generarHorarioUnico());
        r.setEstado(ReservaService.CONFIRMADO);
        Reserva guardada = reservaRepository.save(r);

        // Admin cancela
        reservaService.cancelarAdministrativa(guardada, "Mantenimiento eléctrico del local");

        List<Reserva> historial = reservaController.listar(null, true);
        Reserva encontrada = historial.stream().filter(h -> code.equals(h.getCodigoReserva())).findFirst().orElseThrow();

        assertEquals(ReservaService.CANCELADO, encontrada.getEstado());
        assertEquals("ADMIN", encontrada.getCanceladaPor());
        assertEquals("Mantenimiento eléctrico del local", encontrada.getMotivoCancelacion());
        assertNotNull(encontrada.getFechaCancelacion());
    }

    @Test
    @DisplayName("3. Liberación Provisional (Elegir otro horario) se conserva y distingue de cancelación comercial")
    public void testLiberacionProvisionalSeparada() {
        String code = "ISV-PROV-" + UUID.randomUUID().toString().substring(0, 8);
        Reserva r = new Reserva();
        r.setCodigoReserva(code);
        r.setNombreCliente("Cliente Hold Cambio");
        r.setTelefono("300" + (2000000 + counter.incrementAndGet()));
        r.setFechaCita(hoy.plusDays(4));
        r.setHoraCita(generarHorarioUnico());
        r.setEstado(ReservaService.PENDIENTE_PAGO);
        Reserva guardada = reservaRepository.save(r);

        // Libera retención provisional
        Reserva liberada = reservaService.liberarRetencionProvisional(guardada, r.getTelefono(), code);
        assertEquals("LIBERACION_PROVISIONAL", liberada.getCanceladaPor());

        // Aparece en historial técnico pero no como cancelación de cliente
        List<Reserva> historial = reservaController.listar(null, true);
        Reserva enHist = historial.stream().filter(h -> code.equals(h.getCodigoReserva())).findFirst().orElseThrow();
        assertEquals("LIBERACION_PROVISIONAL", enHist.getCanceladaPor());
        assertNotEquals("CLIENTE", enHist.getCanceladaPor());
    }
}
