package com.isivi.app;

import com.isivi.app.controller.ReservaController;
import com.isivi.app.dto.GestionReservaRequest;
import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Reserva;
import com.isivi.app.model.Servicio;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.repository.ServicioRepository;
import com.isivi.app.util.HorarioUtil;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class HorarioValidationTest {

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    private String testServicioId;

    // Hora de referencia fija: 2026-08-15 11:22:00 en America/Bogota
    private static final ZoneId ZONE_BOGOTA = ZoneId.of("America/Bogota");
    private static final ZonedDateTime FIXED_NOW = ZonedDateTime.of(2026, 8, 15, 11, 22, 0, 0, ZONE_BOGOTA);
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_NOW.toInstant(), ZONE_BOGOTA);

    private static final LocalDate HOY = LocalDate.of(2026, 8, 15);
    private static final LocalDate AYER = LocalDate.of(2026, 8, 14);
    private static final LocalDate MANANA = LocalDate.of(2026, 8, 16);
    private static final LocalDate FUTURO = LocalDate.of(2026, 11, 20); // Viernes laboral

    @BeforeEach
    void setUp() {
        reservaController.setClock(FIXED_CLOCK);
        Servicio serv = servicioRepository.findAll().stream().findFirst().orElseGet(() -> {
            Servicio s = new Servicio();
            s.setNombre("Balayage Test");
            s.setPrecio(150000.0);
            s.setCategoria("Color");
            return servicioRepository.save(s);
        });
        testServicioId = serv.getId();
    }

    // ==========================================
    // CASO 1: Fecha futura + horario válido -> permitido
    // ==========================================
    @Test
    @DisplayName("1. Fecha futura + horario válido -> permitido (no pasado)")
    void testCaso1_FechaFuturaHorarioValido() {
        assertFalse(HorarioUtil.isPastTimeSlot(FUTURO, "09:30 AM", FIXED_CLOCK));
        assertFalse(HorarioUtil.isPastTimeSlot(FUTURO, "08:00 AM", FIXED_CLOCK));
    }

    // ==========================================
    // CASO 2: Fecha de hoy + horario futuro -> permitido
    // ==========================================
    @Test
    @DisplayName("2. Fecha de hoy + horario futuro -> permitido (no pasado)")
    void testCaso2_FechaHoyHorarioFuturo() {
        assertFalse(HorarioUtil.isPastTimeSlot(HOY, "01:30 PM", FIXED_CLOCK));
        assertFalse(HorarioUtil.isPastTimeSlot(HOY, "03:00 PM", FIXED_CLOCK));
        assertFalse(HorarioUtil.isPastTimeSlot(HOY, "04:30 PM", FIXED_CLOCK));
        assertFalse(HorarioUtil.isPastTimeSlot(HOY, "06:00 PM", FIXED_CLOCK));
    }

    // ==========================================
    // CASO 3: Fecha de hoy + horario exactamente igual a la hora actual -> rechazado
    // ==========================================
    @Test
    @DisplayName("3. Fecha de hoy + horario exactamente igual a la hora actual -> rechazado")
    void testCaso3_FechaHoyHorarioExactamenteIgual() {
        // Reloj a las 11:00 AM exactas
        Clock clock1100 = Clock.fixed(ZonedDateTime.of(2026, 8, 15, 11, 0, 0, 0, ZONE_BOGOTA).toInstant(), ZONE_BOGOTA);
        assertTrue(HorarioUtil.isPastTimeSlot(HOY, "11:00 AM", clock1100),
                "El horario exactamente igual a la hora actual debe considerarse pasado/no disponible");
    }

    // ==========================================
    // CASO 4: Fecha de hoy + horario pasado -> rechazado
    // ==========================================
    @Test
    @DisplayName("4. Fecha de hoy + horario pasado -> rechazado")
    void testCaso4_FechaHoyHorarioPasado() {
        assertTrue(HorarioUtil.isPastTimeSlot(HOY, "08:00 AM", FIXED_CLOCK));
        assertTrue(HorarioUtil.isPastTimeSlot(HOY, "09:30 AM", FIXED_CLOCK));
        assertTrue(HorarioUtil.isPastTimeSlot(HOY, "11:00 AM", FIXED_CLOCK));
    }

    // ==========================================
    // CASO 5: Fecha pasada -> rechazado
    // ==========================================
    @Test
    @DisplayName("5. Fecha pasada -> rechazado")
    void testCaso5_FechaPasada() {
        assertTrue(HorarioUtil.isPastTimeSlot(AYER, "06:00 PM", FIXED_CLOCK));
        assertTrue(HorarioUtil.isPastTimeSlot(LocalDate.of(2025, 1, 1), "08:00 AM", FIXED_CLOCK));
    }

    // ==========================================
    // CASO 6: Fecha de mañana + primer horario configurado -> permitido
    // ==========================================
    @Test
    @DisplayName("6. Fecha de mañana + primer horario configurado -> permitido")
    void testCaso6_FechaMananaPrimerHorario() {
        assertFalse(HorarioUtil.isPastTimeSlot(MANANA, "08:00 AM", FIXED_CLOCK));
    }

    // ==========================================
    // CASO 7: 12:00 AM -> conversión correcta
    // ==========================================
    @Test
    @DisplayName("7. 12:00 AM -> conversión correcta a 00:00")
    void testCaso7_Conversion1200AM() {
        LocalTime time = HorarioUtil.parseTimeSlot("12:00 AM");
        assertNotNull(time);
        assertEquals(LocalTime.of(0, 0), time);
    }

    // ==========================================
    // CASO 8: 12:00 PM -> conversión correcta
    // ==========================================
    @Test
    @DisplayName("8. 12:00 PM -> conversión correcta a 12:00")
    void testCaso8_Conversion1200PM() {
        LocalTime time = HorarioUtil.parseTimeSlot("12:00 PM");
        assertNotNull(time);
        assertEquals(LocalTime.of(12, 0), time);
    }

    // ==========================================
    // CASO 9: 01:30 PM -> conversión correcta
    // ==========================================
    @Test
    @DisplayName("9. 01:30 PM -> conversión correcta a 13:30")
    void testCaso9_Conversion0130PM() {
        LocalTime time = HorarioUtil.parseTimeSlot("01:30 PM");
        assertNotNull(time);
        assertEquals(LocalTime.of(13, 30), time);
    }

    // ==========================================
    // CASO 10: Horario futuro en la fecha actual -> permitido
    // ==========================================
    @Test
    @DisplayName("10. Horario futuro en la fecha actual -> permitido")
    void testCaso10_HorarioFuturoFechaActual() {
        assertFalse(HorarioUtil.isPastTimeSlot(HOY, "03:00 PM", FIXED_CLOCK));
        assertFalse(HorarioUtil.isPastTimeSlot(HOY, "04:30 PM", FIXED_CLOCK));
        assertFalse(HorarioUtil.isPastTimeSlot(HOY, "06:00 PM", FIXED_CLOCK));
    }

    // ==========================================
    // PRUEBAS DE INTEGRACIÓN: Endpoints ReservaController con Clock Fijo
    // ==========================================
    @Test
    @DisplayName("POST /api/reservas rechaza horario pasado de hoy con HORARIO_PASADO")
    void testCrearReservaHorarioPasadoRechazado() {
        Reserva r = new Reserva();
        r.setNombreCliente("Cliente Prueba");
        r.setTelefono("3001112233");
        r.setFechaCita(HOY);
        r.setHoraCita("08:00 AM"); // Ya pasó a las 11:22 AM

        ItemReserva item = new ItemReserva();
        item.setId(testServicioId);
        item.setTipo("servicio");
        item.setNombre("Balayage Test");
        item.setPrecioUnitario(150000.0);
        item.setSubtotal(150000.0);
        r.setItemsInventario(List.of(item));

        ResponseEntity<?> response = reservaController.crear(r);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("HORARIO_PASADO", body.get("error"));
        assertTrue(String.valueOf(body.get("mensaje")).contains("Ese horario ya pasó"));
    }

    @Test
    @DisplayName("PATCH /api/reservas/{id}/reprogramar rechaza horario pasado de hoy")
    void testReprogramarHorarioPasadoRechazado() {
        // Crear una reserva válida primero para fecha futura
        Reserva r = new Reserva();
        r.setNombreCliente("Cliente Reprog");
        r.setTelefono("3004445566");
        r.setFechaCita(FUTURO);
        r.setHoraCita("03:00 PM");

        ItemReserva item = new ItemReserva();
        item.setId(testServicioId);
        item.setTipo("servicio");
        item.setNombre("Balayage Test");
        item.setPrecioUnitario(150000.0);
        item.setSubtotal(150000.0);
        r.setItemsInventario(List.of(item));

        ResponseEntity<?> creadaResp = reservaController.crear(r);
        assertEquals(HttpStatus.CREATED, creadaResp.getStatusCode());
        Reserva creada = (Reserva) creadaResp.getBody();
        assertNotNull(creada);

        try {
            // Intentar reprogramar para HOY a las 09:30 AM (ya pasó a las 11:22 AM)
            GestionReservaRequest req = new GestionReservaRequest();
            req.setCodigoReserva(creada.getCodigoReserva());
            req.setTelefono("3004445566");
            req.setFechaCita(HOY);
            req.setHoraCita("09:30 AM");

            ResponseEntity<?> reprogResp = reservaController.reprogramar(creada.getId(), req);
            assertEquals(HttpStatus.BAD_REQUEST, reprogResp.getStatusCode());
            Map<?, ?> body = (Map<?, ?>) reprogResp.getBody();
            assertEquals("HORARIO_PASADO", body.get("error"));
        } finally {
            reservaRepository.delete(creada);
        }
    }

    @Test
    @DisplayName("PATCH /api/reservas/{id}/admin-reprogramar rechaza horario pasado de hoy")
    void testAdminReprogramarHorarioPasadoRechazado() {
        // Crear reserva válida primero
        Reserva r = new Reserva();
        r.setNombreCliente("Cliente Admin Reprog");
        r.setTelefono("3006667788");
        r.setFechaCita(FUTURO);
        r.setHoraCita("04:30 PM");

        ItemReserva item = new ItemReserva();
        item.setId(testServicioId);
        item.setTipo("servicio");
        item.setNombre("Balayage Test");
        item.setPrecioUnitario(150000.0);
        item.setSubtotal(150000.0);
        r.setItemsInventario(List.of(item));

        ResponseEntity<?> creadaResp = reservaController.crear(r);
        assertEquals(HttpStatus.CREATED, creadaResp.getStatusCode());
        Reserva creada = (Reserva) creadaResp.getBody();
        assertNotNull(creada);

        try {
            // Intentar reprogramación administrativa para HOY a las 11:00 AM (ya pasó a las 11:22 AM)
            Map<String, String> bodyReq = Map.of(
                    "fechaCita", HOY.toString(),
                    "horaCita", "11:00 AM"
            );

            ResponseEntity<?> reprogResp = reservaController.adminReprogramar(creada.getId(), bodyReq);
            assertEquals(HttpStatus.BAD_REQUEST, reprogResp.getStatusCode());
            Map<?, ?> body = (Map<?, ?>) reprogResp.getBody();
            assertEquals("HORARIO_PASADO", body.get("error"));
        } finally {
            reservaRepository.delete(creada);
        }
    }
}
