package com.isivi.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isivi.app.model.Administrador;
import com.isivi.app.model.ConfiguracionAgenda;
import com.isivi.app.model.ConfiguracionNegocio;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.AdministradorRepository;
import com.isivi.app.repository.ConfiguracionAgendaRepository;
import com.isivi.app.repository.ConfiguracionNegocioRepository;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.security.JwtService;
import com.isivi.app.util.HorarioUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BusinessConfigurationAndFreeMinuteSlotsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConfiguracionNegocioRepository configuracionNegocioRepository;

    @Autowired
    private ConfiguracionAgendaRepository configuracionAgendaRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String adminToken;

    @BeforeEach
    void setUp() {
        administradorRepository.findAllByUsuario("admin_test_config").forEach(administradorRepository::delete);
        administradorRepository.save(new Administrador("admin_test_config", passwordEncoder.encode("1234")));
        adminToken = jwtService.generarToken("admin_test_config");

        configuracionNegocioRepository.deleteAll();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        configuracionNegocioRepository.deleteAll();
        configuracionAgendaRepository.deleteAll();
        ConfiguracionAgenda defaultConfig = new ConfiguracionAgenda();
        defaultConfig.setId("principal");
        defaultConfig.setDiasLaborales(List.of(0, 2, 3, 4, 5, 6));
        defaultConfig.setHorarios(List.of("08:00 AM", "09:30 AM", "11:00 AM", "01:30 PM", "03:00 PM", "04:30 PM", "06:00 PM"));
        configuracionAgendaRepository.save(defaultConfig);
    }

    @Nested
    @DisplayName("1. Pruebas de Configuración del Negocio")
    class BusinessConfigTests {

        @Test
        @DisplayName("Entidad ConfiguracionNegocio tiene valores por defecto correctos")
        void testDefaultValues() {
            ConfiguracionNegocio cfg = new ConfiguracionNegocio();
            assertThat(cfg.getId()).isEqualTo("principal");
            assertThat(cfg.getCiudad()).isEqualTo("Cartagena");
            assertThat(cfg.getPais()).isEqualTo("Colombia");
            assertThat(cfg.getDiasAtencion()).isEqualTo("Mar - Sáb");
            assertThat(cfg.getHoraApertura()).isEqualTo("08:00");
            assertThat(cfg.getHoraCierre()).isEqualTo("19:00");
            assertThat(cfg.getTelefonoMayorista()).isEqualTo("+57 300 962 3174");
            assertThat(cfg.getUbicacionFormateada()).isEqualTo("Cartagena, Colombia");
            assertThat(cfg.getHorarioFormateado()).isEqualTo("Mar - Sáb: 8:00 AM - 7:00 PM");
        }

        @Test
        @DisplayName("GET /api/configuracion público retorna defaults cuando no existe registro en BD")
        void testGetPublicConfigDefaults() throws Exception {
            mockMvc.perform(get("/api/configuracion"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ciudad").value("Cartagena"))
                    .andExpect(jsonPath("$.pais").value("Colombia"))
                    .andExpect(jsonPath("$.diasAtencion").value("Mar - Sáb"))
                    .andExpect(jsonPath("$.horaApertura").value("08:00"))
                    .andExpect(jsonPath("$.horaCierre").value("19:00"))
                    .andExpect(jsonPath("$.telefonoMayorista").value("+57 300 962 3174"));
        }

        @Test
        @DisplayName("PUT /api/configuracion requiere rol ADMIN (401/403 sin auth)")
        void testPutConfigUnauthorized() throws Exception {
            ConfiguracionNegocio payload = new ConfiguracionNegocio("Barranquilla", "Colombia", "Lun - Sáb", "09:00", "20:00", "+57 300 123 4567");

            mockMvc.perform(put("/api/configuracion")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("PUT /api/configuracion permite actualizar y persistir la configuración con token ADMIN")
        void testPutConfigAsAdmin() throws Exception {
            ConfiguracionNegocio payload = new ConfiguracionNegocio("Barranquilla", "Colombia", "Lun - Sáb", "09:00", "20:00", "+57 300 123 4567");

            mockMvc.perform(put("/api/configuracion")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("principal"))
                    .andExpect(jsonPath("$.ciudad").value("Barranquilla"))
                    .andExpect(jsonPath("$.pais").value("Colombia"))
                    .andExpect(jsonPath("$.diasAtencion").value("Lun - Sáb"))
                    .andExpect(jsonPath("$.horaApertura").value("09:00"))
                    .andExpect(jsonPath("$.horaCierre").value("20:00"))
                    .andExpect(jsonPath("$.telefonoMayorista").value("+57 300 123 4567"));

            ConfiguracionNegocio guardada = configuracionNegocioRepository.findById("principal").orElse(null);
            assertThat(guardada).isNotNull();
            assertThat(guardada.getCiudad()).isEqualTo("Barranquilla");
            assertThat(guardada.getUbicacionFormateada()).isEqualTo("Barranquilla, Colombia");
            assertThat(guardada.getHorarioFormateado()).isEqualTo("Lun - Sáb: 9:00 AM - 8:00 PM");

            mockMvc.perform(get("/api/configuracion"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ciudad").value("Barranquilla"))
                    .andExpect(jsonPath("$.telefonoMayorista").value("+57 300 123 4567"));
        }

        @Test
        @DisplayName("PUT /api/configuracion valida campos obligatorios")
        void testPutConfigValidation() throws Exception {
            ConfiguracionNegocio invalida = new ConfiguracionNegocio("", "Colombia", "Mar - Sáb", "08:00", "19:00", "+57 300 962 3174");

            mockMvc.perform(put("/api/configuracion")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalida)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("La ciudad es obligatoria."));
        }
    }

    @Nested
    @DisplayName("2. Pruebas de Turnos con Minutos Libres (00-59)")
    class FreeMinuteSlotsTests {

        @Test
        @DisplayName("HorarioUtil.parseTimeSlot parsea cualquier minuto arbitrario correctamente")
        void testParseArbitraryMinutes() {
            LocalTime t1 = HorarioUtil.parseTimeSlot("01:01 AM");
            assertThat(t1).isEqualTo(LocalTime.of(1, 1));

            LocalTime t2 = HorarioUtil.parseTimeSlot("01:02 AM");
            assertThat(t2).isEqualTo(LocalTime.of(1, 2));

            LocalTime t3 = HorarioUtil.parseTimeSlot("08:07 AM");
            assertThat(t3).isEqualTo(LocalTime.of(8, 7));

            LocalTime t4 = HorarioUtil.parseTimeSlot("10:17 AM");
            assertThat(t4).isEqualTo(LocalTime.of(10, 17));

            LocalTime t5 = HorarioUtil.parseTimeSlot("10:41 AM");
            assertThat(t5).isEqualTo(LocalTime.of(10, 41));

            LocalTime t6 = HorarioUtil.parseTimeSlot("07:58 PM");
            assertThat(t6).isEqualTo(LocalTime.of(19, 58));

            LocalTime t7 = HorarioUtil.parseTimeSlot("19:58");
            assertThat(t7).isEqualTo(LocalTime.of(19, 58));

            LocalTime t8 = HorarioUtil.parseTimeSlot("11:59 PM");
            assertThat(t8).isEqualTo(LocalTime.of(23, 59));

            LocalTime t9 = HorarioUtil.parseTimeSlot("12:00 AM");
            assertThat(t9).isEqualTo(LocalTime.of(0, 0));
        }

        @Test
        @DisplayName("HorarioUtil.calculateEndTime calcula duración exacta a partir de minutos libres")
        void testCalculateEndTimeWithArbitraryMinutes() {
            // Turno 10:17 AM con servicio de 60 min -> 11:17 AM
            LocalTime end60 = HorarioUtil.calculateEndTime("10:17 AM", 60);
            assertThat(end60).isEqualTo(LocalTime.of(11, 17));

            // Turno 10:17 AM con servicio de 90 min -> 11:47 AM
            LocalTime end90 = HorarioUtil.calculateEndTime("10:17 AM", 90);
            assertThat(end90).isEqualTo(LocalTime.of(11, 47));

            // Turno 08:07 AM con servicio de 45 min -> 08:52 AM
            LocalTime end45 = HorarioUtil.calculateEndTime("08:07 AM", 45);
            assertThat(end45).isEqualTo(LocalTime.of(8, 52));

            // Turno 10:41 AM con servicio de 30 min -> 11:11 AM
            LocalTime end30 = HorarioUtil.calculateEndTime("10:41 AM", 30);
            assertThat(end30).isEqualTo(LocalTime.of(11, 11));
        }

        @Test
        @DisplayName("HorarioUtil detecta citas completadas o en curso con minutos exactos")
        void testIsAppointmentCompletedAndInProgress() {
            // Fijar reloj a las 11:00 AM del 2026-08-20 (Bogotá UTC-5 -> 16:00 UTC)
            Clock clock = Clock.fixed(Instant.parse("2026-08-20T16:00:00Z"), ZoneId.of("America/Bogota"));
            LocalDate fecha = LocalDate.of(2026, 8, 20);

            // Cita de 10:17 AM a 10:47 AM (30 min) -> Debe estar completada a las 11:00 AM
            boolean completed = HorarioUtil.isAppointmentCompleted(fecha, "10:17 AM", 30, clock);
            assertThat(completed).isTrue();

            // Cita de 10:17 AM a 11:17 AM (60 min) -> Debe estar en progreso a las 11:00 AM
            boolean inProgress = HorarioUtil.isAppointmentInProgress(fecha, "10:17 AM", 60, clock);
            assertThat(inProgress).isTrue();

            // Cita futura de 11:15 AM a 12:15 PM -> No completada ni en progreso a las 11:00 AM
            boolean futureCompleted = HorarioUtil.isAppointmentCompleted(fecha, "11:15 AM", 60, clock);
            boolean futureInProgress = HorarioUtil.isAppointmentInProgress(fecha, "11:15 AM", 60, clock);
            assertThat(futureCompleted).isFalse();
            assertThat(futureInProgress).isFalse();
        }

        @Test
        @DisplayName("Guardar y consultar configuración de agenda con turnos de minutos arbitrarios")
        void testAgendaWithFreeMinutes() throws Exception {
            ConfiguracionAgenda config = new ConfiguracionAgenda();
            config.setId("principal");
            config.setDiasLaborales(List.of(1, 2, 3, 4, 5, 6));
            config.setHorarios(List.of("01:01 AM", "01:02 AM", "08:07 AM", "10:17 AM", "10:41 AM", "07:58 PM"));

            mockMvc.perform(put("/api/agenda")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(config)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.horarios[0]").value("01:01 AM"))
                    .andExpect(jsonPath("$.horarios[1]").value("01:02 AM"))
                    .andExpect(jsonPath("$.horarios[2]").value("08:07 AM"))
                    .andExpect(jsonPath("$.horarios[3]").value("10:17 AM"))
                    .andExpect(jsonPath("$.horarios[4]").value("10:41 AM"))
                    .andExpect(jsonPath("$.horarios[5]").value("07:58 PM"));

            mockMvc.perform(get("/api/agenda"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.horarios[3]").value("10:17 AM"));
        }

        @Test
        @DisplayName("Persistencia de Reserva con minuto libre exacto (10:17 AM)")
        void testReservaPersistenceWithFreeMinutes() {
            Reserva r = new Reserva();
            r.setCodigoReserva("ISV-MIN-1017");
            r.setNombreCliente("Cliente Minuto Exacto");
            r.setTelefono("3001234567");
            r.setFechaCita(LocalDate.of(2026, 8, 25));
            r.setHoraCita("10:17 AM");
            r.setSubtotal(120000.0);
            r.setAnticipo(30000.0);
            r.setSaldo(90000.0);
            r.setItems(List.of("Balayage"));
            r.setEstado("Confirmado");

            Reserva guardada = reservaRepository.save(r);
            assertThat(guardada.getId()).isNotNull();
            assertThat(guardada.getHoraCita()).isEqualTo("10:17 AM");

            Reserva recuperada = reservaRepository.findById(guardada.getId()).orElse(null);
            assertThat(recuperada).isNotNull();
            assertThat(recuperada.getHoraCita()).isEqualTo("10:17 AM");

            // Limpieza
            reservaRepository.deleteById(guardada.getId());
        }
    }
}
