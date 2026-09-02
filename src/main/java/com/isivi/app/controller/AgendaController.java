package com.isivi.app.controller;

import com.isivi.app.model.ConfiguracionAgenda;
import com.isivi.app.model.ExcepcionAgenda;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.ConfiguracionAgendaRepository;
import com.isivi.app.repository.ExcepcionAgendaRepository;
import com.isivi.app.repository.ReservaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/api/agenda")
@CrossOrigin(origins = "*")
public class AgendaController {
    private static final List<Integer> DIAS_POR_DEFECTO = List.of(0, 2, 3, 4, 5, 6);
    private static final List<String> HORARIOS_POR_DEFECTO = List.of("08:00 AM", "09:30 AM", "11:00 AM", "01:30 PM", "03:00 PM", "04:30 PM", "06:00 PM");

    private final ConfiguracionAgendaRepository configuracionAgendaRepository;
    private final ExcepcionAgendaRepository excepcionAgendaRepository;
    private final ReservaRepository reservaRepository;
    private final com.isivi.app.repository.BloqueoRecurrenteRepository bloqueoRecurrenteRepository;

    public AgendaController(ConfiguracionAgendaRepository configuracionAgendaRepository,
                            ExcepcionAgendaRepository excepcionAgendaRepository,
                            ReservaRepository reservaRepository,
                            com.isivi.app.repository.BloqueoRecurrenteRepository bloqueoRecurrenteRepository) {
        this.configuracionAgendaRepository = configuracionAgendaRepository;
        this.excepcionAgendaRepository = excepcionAgendaRepository;
        this.reservaRepository = reservaRepository;
        this.bloqueoRecurrenteRepository = bloqueoRecurrenteRepository;
    }


    @GetMapping
    public ConfiguracionAgenda obtener() {
        ConfiguracionAgenda config = configuracionAgendaRepository.findById("principal").orElseGet(this::configuracionPorDefecto);
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Bogota"));
        List<ExcepcionAgenda> excepciones = excepcionAgendaRepository.findByFechaGreaterThanEqualOrderByFechaAsc(hoy);
        config.setExcepciones(excepciones);
        return config;
    }

    @PutMapping
    public ResponseEntity<?> actualizar(@RequestBody ConfiguracionAgenda configuracion) {
        if (configuracion.getDiasLaborales() == null || configuracion.getDiasLaborales().isEmpty()
                || configuracion.getDiasLaborales().stream().anyMatch(dia -> dia == null || dia < 0 || dia > 6))
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Selecciona al menos un día laboral válido (0 a 6)."));
        if (configuracion.getHorarios() == null || configuracion.getHorarios().isEmpty()
                || configuracion.getHorarios().stream().anyMatch(hora -> hora == null || hora.isBlank()))
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Agrega al menos un horario válido."));

        configuracion.setId("principal");
        configuracion.setDiasLaborales(configuracion.getDiasLaborales().stream().distinct().sorted().toList());
        configuracion.setHorarios(configuracion.getHorarios().stream().distinct().toList());
        ConfiguracionAgenda guardada = configuracionAgendaRepository.save(configuracion);

        LocalDate hoy = LocalDate.now(ZoneId.of("America/Bogota"));
        guardada.setExcepciones(excepcionAgendaRepository.findByFechaGreaterThanEqualOrderByFechaAsc(hoy));
        return ResponseEntity.ok(guardada);
    }

    @GetMapping("/excepciones")
    public List<ExcepcionAgenda> listarExcepciones() {
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Bogota"));
        return excepcionAgendaRepository.findByFechaGreaterThanEqualOrderByFechaAsc(hoy);
    }

    @PostMapping("/excepciones")
    public synchronized ResponseEntity<?> guardarExcepcion(@RequestBody Map<String, Object> payload, Authentication auth) {
        String fechaStr = (String) payload.get("fecha");
        String tipo = (String) payload.get("tipo");
        String motivo = (String) payload.getOrDefault("motivo", "");
        Boolean forzar = (Boolean) payload.getOrDefault("forzar", false);

        if (fechaStr == null || fechaStr.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Debes especificar una fecha válida."));
        }
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(fechaStr);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Formato de fecha inválido. Usa YYYY-MM-DD."));
        }

