package com.isivi.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isivi.app.model.*;
import com.isivi.app.repository.*;
import com.isivi.app.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AgendaManagementTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConfiguracionAgendaRepository configuracionAgendaRepository;

    @Autowired
    private ExcepcionAgendaRepository excepcionAgendaRepository;

    @Autowired
    private BloqueoHorarioRepository bloqueoHorarioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String adminToken;
    private static final LocalDate TEST_DATE = LocalDate.of(2026, 11, 20);
    private static final LocalDate TEST_DATE_SPECIAL = LocalDate.of(2026, 11, 21);
    private static final LocalDate TEST_DATE_CLOSED = LocalDate.of(2026, 11, 22);

    @BeforeEach
    void setUp() {
        administradorRepository.findAllByUsuario("admin").forEach(administradorRepository::delete);
        administradorRepository.save(new Administrador("admin", passwordEncoder.encode("1234")));

        adminToken = jwtService.generarToken("admin");

        // Limpiar configuración de pruebas
        excepcionAgendaRepository.deleteAll();
        bloqueoHorarioRepository.deleteAll();
        reservaRepository.findByFechaCita(TEST_DATE).forEach(reservaRepository::delete);
        reservaRepository.findByFechaCita(TEST_DATE_SPECIAL).forEach(reservaRepository::delete);
        reservaRepository.findByFechaCita(TEST_DATE_CLOSED).forEach(reservaRepository::delete);

        // Restaurar configuración principal estándar
        ConfiguracionAgenda config = new ConfiguracionAgenda();
        config.setId("principal");
        config.setDiasLaborales(List.of(0, 2, 3, 4, 5, 6)); // Martes a Domingo (Lunes cerrado)
        config.setHorarios(List.of("08:00 AM", "09:30 AM", "11:00 AM", "01:30 PM", "03:00 PM", "04:30 PM", "06:00 PM"));
        configuracionAgendaRepository.save(config);
    }

    @Test
    @DisplayName("1. Obtener horario semanal por defecto vía GET /api/agenda")
    void test1_ObtenerHorarioSemanalPorDefecto() throws Exception {
        mockMvc.perform(get("/api/agenda"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("principal"))
                .andExpect(jsonPath("$.diasLaborales").isArray())
                .andExpect(jsonPath("$.horarios").isArray());
    }

    @Test
    @DisplayName("2. Actualizar horario semanal con autenticación ADMIN")
    void test2_ActualizarHorarioSemanalConAdmin() throws Exception {
        ConfiguracionAgenda nuevaConfig = new ConfiguracionAgenda();
        nuevaConfig.setDiasLaborales(List.of(1, 2, 3, 4, 5)); // Lunes a Viernes
        nuevaConfig.setHorarios(List.of("09:00 AM", "11:00 AM", "02:00 PM", "04:00 PM"));

        mockMvc.perform(put("/api/agenda")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevaConfig)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diasLaborales.length()").value(5))
                .andExpect(jsonPath("$.horarios.length()").value(4));

        ConfiguracionAgenda persistida = configuracionAgendaRepository.findById("principal").orElseThrow();
        assertEquals(5, persistida.getDiasLaborales().size());
        assertEquals(4, persistida.getHorarios().size());
    }

    @Test
    @DisplayName("3. Actualizar horario semanal sin rol ADMIN es rechazado con 401/403")
    void test3_ActualizarHorarioSemanalSinAdminRechazado() throws Exception {
        ConfiguracionAgenda nuevaConfig = new ConfiguracionAgenda();
        nuevaConfig.setDiasLaborales(List.of(1, 2, 3));
        nuevaConfig.setHorarios(List.of("08:00 AM"));

        mockMvc.perform(put("/api/agenda")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevaConfig)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("4. Validaciones de horario semanal (días vacíos o turnos vacíos retornan 400)")
    void test4_ActualizarHorarioSemanalValidacionesInvalido() throws Exception {
        ConfiguracionAgenda configInvalida = new ConfiguracionAgenda();
        configInvalida.setDiasLaborales(Collections.emptyList());
        configInvalida.setHorarios(List.of("08:00 AM"));

        mockMvc.perform(put("/api/agenda")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(configInvalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Selecciona al menos un día laboral válido (0 a 6)."));
    }

    @Test
    @DisplayName("5. Crear excepción de día cerrado para una fecha específica")
    void test5_CrearExcepcionDiaCerrado() throws Exception {
        Map<String, Object> payload = Map.of(
                "fecha", TEST_DATE_CLOSED.toString(),
                "tipo", "CERRADO",
                "motivo", "Festivo Nacional"
        );

        mockMvc.perform(post("/api/agenda/excepciones")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("CERRADO"))
                .andExpect(jsonPath("$.motivo").value("Festivo Nacional"));

        assertTrue(excepcionAgendaRepository.existsByFecha(TEST_DATE_CLOSED));
    }

    @Test
    @DisplayName("6. Disponibilidad pública refleja día cerrado devolviendo todos los horarios bloqueados")
    void test6_DisponibilidadReflejaDiaCerrado() throws Exception {
        ExcepcionAgenda cerrada = new ExcepcionAgenda(TEST_DATE_CLOSED, "CERRADO", Collections.emptyList(), "Mantenimiento", "admin");
        excepcionAgendaRepository.save(cerrada);

        mockMvc.perform(get("/api/reservas/disponibilidad")
                        .param("fecha", TEST_DATE_CLOSED.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7)); // Todos los 7 slots bloqueados
    }

    @Test
    @DisplayName("7. Crear excepción de horario especial con turnos reducidos")
    void test7_CrearExcepcionHorarioEspecial() throws Exception {
        Map<String, Object> payload = Map.of(
                "fecha", TEST_DATE_SPECIAL.toString(),
                "tipo", "HORARIO_ESPECIAL",
                "horarios", List.of("10:00 AM", "12:00 PM", "02:00 PM"),
                "motivo", "Sábado extendido especial"
        );

        mockMvc.perform(post("/api/agenda/excepciones")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("HORARIO_ESPECIAL"))
                .andExpect(jsonPath("$.horarios.length()").value(3));
    }

    @Test
    @DisplayName("8. Bloqueo de horario individual vía POST /api/reservas/bloqueos")
    void test8_BloqueoHorarioIndividual() throws Exception {
        BloqueoHorario bloqueo = new BloqueoHorario(TEST_DATE, "08:00 AM", "Reunión privada", "admin");

        mockMvc.perform(post("/api/reservas/bloqueos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bloqueo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.horaCita").value("08:00 AM"));

        // Comprobar que en disponibilidad pública aparece como ocupado
        mockMvc.perform(get("/api/reservas/disponibilidad")
                        .param("fecha", TEST_DATE.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("08:00 AM"));
    }

    @Test
    @DisplayName("9. Bloqueo de rango de horarios vía POST /api/reservas/bloqueos/rango")
    void test9_BloqueoRangoHorarios() throws Exception {
        Map<String, Object> payload = Map.of(
                "fecha", TEST_DATE.toString(),
                "horas", List.of("09:30 AM", "11:00 AM", "01:30 PM"),
                "motivo", "Tarde de capacitación"
        );

        mockMvc.perform(post("/api/reservas/bloqueos/rango")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bloqueados").value(3));

        assertEquals(3, bloqueoHorarioRepository.findByFechaCita(TEST_DATE).size());
    }

    @Test
    @DisplayName("10. Detección de conflicto: Bloquear rango con reserva activa devuelve 409 y lista de afectadas")
    void test10_DeteccionConflictoBloqueoConReservaExistente() throws Exception {
        // Crear una reserva activa en TEST_DATE a las 11:00 AM
        Reserva activa = new Reserva();
        activa.setCodigoReserva("ISV-TEST1");
        activa.setNombreCliente("Ana Gómez");
        activa.setTelefono("3001112233");
        activa.setFechaCita(TEST_DATE);
        activa.setHoraCita("11:00 AM");
        activa.setEstado("Confirmado");
        activa.setArchivada(false);
        reservaRepository.save(activa);

        Map<String, Object> payload = Map.of(
                "fecha", TEST_DATE.toString(),
                "horas", List.of("09:30 AM", "11:00 AM"),
                "motivo", "Cierre anticipado",
                "forzar", false
        );

        mockMvc.perform(post("/api/reservas/bloqueos/rango")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.conflicto").value(true))
                .andExpect(jsonPath("$.reservasAfectadas.length()").value(1))
                .andExpect(jsonPath("$.reservasAfectadas[0].codigo").value("ISV-TEST1"));
    }

    @Test
    @DisplayName("11. Invariante Crítica: Confirmación forzada de bloqueo NUNCA cancela ni elimina reservas")
    void test11_ConfirmacionForzadaPreservaReservaExistente() throws Exception {
        Reserva activa = new Reserva();
        activa.setCodigoReserva("ISV-TEST2");
        activa.setNombreCliente("Carlos Ruiz");
        activa.setTelefono("3004445566");
        activa.setFechaCita(TEST_DATE);
        activa.setHoraCita("11:00 AM");
        activa.setEstado("Confirmado");
        activa.setArchivada(false);
        reservaRepository.save(activa);

        Map<String, Object> payload = Map.of(
                "fecha", TEST_DATE.toString(),
                "horas", List.of("09:30 AM", "11:00 AM"),
                "motivo", "Cierre anticipado",
                "forzar", true
        );

        mockMvc.perform(post("/api/reservas/bloqueos/rango")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());

        // Verificar que la reserva activa SIGUE EXISTIENDO y con su estado INTACTO
        Reserva reservaDespues = reservaRepository.findByCodigoReservaAndTelefono("ISV-TEST2", "3004445566").orElseThrow();
        assertEquals("Confirmado", reservaDespues.getEstado());
        assertFalse(Boolean.TRUE.equals(reservaDespues.getArchivada()));
    }

    @Test
    @DisplayName("12. Detección de conflicto al cerrar día que contiene reservas existentes")
    void test12_DeteccionConflictoExcepcionDiaCerradoConReserva() throws Exception {
        Reserva activa = new Reserva();
        activa.setCodigoReserva("ISV-TEST3");
        activa.setNombreCliente("Laura Mora");
        activa.setTelefono("3007778899");
        activa.setFechaCita(TEST_DATE_CLOSED);
        activa.setHoraCita("03:00 PM");
        activa.setEstado("Confirmado");
        activa.setArchivada(false);
        reservaRepository.save(activa);

        Map<String, Object> payload = Map.of(
                "fecha", TEST_DATE_CLOSED.toString(),
                "tipo", "CERRADO",
                "motivo", "Cierre imprevisto",
                "forzar", false
        );

        mockMvc.perform(post("/api/agenda/excepciones")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.conflicto").value(true))
                .andExpect(jsonPath("$.reservasAfectadas[0].codigo").value("ISV-TEST3"));
    }

    @Test
    @DisplayName("13. Eliminar excepción de agenda restaura el horario habitual")
    void test13_EliminarExcepcionRestauraHorarioHabitual() throws Exception {
        ExcepcionAgenda ex = excepcionAgendaRepository.save(new ExcepcionAgenda(TEST_DATE, "CERRADO", null, "Temp", "admin"));

        mockMvc.perform(delete("/api/agenda/excepciones/" + ex.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        assertFalse(excepcionAgendaRepository.existsByFecha(TEST_DATE));
    }

    @Test
    @DisplayName("14. Seguridad: Endpoints de excepciones y bloqueos requieren ADMIN")
    void test14_SeguridadEndpointsBloqueosYExcepciones() throws Exception {
        mockMvc.perform(post("/api/agenda/excepciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/reservas/bloqueos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/reservas/bloqueos/rango")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/reservas/bloqueos")
                        .param("fecha", TEST_DATE.toString())
                        .param("hora", "08:00 AM"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("15. Prevención de duplicados en bloqueo manual de horarios")
    void test15_PrevencionDuplicadosEnBloqueo() throws Exception {
        BloqueoHorario b1 = new BloqueoHorario(TEST_DATE, "08:00 AM", "Bloqueo inicial", "admin");
        mockMvc.perform(post("/api/reservas/bloqueos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(b1)))
                .andExpect(status().isCreated());

        // Intentar bloquear el mismo slot nuevamente debe retornar 409 Conflicto amigable
        mockMvc.perform(post("/api/reservas/bloqueos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(b1)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("16. Cliente no puede reservar un horario bloqueado manualmente")
    void test16_ReservaNoPuedeTomarSlotBloqueado() throws Exception {
        bloqueoHorarioRepository.save(new BloqueoHorario(TEST_DATE, "08:00 AM", "Bloqueado", "admin"));

        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setNombreCliente("Pedro Martínez");
        nuevaReserva.setTelefono("3009990011");
        nuevaReserva.setCiudad("Cartagena");
        nuevaReserva.setFechaCita(TEST_DATE);
        nuevaReserva.setHoraCita("08:00 AM");
        nuevaReserva.setMedioPago("TRANSFERENCIA");
        nuevaReserva.setSubtotal(50000.0);
        nuevaReserva.setAnticipo(12500.0);
        nuevaReserva.setSaldo(37500.0);

        mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevaReserva)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Ese horario acaba de ser reservado. Elige otro turno."));
    }

    @Test
    @DisplayName("17. Persistencia y Fórmula de Disponibilidad Real")
    void test17_PersistenciaYFormulaPrioridad() throws Exception {
        // 1. Reserva activa en 08:00 AM
        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-P1");
        r.setNombreCliente("Cliente 1");
        r.setTelefono("3001234567");
        r.setFechaCita(TEST_DATE);
        r.setHoraCita("08:00 AM");
        r.setEstado("Confirmado");
        r.setArchivada(false);
        reservaRepository.save(r);

        // 2. Bloqueo manual en 09:30 AM
        bloqueoHorarioRepository.save(new BloqueoHorario(TEST_DATE, "09:30 AM", "Mantenimiento", "admin"));

        // 3. Consultar disponibilidad pública
        mockMvc.perform(get("/api/reservas/disponibilidad")
                        .param("fecha", TEST_DATE.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("08:00 AM"))
                .andExpect(jsonPath("$[1]").value("09:30 AM"));
    }
}
