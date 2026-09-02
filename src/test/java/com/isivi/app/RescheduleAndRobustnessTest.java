package com.isivi.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isivi.app.controller.ReservaController;
import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RescheduleAndRobustnessTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.isivi.app.service.WhatsAppNotificationService whatsappMock;

    private static final java.time.ZoneId ZONE_BOGOTA = java.time.ZoneId.of("America/Bogota");
    private static final java.time.ZonedDateTime FIXED_NOW = java.time.ZonedDateTime.of(2026, 8, 15, 11, 22, 0, 0, ZONE_BOGOTA);
    private static final java.time.Clock FIXED_CLOCK = java.time.Clock.fixed(FIXED_NOW.toInstant(), ZONE_BOGOTA);

    private static final LocalDate FUTURO = LocalDate.of(2026, 11, 20); // Viernes laboral

    private final ObjectMapper mapper = new ObjectMapper();
    private String adminToken;

    @BeforeEach
    public void setup() {
        org.springframework.test.util.ReflectionTestUtils.setField(whatsappMock, "accessToken", "mockToken");
        org.springframework.test.util.ReflectionTestUtils.setField(whatsappMock, "phoneNumberId", "mockId");
        Object targetController = org.springframework.test.util.AopTestUtils.getTargetObject(reservaController);
        org.springframework.test.util.ReflectionTestUtils.setField(targetController, "whatsapp", whatsappMock);
        reservaController.setClock(FIXED_CLOCK);
        reservaRepository.deleteAll();
        adminToken = "Bearer " + jwtService.generarToken("admin");
    }

    @Test
    public void testReprogramacionClienteYResolucionAdmin() throws Exception {
        // 1. Crear reserva original confirmada
        Reserva r = new Reserva();
        r.setNombreCliente("Cliente Reprog");
        r.setTelefono("3001234567");
        r.setFechaCita(FUTURO);
        r.setHoraCita("09:30 AM");
        r.setEstado("Confirmado");
        r.setCodigoReserva("ISV-TEST1");
        r.setMontoPagoCentavos(2500000L);
        ItemReserva itemSrv = new ItemReserva();
        itemSrv.setId("srv-1");
        itemSrv.setNombre("Corte de Cabello");
        itemSrv.setTipo("servicio");
        itemSrv.setCantidad(1);
        r.setItemsInventario(List.of(itemSrv));
        r = reservaRepository.save(r);

        // 2. Solicitar reprogramacion (Cliente)
        Map<String, String> bodyCliente = Map.of(
                "fechaCita", FUTURO.plusDays(7).toString(), // Nov 27, 2026 (Viernes)
                "horaCita", "11:00 AM",
                "codigoReserva", "ISV-TEST1",
                "telefono", "3001234567"
        );

        mockMvc.perform(patch("/api/reservas/" + r.getId() + "/reprogramar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(bodyCliente)))
                .andExpect(status().isOk());

        Reserva actualizada = reservaRepository.findById(r.getId()).orElseThrow();
        assertEquals("Pendiente Reprogramación", actualizableEstado(actualizada));
        assertEquals(FUTURO, actualizada.getFechaCita()); // se mantiene la original
        assertEquals("09:30 AM", actualizada.getHoraCita()); // se mantiene la original
        assertEquals(FUTURO.plusDays(7), actualizada.getFechaPropuestaReprogramacion());
        assertEquals("11:00 AM", actualizada.getHoraPropuestaReprogramacion());
        assertEquals("Confirmado", actualizada.getEstadoPrevioReprogramacion());

        // 3. Aprobar reprogramación (Admin)
        mockMvc.perform(patch("/api/reservas/" + r.getId() + "/aprobar-reprogramacion")
                .header("Authorization", adminToken))
                .andExpect(status().isOk());

        Reserva aprobada = reservaRepository.findById(r.getId()).orElseThrow();
        assertEquals("Confirmado", aprobada.getEstado());
        assertEquals(FUTURO.plusDays(7), aprobada.getFechaCita());
        assertEquals("11:00 AM", aprobada.getHoraCita());
        assertNull(aprobada.getFechaPropuestaReprogramacion());
        assertNull(aprobada.getHoraPropuestaReprogramacion());
        assertNull(aprobada.getEstadoPrevioReprogramacion());
    }

    @Test
    public void testEnviarRecordatoriosWhatsApp_ComportamientoYErrores() {
        LocalDate mañana = LocalDate.now(FIXED_CLOCK).plusDays(1);

        // Reserva 1: Enviada con éxito
        Reserva r1 = new Reserva();
        r1.setCodigoReserva("ISV-REC1");
        r1.setNombreCliente("Cliente Recordatorio 1");
        r1.setTelefono("3001111111");
        r1.setFechaCita(mañana);
        r1.setHoraCita("08:00 AM");
        r1.setEstado("Confirmado");
        r1.setRecordatorioEnviado(false);
        r1 = reservaRepository.save(r1);

        // Reserva 2: Error de red (timeout) -> no debe marcarse como enviado
        Reserva r2 = new Reserva();
        r2.setCodigoReserva("ISV-REC2");
        r2.setNombreCliente("Cliente Recordatorio 2");
        r2.setTelefono("3002222222");
        r2.setFechaCita(mañana);
        r2.setHoraCita("09:30 AM");
        r2.setEstado("Confirmado");
        r2.setRecordatorioEnviado(false);
        r2 = reservaRepository.save(r2);

        // 1. Simular éxito para R1 cambiando el token de configuración
        Object targetService = org.springframework.test.util.AopTestUtils.getTargetObject(whatsappMock);
        org.springframework.test.util.ReflectionTestUtils.setField(targetService, "accessToken", "validRealMockToken");
        org.springframework.test.util.ReflectionTestUtils.setField(targetService, "phoneNumberId", "mockId");

        // Creamos una subclase local/mock del servicio real y la inyectamos manualmente para mayor fidelidad técnica
        com.isivi.app.service.WhatsAppNotificationService customService = new com.isivi.app.service.WhatsAppNotificationService(mongoTemplate, null) {
            @Override
            public void enviar(Reserva r, String mensaje) {
                if ("ISV-REC2".equals(r.getCodigoReserva())) {
                    throw new org.springframework.web.client.ResourceAccessException("Timeout de red");
                }
                // Éxito para los demás
            }
        };
        org.springframework.test.util.ReflectionTestUtils.setField(customService, "accessToken", "validRealMockToken");
        org.springframework.test.util.ReflectionTestUtils.setField(customService, "phoneNumberId", "mockId");
        
        Object targetController = org.springframework.test.util.AopTestUtils.getTargetObject(reservaController);
        org.springframework.test.util.ReflectionTestUtils.setField(targetController, "whatsapp", customService);

        // Ejecutar scheduler de recordatorios
        reservaController.enviarRecordatoriosWhatsAppProgramado();

        // Verificar resultados en base de datos
        Reserva r1Db = reservaRepository.findById(r1.getId()).orElseThrow();
        assertTrue(r1Db.getRecordatorioEnviado(), "Debe marcarse como enviado");

        Reserva r2Db = reservaRepository.findById(r2.getId()).orElseThrow();
        assertFalse(r2Db.getRecordatorioEnviado(), "No debe marcarse como enviado tras ResourceAccessException");
    }

    @Test
    public void testAgendaExcepciones_BloqueosYAlertasAccionables() throws Exception {
        LocalDate fechaBloqueo = LocalDate.of(2026, 12, 10);
        reservaRepository.findByFechaCita(fechaBloqueo).forEach(reservaRepository::delete);

        // Caso A: Crear CERRADO en día sin citas -> permitido
        mockMvc.perform(post("/api/agenda/excepciones")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "fecha", fechaBloqueo.toString(),
                        "tipo", "CERRADO",
                        "motivo", "Día festivo"
                ))))
                .andExpect(status().isOk());

        // Limpiar para el siguiente escenario
        reservaRepository.deleteAll();

        // Caso B: Crear CERRADO en día con 1 cita activa -> conflicto accionable
        Reserva cita = new Reserva();
        cita.setCodigoReserva("ISV-CITA1");
        cita.setNombreCliente("Cliente Cita 1");
        cita.setTelefono("3005555555");
        cita.setFechaCita(fechaBloqueo);
        cita.setHoraCita("09:30 AM");
        cita.setEstado("Confirmado");
        reservaRepository.save(cita);

        mockMvc.perform(post("/api/agenda/excepciones")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "fecha", fechaBloqueo.toString(),
                        "tipo", "CERRADO",
                        "motivo", "Cierre inesperado"
                ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICTO_RESERVAS"))
                .andExpect(jsonPath("$.mensaje").value("El día " + fechaBloqueo + " tiene 1 citas activas. No es posible aplicar la excepción de tipo CERRADO sin gestionar previamente esas reservas."));

        // Caso C: Citas canceladas no deben contarse como activas
        cita.setEstado("Cancelada");
        reservaRepository.save(cita);

        mockMvc.perform(post("/api/agenda/excepciones")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "fecha", fechaBloqueo.toString(),
                        "tipo", "CERRADO",
                        "motivo", "Cierre inesperado sin activas"
                ))))
                .andExpect(status().isOk());
    }

    @Test
    public void testBypassReprogramacionReservasTerminales() throws Exception {
        List<String> estadosTerminales = List.of("Expirada", "Denegada", "Cancelada");

        for (String estado : estadosTerminales) {
            // 1. Crear reserva original con estado terminal
            Reserva r = new Reserva();
            r.setNombreCliente("Cliente Term " + estado);
            r.setTelefono("3009999999");
            r.setFechaCita(FUTURO);
            r.setHoraCita("09:30 AM");
            r.setEstado(estado);
            r.setCodigoReserva("ISV-" + estado.toUpperCase());
            r.setMontoPagoCentavos(2500000L);
            ItemReserva itemSrv = new ItemReserva();
            itemSrv.setId("srv-1");
            itemSrv.setNombre("Corte de Cabello");
            itemSrv.setTipo("servicio");
            itemSrv.setCantidad(1);
            r.setItemsInventario(List.of(itemSrv));
            r = reservaRepository.save(r);

            // Intentar reprogramar (Cliente) -> Debe fallar
            Map<String, String> bodyCliente = Map.of(
                    "fechaCita", FUTURO.plusDays(7).toString(),
                    "horaCita", "11:00 AM",
                    "codigoReserva", r.getCodigoReserva(),
                    "telefono", r.getTelefono()
            );

            mockMvc.perform(patch("/api/reservas/" + r.getId() + "/reprogramar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(bodyCliente)))
                    .andExpect(status().isBadRequest());

            // Verificar que no se modificó ningún campo
            Reserva dbRes = reservaRepository.findById(r.getId()).orElseThrow();
            assertEquals(estado, dbRes.getEstado());
            assertEquals(FUTURO, dbRes.getFechaCita());
            assertEquals("09:30 AM", dbRes.getHoraCita());
            assertNull(dbRes.getFechaPropuestaReprogramacion());
            assertNull(dbRes.getHoraPropuestaReprogramacion());

            // Intentar aprobar reprogramacion administrativamente -> Debe fallar
            mockMvc.perform(patch("/api/reservas/" + r.getId() + "/aprobar-reprogramacion")
                    .header("Authorization", adminToken))
                    .andExpect(status().isBadRequest());

            Reserva dbResPostAdmin = reservaRepository.findById(r.getId()).orElseThrow();
            assertEquals(estado, dbResPostAdmin.getEstado());
        }
    }

    private String actualizableEstado(Reserva r) {
        return r.getEstado();
    }
}