        if (tipo == null || (!tipo.equals("CERRADO") && !tipo.equals("HORARIO_ESPECIAL"))) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Tipo de excepción no válido. Debe ser CERRADO o HORARIO_ESPECIAL."));
        }

        @SuppressWarnings("unchecked")
        List<String> horarios = (List<String>) payload.get("horarios");
        if (tipo.equals("HORARIO_ESPECIAL") && (horarios == null || horarios.isEmpty())) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Para un horario especial debes definir al menos un horario de atención."));
        }

        // Detección de conflicto con reservas existentes
        List<Reserva> reservasFecha = reservaRepository.findByFechaCita(fecha).stream()
                .filter(r -> !Boolean.TRUE.equals(r.getArchivada()))
                .filter(r -> !"Cancelada".equalsIgnoreCase(r.getEstado()) && !"Denegada".equalsIgnoreCase(r.getEstado()))
                .toList();

        List<Map<String, Object>> reservasAfectadas = new ArrayList<>();
        if (tipo.equals("CERRADO") && !reservasFecha.isEmpty()) {
            for (Reserva r : reservasFecha) {
                reservasAfectadas.add(Map.of(
                        "id", r.getId() != null ? r.getId() : "",
                        "codigo", r.getCodigoReserva() != null ? r.getCodigoReserva() : "",
                        "cliente", r.getNombreCliente() != null ? r.getNombreCliente() : "",
                        "telefono", r.getTelefono() != null ? r.getTelefono() : "",
                        "hora", r.getHoraCita() != null ? r.getHoraCita() : "",
                        "estado", r.getEstado() != null ? r.getEstado() : ""
                ));
            }
        } else if (tipo.equals("HORARIO_ESPECIAL") && !reservasFecha.isEmpty() && horarios != null) {
            for (Reserva r : reservasFecha) {
                if (r.getHoraCita() != null && !horarios.contains(r.getHoraCita())) {
                    reservasAfectadas.add(Map.of(
                            "id", r.getId() != null ? r.getId() : "",
                            "codigo", r.getCodigoReserva() != null ? r.getCodigoReserva() : "",
                            "cliente", r.getNombreCliente() != null ? r.getNombreCliente() : "",
                            "telefono", r.getTelefono() != null ? r.getTelefono() : "",
                            "hora", r.getHoraCita() != null ? r.getHoraCita() : "",
                            "estado", r.getEstado() != null ? r.getEstado() : ""
                    ));
                }
            }
        }

        if (!reservasAfectadas.isEmpty() && !Boolean.TRUE.equals(forzar)) {
            Map<String, Object> conflicto = new HashMap<>();
            conflicto.put("conflicto", true);
            conflicto.put("error", "CONFLICTO_RESERVAS");
            conflicto.put("mensaje", "El día " + fecha + " tiene " + reservasAfectadas.size() + " citas activas. No es posible aplicar la excepción de tipo " + tipo + " sin gestionar previamente esas reservas.");
            conflicto.put("reservasAfectadas", reservasAfectadas);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(conflicto);
        }

        String adminUser = auth != null ? auth.getName() : "admin";
        ExcepcionAgenda excepcion = excepcionAgendaRepository.findByFecha(fecha).orElse(new ExcepcionAgenda());
        excepcion.setFecha(fecha);
        excepcion.setTipo(tipo);
        excepcion.setHorarios(tipo.equals("HORARIO_ESPECIAL") ? horarios : Collections.emptyList());
        excepcion.setMotivo(motivo);
        excepcion.setCreadoPor(adminUser);

        ExcepcionAgenda guardada = excepcionAgendaRepository.save(excepcion);
        return ResponseEntity.ok(guardada);
    }

    @DeleteMapping("/excepciones/{id}")
    public ResponseEntity<Void> eliminarExcepcion(@PathVariable String id) {
        return excepcionAgendaRepository.findById(id).map(excepcion -> {
            excepcionAgendaRepository.delete(excepcion);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    // ============ BLOQUEOS RECURRENTES ============
    @GetMapping("/bloqueos-recurrentes")
    public List<com.isivi.app.model.BloqueoRecurrente> listarBloqueosRecurrentes() {
        return bloqueoRecurrenteRepository.findAll();
    }

    @PostMapping("/bloqueos-recurrentes")
    public synchronized ResponseEntity<?> guardarBloqueoRecurrente(@RequestBody Map<String, Object> payload, Authentication auth) {
        Integer diaSemana = payload.get("diaSemana") instanceof Number ? ((Number) payload.get("diaSemana")).intValue() : null;
        @SuppressWarnings("unchecked")
        List<String> horarios = (List<String>) payload.get("horarios");
        String fechaInicioStr = (String) payload.get("fechaInicio");
        String fechaFinStr = (String) payload.get("fechaFin");
        String motivo = (String) payload.getOrDefault("motivo", "");
        Boolean forzar = (Boolean) payload.getOrDefault("forzar", false);

        if (diaSemana == null || diaSemana < 0 || diaSemana > 6) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Debes especificar un día de la semana válido (0 a 6)."));
        }
        if (horarios == null || horarios.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Debes seleccionar al menos un horario para bloquear."));
        }

        LocalDate fechaInicio = (fechaInicioStr != null && !fechaInicioStr.isBlank()) ? LocalDate.parse(fechaInicioStr) : LocalDate.now(ZoneId.of("America/Bogota"));
        LocalDate fechaFin = (fechaFinStr != null && !fechaFinStr.isBlank()) ? LocalDate.parse(fechaFinStr) : null;

        // Comprobar conflicto con reservas existentes en los próximos 60 días
        LocalDate limiteEval = fechaFin != null ? fechaFin : fechaInicio.plusDays(60);
        List<Map<String, Object>> reservasAfectadas = new ArrayList<>();

        for (LocalDate cur = fechaInicio; !cur.isAfter(limiteEval); cur = cur.plusDays(1)) {
            if (cur.getDayOfWeek().getValue() % 7 == diaSemana) {
                List<Reserva> match = reservaRepository.findByFechaCita(cur).stream()
                        .filter(r -> !Boolean.TRUE.equals(r.getArchivada()))
                        .filter(r -> "Confirmado".equalsIgnoreCase(r.getEstado()) || "Pendiente Comprobante".equalsIgnoreCase(r.getEstado()) || "Pendiente Pago".equalsIgnoreCase(r.getEstado()))
                        .filter(r -> horarios.contains(r.getHoraCita()))
                        .toList();
                for (Reserva r : match) {
                    reservasAfectadas.add(Map.of(
                            "id", r.getId() != null ? r.getId() : "",
                            "codigo", r.getCodigoReserva() != null ? r.getCodigoReserva() : "",
                            "cliente", r.getNombreCliente() != null ? r.getNombreCliente() : "",
                            "fecha", r.getFechaCita() != null ? r.getFechaCita().toString() : "",
                            "hora", r.getHoraCita() != null ? r.getHoraCita() : "",
                            "estado", r.getEstado() != null ? r.getEstado() : ""
                    ));
                }
            }
        }

        if (!reservasAfectadas.isEmpty() && !Boolean.TRUE.equals(forzar)) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("conflicto", true);
            resp.put("error", "CONFLICTO_RESERVAS");
            resp.put("mensaje", "Existen " + reservasAfectadas.size() + " reserva(s) existente(s) en las fechas de esta recurrencia. No se cancelará ninguna reserva.");
            resp.put("reservasAfectadas", reservasAfectadas);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);
        }

        String adminUser = auth != null ? auth.getName() : "admin";
        com.isivi.app.model.BloqueoRecurrente recurrente = new com.isivi.app.model.BloqueoRecurrente(diaSemana, horarios, fechaInicio, fechaFin, motivo, adminUser);
        com.isivi.app.model.BloqueoRecurrente guardado = bloqueoRecurrenteRepository.save(recurrente);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/bloqueos-recurrentes/{id}")
    public synchronized ResponseEntity<?> actualizarBloqueoRecurrente(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        return bloqueoRecurrenteRepository.findById(id).map(b -> {
            if (payload.containsKey("activo")) {
                b.setActivo((Boolean) payload.get("activo"));
            }
            if (payload.containsKey("motivo")) {
                b.setMotivo((String) payload.get("motivo"));
            }
            if (payload.containsKey("horarios")) {
                @SuppressWarnings("unchecked")
                List<String> h = (List<String>) payload.get("horarios");
                b.setHorarios(h);
            }
            return ResponseEntity.ok(bloqueoRecurrenteRepository.save(b));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/bloqueos-recurrentes/{id}")
    public ResponseEntity<Void> eliminarBloqueoRecurrente(@PathVariable String id) {
        return bloqueoRecurrenteRepository.findById(id).map(b -> {
            bloqueoRecurrenteRepository.delete(b);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    private ConfiguracionAgenda configuracionPorDefecto() {
        ConfiguracionAgenda configuracion = new ConfiguracionAgenda();
        configuracion.setDiasLaborales(DIAS_POR_DEFECTO);
        configuracion.setHorarios(HORARIOS_POR_DEFECTO);
        return configuracion;
    }
}

