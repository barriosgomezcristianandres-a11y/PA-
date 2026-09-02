package com.isivi.app.controller;

import com.isivi.app.dto.GestionReservaRequest;
import com.isivi.app.exception.CancelacionNoPermitidaException;
import com.isivi.app.model.BloqueoHorario;
import com.isivi.app.model.BloqueoRecurrente;
import com.isivi.app.model.Reserva;
import com.isivi.app.model.ConfiguracionAgenda;
import com.isivi.app.model.ExcepcionAgenda;
import com.isivi.app.repository.BloqueoHorarioRepository;
import com.isivi.app.repository.BloqueoRecurrenteRepository;
import com.isivi.app.repository.ConfiguracionAgendaRepository;
import com.isivi.app.repository.ExcepcionAgendaRepository;
import com.isivi.app.repository.ReservaRepository;

import com.isivi.app.service.EmailNotificationService;
import com.isivi.app.service.ReservaService;
import com.isivi.app.service.WhatsAppNotificationService;
import com.isivi.app.util.HorarioUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = "*")
public class ReservaController {
    
    private boolean esAdministrador(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return false;
        if (auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) return false;
        return auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private final ReservaRepository reservaRepository;
    private final BloqueoHorarioRepository bloqueoHorarioRepository;
    private final BloqueoRecurrenteRepository bloqueoRecurrenteRepository;
    private final ConfiguracionAgendaRepository configuracionAgendaRepository;
    private final ExcepcionAgendaRepository excepcionAgendaRepository;
    private final ReservaService reservaService;
    private final WhatsAppNotificationService whatsapp;
    private final EmailNotificationService emailNotificationService;
    private final com.isivi.app.service.NotificacionAdminService notificacionAdminService;
    private Clock clock = Clock.system(HorarioUtil.ZONA_BOGOTA);

    public ReservaController(ReservaRepository reservaRepository, BloqueoHorarioRepository bloqueoHorarioRepository,
                             BloqueoRecurrenteRepository bloqueoRecurrenteRepository,
                             ConfiguracionAgendaRepository configuracionAgendaRepository,
                             ExcepcionAgendaRepository excepcionAgendaRepository,
                             ReservaService reservaService, WhatsAppNotificationService whatsapp,
                             @Autowired(required = false) EmailNotificationService emailNotificationService,
                             com.isivi.app.service.NotificacionAdminService notificacionAdminService) {
        this.reservaRepository = reservaRepository;
        this.bloqueoHorarioRepository = bloqueoHorarioRepository;
        this.bloqueoRecurrenteRepository = bloqueoRecurrenteRepository;
        this.configuracionAgendaRepository = configuracionAgendaRepository;
        this.excepcionAgendaRepository = excepcionAgendaRepository;
        this.reservaService = reservaService;
        this.whatsapp = whatsapp;
        this.emailNotificationService = emailNotificationService;
        this.notificacionAdminService = notificacionAdminService;
    }

    public void setClock(Clock clock) {
        this.clock = clock != null ? clock : Clock.system(HorarioUtil.ZONA_BOGOTA);
        if (this.reservaService != null) {
            this.reservaService.setClock(this.clock);
        }
    }

    @GetMapping
    public List<Reserva> listar(
            @RequestParam(required = false) String fecha,
            @RequestParam(defaultValue = "false") boolean historial,
            @RequestParam(required = false) String tipo) {
        archivarReservasVencidas();
        reservaService.expirarReservasVencidas();

        String tipoEfectivo = tipo;
        String fechaEfectiva = fecha;
        if (fecha != null && ("citas".equalsIgnoreCase(fecha) || "pedidos".equalsIgnoreCase(fecha) || "todos".equalsIgnoreCase(fecha))) {
            tipoEfectivo = fecha;
            fechaEfectiva = null;
        }

        final String tipoFinal = tipoEfectivo;
        final String fechaFinal = fechaEfectiva;

        return reservaRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .filter(reserva -> {
                    boolean esWompiRechazado = reserva.esPedidoPuro()
                            && "Pendiente Pago".equalsIgnoreCase(reserva.getEstado())
                            && List.of("RECHAZADO", "DECLINED", "VOIDED", "ERROR").contains(reserva.getEstadoPago() != null ? reserva.getEstadoPago().toUpperCase() : "");
                    if (historial) {
                        return esTerminal(reserva);
                    } else {
                        return !esTerminal(reserva) && !esWompiRechazado;
                    }
                })
                .filter(reserva -> {
                    if (tipoFinal == null || tipoFinal.isBlank() || "todos".equalsIgnoreCase(tipoFinal)) return true;
                    if ("citas".equalsIgnoreCase(tipoFinal)) return reserva.esCita();
                    if ("pedidos".equalsIgnoreCase(tipoFinal)) return reserva.esPedidoPuro();
                    return true;
                })
                .filter(reserva -> {
                    if (fechaFinal == null || fechaFinal.isBlank()) return true;
                    try {
                        return LocalDate.parse(fechaFinal).equals(reserva.getFechaCita());
                    } catch (Exception e) {
                        return true;
                    }
                })
                .map(reserva -> {
                    if (reserva.esCita() && ("Confirmado".equalsIgnoreCase(reserva.getEstado()) || "Confirmada".equalsIgnoreCase(reserva.getEstado())) && reservaService.citaFinalizada(reserva)) {
                        reserva.setEstado("Realizada");
                    }
                    return reserva;
                })
                .toList();
    }

    public List<Reserva> listar(String fecha, boolean historial) {
        return listar(fecha, historial, "todos");
    }



    private boolean esTerminal(Reserva r) {
        if (r == null) return false;
        if (Boolean.TRUE.equals(r.getArchivada())) return true;
        String st = r.getEstado();
        if ("Cancelada".equalsIgnoreCase(st)
                || "Cancelado".equalsIgnoreCase(st)
                || "Denegada".equalsIgnoreCase(st)
                || "Expirada".equalsIgnoreCase(st)
                || "Completada".equalsIgnoreCase(st)
                || "Realizada".equalsIgnoreCase(st)
                || "Entregado".equalsIgnoreCase(st)
                || "Entregada".equalsIgnoreCase(st)
                || "Recogido".equalsIgnoreCase(st)) {
            return true;
        }
        if (r.esCita() && ("Confirmado".equalsIgnoreCase(st) || "Confirmada".equalsIgnoreCase(st))) {
            return reservaService.citaFinalizada(r);
        }
        return false;
    }


    /** Solo expone las horas bloqueadas; no revela datos de otros clientes. */
    @GetMapping("/disponibilidad")
    public List<String> disponibilidad(@RequestParam String fecha) {
        archivarReservasVencidas();
        reservaService.expirarReservasVencidas();
        LocalDate fechaCita = LocalDate.parse(fecha);

        // 1. Si existe excepción de día CERRADO para esta fecha, bloquear todos los slots
        Optional<ExcepcionAgenda> excepcionOpt = excepcionAgendaRepository.findByFecha(fechaCita);
        if (excepcionOpt.isPresent() && "CERRADO".equalsIgnoreCase(excepcionOpt.get().getTipo())) {
            ConfiguracionAgenda cfg = configuracionAgendaRepository.findById("principal").orElse(null);
            return cfg != null && cfg.getHorarios() != null ? cfg.getHorarios() : List.of("08:00 AM", "09:30 AM", "11:00 AM", "01:30 PM", "03:00 PM", "04:30 PM", "06:00 PM");
        }

        // 2. Horas bloqueadas recurrentes aplicables a esta fecha
        List<String> horasRecurrentes = (excepcionOpt.isPresent() && "HORARIO_ESPECIAL".equalsIgnoreCase(excepcionOpt.get().getTipo()))
                ? List.of()
                : bloqueoRecurrenteRepository.findByActivoTrue().stream()
                        .filter(br -> br.aplicaAFecha(fechaCita))
                        .flatMap(br -> br.getHorarios().stream())
                        .toList();

        // 3. Unir horas ocupadas por reservas activas + bloqueos manuales + bloqueos recurrentes
        return java.util.stream.Stream.of(
                reservaRepository.findByFechaCita(fechaCita).stream().filter(reserva -> !Boolean.TRUE.equals(reserva.getArchivada())).filter(this::bloqueaAgenda).map(Reserva::getHoraCita),
                bloqueoHorarioRepository.findByFechaCita(fechaCita).stream().map(BloqueoHorario::getHoraCita),
                horasRecurrentes.stream())
                .flatMap(java.util.function.Function.identity())
                .filter(h -> h != null && !h.isBlank()).distinct().toList();
    }



    @GetMapping("/bloqueos")
    public List<BloqueoHorario> listarBloqueos(@RequestParam String fecha) {
        return bloqueoHorarioRepository.findByFechaCita(LocalDate.parse(fecha));
    }

    @PostMapping("/bloqueos")
    public synchronized ResponseEntity<?> bloquearHorario(@RequestBody BloqueoHorario bloqueo, Authentication auth) {
        if (bloqueo.getFechaCita() == null || bloqueo.getHoraCita() == null || bloqueo.getHoraCita().isBlank())
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Selecciona una fecha y hora válidas."));
        if (agendaOcupada(bloqueo.getFechaCita(), bloqueo.getHoraCita(), null))
            return conflicto("Ese horario ya está ocupado.");
        if (auth != null && auth.getName() != null) {
            bloqueo.setCreadoPor(auth.getName());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(bloqueoHorarioRepository.save(bloqueo));
    }

    @PostMapping("/bloqueos/rango")
    public synchronized ResponseEntity<?> bloquearRango(@RequestBody Map<String, Object> payload, Authentication auth) {
        String fechaStr = (String) payload.get("fecha");
        @SuppressWarnings("unchecked")
        List<String> horas = (List<String>) payload.get("horas");
        String motivo = (String) payload.getOrDefault("motivo", "");
        Boolean forzar = (Boolean) payload.getOrDefault("forzar", false);

        if (fechaStr == null || fechaStr.isBlank() || horas == null || horas.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Selecciona una fecha y al menos un horario para bloquear."));
        }
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(fechaStr);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Formato de fecha inválido."));
        }

        // Comprobar si hay reservas existentes en ese rango de horas
        List<Reserva> reservasAfectadas = reservaRepository.findByFechaCita(fecha).stream()
                .filter(r -> !Boolean.TRUE.equals(r.getArchivada()))
                .filter(this::bloqueaAgenda)
                .filter(r -> horas.contains(r.getHoraCita()))
                .toList();

        if (!reservasAfectadas.isEmpty() && !Boolean.TRUE.equals(forzar)) {
            List<Map<String, Object>> detalle = reservasAfectadas.stream().map(r -> Map.<String, Object>of(
                    "id", r.getId() != null ? r.getId() : "",
                    "codigo", r.getCodigoReserva() != null ? r.getCodigoReserva() : "",
                    "cliente", r.getNombreCliente() != null ? r.getNombreCliente() : "",
                    "hora", r.getHoraCita() != null ? r.getHoraCita() : "",
                    "estado", r.getEstado() != null ? r.getEstado() : ""
            )).toList();

            Map<String, Object> resp = new HashMap<>();
            resp.put("conflicto", true);
            resp.put("error", "CONFLICTO_RESERVAS");
            resp.put("mensaje", "El rango seleccionado contiene " + reservasAfectadas.size() + " reserva(s) existente(s). No se cancelará ninguna reserva.");
            resp.put("reservasAfectadas", detalle);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);
        }

        String adminUser = auth != null ? auth.getName() : "admin";
        List<BloqueoHorario> guardados = new ArrayList<>();
        for (String hora : horas) {
            if (!bloqueoHorarioRepository.existsByFechaCitaAndHoraCita(fecha, hora)) {
                BloqueoHorario b = new BloqueoHorario(fecha, hora, motivo, adminUser);
                guardados.add(bloqueoHorarioRepository.save(b));
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Bloqueo guardado exitosamente.",
                "bloqueados", guardados.size()
        ));
    }

    @DeleteMapping("/bloqueos")
    public ResponseEntity<Void> liberarHorario(@RequestParam String fecha, @RequestParam String hora) {
        return bloqueoHorarioRepository.findByFechaCitaAndHoraCita(LocalDate.parse(fecha), hora).map(bloqueo -> {
            bloqueoHorarioRepository.delete(bloqueo);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    private Reserva sanitizarParaPublico(Reserva r) {
        if (r == null) return null;
        Reserva s = new Reserva();
        s.setId(r.getId());
        s.setCodigoReserva(r.getCodigoReserva());
        s.setNombreCliente(r.getNombreCliente());
        s.setItems(r.getItems());
        s.setItemsInventario(r.getItemsInventario());
        s.setFechaCita(r.getFechaCita());
        s.setHoraCita(r.getHoraCita());
        s.setSubtotal(r.getSubtotal());
        s.setAnticipo(r.getAnticipo());
        s.setSaldo(r.getSaldo());
        s.setTipoEntrega(r.getTipoEntrega());
        s.setEstado(r.getEstado());
        s.setEstadoPedido(r.getEstadoPedido());
        s.setFechaCancelacion(r.getFechaCancelacion());
        s.setFechaSolicitudCancelacion(r.getFechaSolicitudCancelacion());
        s.setCanceladaPor(r.getCanceladaPor());
        s.setMotivoCancelacion(r.getMotivoCancelacion());
        s.setMotivoRechazoCancelacion(r.getMotivoRechazoCancelacion());
        s.setEstadoPrevioCancelacion(r.getEstadoPrevioCancelacion());
        s.setNotificacionCancelacionVista(r.getNotificacionCancelacionVista());
        s.setEmailConfirmacionEnviada(r.getEmailConfirmacionEnviada());
        s.setFechaEnvioConfirmacion(r.getFechaEnvioConfirmacion());
        s.setEmailErrorEnvio(r.getEmailErrorEnvio());
        s.setMedioPago(r.getMedioPago());
        s.setFechaExpiracionPago(r.getFechaExpiracionPago());
        return s;
    }

    @GetMapping("/consultar")
    public ResponseEntity<?> consultar(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String telefono,
            Authentication auth) {

        String valorBusqueda = query != null ? query.trim() : "";
        if (valorBusqueda.isBlank() && codigo != null && !codigo.isBlank()) {
            valorBusqueda = codigo.trim();
        }
        if (valorBusqueda.isBlank() && telefono != null && !telefono.isBlank()) {
            valorBusqueda = telefono.trim();
        }

        if (valorBusqueda.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Ingresa tu código de reserva o WhatsApp para consultar el estado de tu reserva."));
        }

        // Si se enviaron ambos parámetros explícitos (código + teléfono para compatibilidad histórica)
        if (codigo != null && !codigo.isBlank() && telefono != null && !telefono.isBlank()) {
            return reservaRepository.findByCodigoReservaAndTelefono(codigo.trim().toUpperCase(), normalizarTelefono(telefono))
                    .<ResponseEntity<?>>map(r -> esAdministrador(auth) ? ResponseEntity.ok(r) : ResponseEntity.ok(sanitizarParaPublico(r)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", "No encontramos una reserva con esos datos.")));
        }

        // Detección automática:
        // 1. Si parece código de reserva (contiene letras, guiones, o formato ISV-)
        if (valorBusqueda.matches("(?i).*[a-z-].*")) {
            return reservaRepository.findByCodigoReserva(valorBusqueda.toUpperCase())
                    .<ResponseEntity<?>>map(r -> esAdministrador(auth) ? ResponseEntity.ok(r) : ResponseEntity.ok(sanitizarParaPublico(r)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", "No encontramos una reserva con el código " + valorBusqueda.toUpperCase() + ".")));
        }

        // 2. Si es numérico (teléfono)
        String telDigitos = normalizarTelefono(valorBusqueda);
        if (telDigitos.startsWith("57") && telDigitos.length() == 12) {
            telDigitos = telDigitos.substring(2);
        }

        // Intentar primero como código por si el código fuera numérico
        Optional<Reserva> porCodigo = reservaRepository.findByCodigoReserva(valorBusqueda.toUpperCase());
        if (porCodigo.isPresent()) {
            return esAdministrador(auth) ? ResponseEntity.ok(porCodigo.get()) : ResponseEntity.ok(sanitizarParaPublico(porCodigo.get()));
        }

        // Buscar por teléfono
        List<Reserva> porTel = reservaRepository.findByTelefonoOrderByFechaRegistroDesc(telDigitos);
        if (porTel.isEmpty() && !telDigitos.equals(valorBusqueda)) {
            porTel = reservaRepository.findByTelefonoOrderByFechaRegistroDesc(valorBusqueda);
        }

        if (!porTel.isEmpty()) {
            Reserva masRelevante = porTel.stream()
                    .filter(r -> !Boolean.TRUE.equals(r.getArchivada()) && !"Cancelada".equalsIgnoreCase(r.getEstado()) && !"Denegada".equalsIgnoreCase(r.getEstado()) && !"Expirada".equalsIgnoreCase(r.getEstado()))
                    .findFirst()
                    .orElse(porTel.get(0));
            return esAdministrador(auth) ? ResponseEntity.ok(masRelevante) : ResponseEntity.ok(sanitizarParaPublico(masRelevante));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", "No encontramos reservas asociadas al número o código ingresado."));
    }

    public ResponseEntity<?> consultar(String codigo, String telefono) {
        return consultar(null, codigo, telefono, null);
    }

    public ResponseEntity<?> consultar(String query) {
        return consultar(query, null, null, null);
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(
            @PathVariable String id,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String telefono,
            Authentication auth) {
        return reservaRepository.findById(id).map(reserva -> {
            if (esAdministrador(auth)) {
                return ResponseEntity.ok(reserva);
            }
            boolean esPropietario = false;
            if (codigo != null && codigo.trim().equalsIgnoreCase(reserva.getCodigoReserva())) {
                esPropietario = true;
            }
            if (telefono != null && normalizarTelefono(telefono).equals(normalizarTelefono(reserva.getTelefono()))) {
                esPropietario = true;
            }
            if (esPropietario) {
                return ResponseEntity.ok(reserva);
            } else {
                Reserva sanitizada = new Reserva();
                sanitizada.setId(reserva.getId());
                sanitizada.setCodigoReserva(reserva.getCodigoReserva());
                sanitizada.setEstado(reserva.getEstado());
                sanitizada.setEstadoPago(reserva.getEstadoPago());
                sanitizada.setFechaExpiracionPago(reserva.getFechaExpiracionPago());
                sanitizada.setItemsInventario(reserva.getItemsInventario());
                sanitizada.setSubtotal(reserva.getSubtotal());
                sanitizada.setAnticipo(reserva.getAnticipo());
                sanitizada.setSaldo(reserva.getSaldo());
                sanitizada.setMedioPago(reserva.getMedioPago());
                sanitizada.setReferenciaWompi(reserva.getReferenciaWompi());
                sanitizada.setMontoPagoCentavos(reserva.getMontoPagoCentavos());
                return ResponseEntity.ok(sanitizada);
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/reenviar-email")
    public ResponseEntity<?> reenviarEmail(@PathVariable String id, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensaje", "Acceso no autorizado"));
        }
        return reservaRepository.findById(id).map(reserva -> {
            if (reserva.getEmail() == null || reserva.getEmail().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "SIN_CORREO", "mensaje", "La reserva no tiene un correo electrónico registrado."));
            }
            if (emailNotificationService == null) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", "SERVICIO_NO_DISPONIBLE", "mensaje", "El servicio de correo no está disponible."));
            }
            boolean enviado = emailNotificationService.reenviarConfirmacion(reserva);
            return ResponseEntity.ok(Map.of(
                    "success", enviado,
                    "mensaje", enviado ? "Comprobante reenviado con éxito por correo." : "No se pudo reenviar el correo. Verifica la configuración de email.",
                    "emailConfirmacionEnviada", enviado
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public synchronized ResponseEntity<?> crear(@Valid @RequestBody Reserva reserva) {
        reserva.setFechaRegistro(LocalDate.now(java.time.ZoneId.of("America/Bogota")));
        reserva.setArchivada(false);
        reserva.setFechaArchivado(null);
        reserva.setTelefono(normalizarTelefono(reserva.getTelefono()));

        // Asegurar que no hay contaminación de citas en pedidos puros
        boolean tieneItems = reserva.getItemsInventario() != null && !reserva.getItemsInventario().isEmpty();
        boolean tieneServicioEnItems = tieneItems && reserva.getItemsInventario().stream().anyMatch(i -> "servicio".equalsIgnoreCase(i.getTipo()));
        boolean tieneProductoEnItems = tieneItems && reserva.getItemsInventario().stream().anyMatch(i -> !"servicio".equalsIgnoreCase(i.getTipo()));
        if (tieneItems && tieneProductoEnItems && !tieneServicioEnItems) {
            reserva.setFechaCita(null);
            reserva.setHoraCita(null);
        }

        // Validación de correo electrónico
        if (reserva.getEmail() != null && !reserva.getEmail().isBlank()) {
            String email = reserva.getEmail().trim();
            if (!email.contains("@") || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                return ResponseEntity.badRequest().body(Map.of("error", "EMAIL_INVALIDO", "mensaje", "El correo electrónico no es válido."));
            }
            reserva.setEmail(email);
        }

        // Validación de entrega a domicilio
        if (reserva.esDomicilio()) {
            String dir = reserva.getDireccionEntrega() != null ? reserva.getDireccionEntrega().trim() : "";
            if (dir.isBlank()) {
                Map<String, Object> err = new LinkedHashMap<>();
                err.put("ok", false);
                err.put("code", "DIRECCION_REQUERIDA");
                err.put("error", "DIRECCION_REQUERIDA");
                err.put("mensaje", "Ingresa la dirección de entrega para continuar.");
                err.put("message", "Ingresa la dirección de entrega para continuar.");
                return ResponseEntity.badRequest().body(err);
            }
            reserva.setDireccionEntrega(dir);
        } else {
            reserva.setTipoEntrega(reserva.getTipoEntrega() != null && !reserva.getTipoEntrega().isBlank() ? reserva.getTipoEntrega() : "pickup");
        }

        if (tieneServicio(reserva) && HorarioUtil.isPastTimeSlot(reserva.getFechaCita(), reserva.getHoraCita(), clock))
            return ResponseEntity.badRequest().body(Map.of("error", "HORARIO_PASADO", "mensaje", "Ese horario ya pasó. Selecciona un horario posterior."));
        if (tieneServicio(reserva) && !horarioConfigurado(reserva.getFechaCita(), reserva.getHoraCita()))
            return ResponseEntity.badRequest().body(Map.of("error", "HORARIO_FUERA_DE_AGENDA", "mensaje", "La fecha u hora seleccionada ya no está disponible en la agenda."));
        
        // Limpiar reservas expiradas en el horario solicitado antes de validar ocupación
        if (tieneServicio(reserva) && reserva.getFechaCita() != null && reserva.getHoraCita() != null) {
            reservaRepository.findByFechaCita(reserva.getFechaCita()).stream()
                    .filter(r -> !Boolean.TRUE.equals(r.getArchivada()))
                    .filter(r -> reserva.getHoraCita().equals(r.getHoraCita()))
                    .filter(reservaService::estaExpirada)
                    .forEach(reservaService::marcarExpirada);
        }

        if (tieneServicio(reserva) && agendaOcupada(reserva.getFechaCita(), reserva.getHoraCita(), null))
            return conflicto("Ese horario acaba de ser reservado. Elige otro turno.");
        try {
            reservaService.prepararNuevaReserva(reserva);
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "";
            if (msg.toLowerCase().contains("correo") || msg.toLowerCase().contains("email")) {
                return ResponseEntity.badRequest().body(Map.of("error", "EMAIL_INVALIDO", "mensaje", msg));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "DATOS_INVALIDOS", "mensaje", msg));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "STOCK_NO_DISPONIBLE", "mensaje", ex.getMessage()));
        }
        reserva.setCodigoReserva(generarCodigo());
        try {
            Reserva guardada = reservaRepository.save(reserva);
            
            // Notificación administrativa centralizada y aislada
            if (guardada.esPedidoPuro()) {
                notificacionAdminService.registrarNuevaCompra(guardada);
            } else {
                notificacionAdminService.registrarNuevaCita(guardada);
            }

            String msg = guardada.esPedido() ? "Pedido creado. Tienes 24 horas para enviar el comprobante." : "Tu solicitud ISIVI fue recibida. Te confirmaremos al validar el comprobante.";
            whatsapp.enviar(guardada, whatsapp.resumen(guardada, msg));
            return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
        } catch (DuplicateKeyException ex) {
            reservaService.liberarInventario(reserva);
            return conflicto("El horario seleccionado acaba de ser reservado por otro cliente. Por favor selecciona otro turno.");
        }
    }


    @PatchMapping("/{id}/aprobar")
    public synchronized ResponseEntity<?> aprobar(@PathVariable String id) {
        return reservaRepository.findById(id).<ResponseEntity<?>>map(existente -> {
            try {
                Reserva guardada = reservaService.aprobar(existente);
                String msg = guardada.esPedido() ? "Comprobante confirmado. Pedido listo para preparación." : "Tu reserva ISIVI está confirmada.";
                whatsapp.enviar(guardada, whatsapp.resumen(guardada, msg));
                return ResponseEntity.ok(guardada);
            } catch (IllegalStateException ex) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/denegar")
    public synchronized ResponseEntity<?> denegar(@PathVariable String id) {
        return reservaRepository.findById(id).<ResponseEntity<?>>map(existente -> {
            Reserva guardada;
            try { guardada = reservaService.denegar(existente); }
            catch (IllegalStateException ex) { return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage())); }
            whatsapp.enviar(guardada, whatsapp.resumen(guardada, "Tu solicitud ISIVI no pudo ser aprobada. Escríbenos para ayudarte a agendar otra fecha."));
            return ResponseEntity.ok(guardada);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/reprogramar")
    public synchronized ResponseEntity<?> reprogramar(@PathVariable String id, @RequestBody(required = false) GestionReservaRequest datos) {
        if (datos == null || datos.getCodigoReserva() == null || datos.getCodigoReserva().isBlank()
                || datos.getTelefono() == null || datos.getTelefono().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El código de reserva y el teléfono son obligatorios."));
        }
        if (datos.getFechaCita() == null || datos.getHoraCita() == null || datos.getHoraCita().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Selecciona una fecha y hora válidas."));
        }
        return reservaRepository.findById(id).<ResponseEntity<?>>map(existente -> {
            if (!coincide(existente, datos)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("mensaje", "No pudimos validar tu reserva. Código o teléfono incorrectos."));
            }
            if (List.of("Expirada", "Denegada", "Cancelada", "Cancelado").contains(existente.getEstado())) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", "No se puede reprogramar una reserva que ya ha finalizado o ha sido cancelada."));
            }
            if (tieneServicio(existente) && HorarioUtil.isPastTimeSlot(datos.getFechaCita(), datos.getHoraCita(), clock))
                return ResponseEntity.badRequest().body(Map.of("error", "HORARIO_PASADO", "mensaje", "Ese horario ya pasó. Selecciona un horario posterior."));
            if (tieneServicio(existente) && !horarioConfigurado(datos.getFechaCita(), datos.getHoraCita()))
                return ResponseEntity.badRequest().body(Map.of("mensaje", "La fecha u hora seleccionada ya no está disponible en la agenda."));
            
            // Limpiar reservas expiradas en el horario objetivo
            if (tieneServicio(existente)) {
                reservaRepository.findByFechaCita(datos.getFechaCita()).stream()
                        .filter(r -> !Boolean.TRUE.equals(r.getArchivada()))
                        .filter(r -> datos.getHoraCita().equals(r.getHoraCita()))
                        .filter(reservaService::estaExpirada)
                        .forEach(reservaService::marcarExpirada);
            }

            if (tieneServicio(existente) && agendaOcupada(datos.getFechaCita(), datos.getHoraCita(), existente.getId()))
                return conflicto("Ese horario ya no está disponible.");

            // Guardar propuesta de reprogramación sin sobreescribir la fecha original
            existente.setFechaPropuestaReprogramacion(datos.getFechaCita());
            existente.setHoraPropuestaReprogramacion(datos.getHoraCita());
            existente.setEstadoPrevioReprogramacion(existente.getEstado());
            existente.setEstado("Pendiente Reprogramación");
            try {
                Reserva guardada = reservaRepository.save(existente);
                
                // Alerta asíncrona de administración
                notificacionAdminService.registrarSolicitudReprogramacion(guardada);
                
                whatsapp.enviar(guardada, whatsapp.resumen(guardada, "Tu solicitud de reprogramación ISIVI fue recibida."));
                return ResponseEntity.ok(guardada);
            } catch (DuplicateKeyException ex) {
                return conflicto("El horario seleccionado ya no está disponible. Por favor selecciona otro turno.");
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/admin-reprogramar")
    public synchronized ResponseEntity<?> adminReprogramar(@PathVariable String id, @RequestBody Map<String, String> body) {
        String fecha = body.get("fechaCita");
        String hora = body.get("horaCita");
        if (fecha == null || hora == null || fecha.isBlank() || hora.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Selecciona una fecha y hora válidas."));
        }
        LocalDate fechaCita = LocalDate.parse(fecha);
        return reservaRepository.findById(id).<ResponseEntity<?>>map(existente -> {
            if (List.of("Expirada", "Denegada", "Cancelada", "Cancelado").contains(existente.getEstado())) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", "No se puede reprogramar una reserva que ya ha finalizado o ha sido cancelada."));
            }
            if (tieneServicio(existente) && HorarioUtil.isPastTimeSlot(fechaCita, hora, clock)) {
                return ResponseEntity.badRequest().body(Map.of("error", "HORARIO_PASADO", "mensaje", "Ese horario ya pasó. Selecciona un horario posterior."));
            }
            if (tieneServicio(existente) && !horarioConfigurado(fechaCita, hora)) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", "La fecha u hora seleccionada ya no está disponible en la agenda."));
            }

            // Limpiar reservas expiradas en el horario objetivo
            if (tieneServicio(existente)) {
                reservaRepository.findByFechaCita(fechaCita).stream()
                        .filter(r -> !Boolean.TRUE.equals(r.getArchivada()))
                        .filter(r -> hora.equals(r.getHoraCita()))
                        .filter(reservaService::estaExpirada)
                        .forEach(reservaService::marcarExpirada);
            }

            if (tieneServicio(existente) && agendaOcupada(fechaCita, hora, existente.getId())) {
                return conflicto("Ese horario ya no está disponible.");
            }
            existente.setFechaCita(fechaCita);
            existente.setHoraCita(hora);
            try {
                Reserva guardada = reservaRepository.save(existente);
                whatsapp.enviar(guardada, whatsapp.resumen(guardada, "Tu cita ISIVI fue actualizada para el " + fechaCita + " a las " + hora + "."));
                return ResponseEntity.ok(guardada);
            } catch (DuplicateKeyException ex) {
                return conflicto("El horario seleccionado ya no está disponible. Por favor selecciona otro turno.");
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/aprobar-reprogramacion")
    public synchronized ResponseEntity<?> aprobarReprogramacion(@PathVariable String id) {
        return reservaRepository.findById(id).<ResponseEntity<?>>map(existente -> {
            if (List.of("Expirada", "Denegada", "Cancelada", "Cancelado").contains(existente.getEstado())) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", "No se puede reprogramar una reserva que ya ha finalizado o ha sido cancelada."));
            }
            if (!"Pendiente Reprogramación".equalsIgnoreCase(existente.getEstado())) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", "La reserva no está pendiente de reprogramación."));
            }
            LocalDate nuevaFecha = existente.getFechaPropuestaReprogramacion();
            String nuevaHora = existente.getHoraPropuestaReprogramacion();
            if (nuevaFecha == null || nuevaHora == null || nuevaHora.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", "No hay una propuesta de reprogramación válida registrada."));
            }
            if (tieneServicio(existente) && agendaOcupada(nuevaFecha, nuevaHora, existente.getId())) {
                return conflicto("El nuevo horario solicitado ya no está disponible.");
            }
            existente.setFechaCita(nuevaFecha);
            existente.setHoraCita(nuevaHora);
            existente.setFechaPropuestaReprogramacion(null);
            existente.setHoraPropuestaReprogramacion(null);
            existente.setEstadoPrevioReprogramacion(null);
            existente.setEstado("Confirmado");
            Reserva guardada = reservaRepository.save(existente);
            whatsapp.enviar(guardada, whatsapp.resumen(guardada, "Tu solicitud de reprogramación fue aprobada. Nueva fecha: " + nuevaFecha + " a las " + nuevaHora + "."));
            return ResponseEntity.ok(guardada);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/rechazar-reprogramacion")
    public synchronized ResponseEntity<?> rechazarReprogramacion(@PathVariable String id) {
        return reservaRepository.findById(id).<ResponseEntity<?>>map(existente -> {
            if (!"Pendiente Reprogramación".equalsIgnoreCase(existente.getEstado())) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", "La reserva no está pendiente de reprogramación."));
            }
            String previo = existente.getEstadoPrevioReprogramacion();
            if (previo == null || previo.isBlank()) {
                previo = "Confirmado";
            }
            existente.setFechaPropuestaReprogramacion(null);
            existente.setHoraPropuestaReprogramacion(null);
            existente.setEstadoPrevioReprogramacion(null);
            existente.setEstado(previo);
            Reserva guardada = reservaRepository.save(existente);
            whatsapp.enviar(guardada, whatsapp.resumen(guardada, "Tu solicitud de reprogramación no pudo ser aprobada. Se mantiene tu horario original."));
            return ResponseEntity.ok(guardada);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/cancelar")
    public synchronized ResponseEntity<?> cancelar(@PathVariable String id, @RequestBody(required = false) GestionReservaRequest datos) {
        if (datos == null || datos.getCodigoReserva() == null || datos.getCodigoReserva().isBlank()
                || datos.getTelefono() == null || datos.getTelefono().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El código de reserva y el teléfono son obligatorios para cancelar."));
        }
        return reservaRepository.findById(id).<ResponseEntity<?>>map(existente -> {
            if (!coincide(existente, datos)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("mensaje", "No pudimos validar tu reserva. Código o teléfono incorrectos."));
            }
            try {
                Map<String, Object> resultado = reservaService.procesarCancelacionCliente(existente, datos.getMotivoCancelacion());
                Reserva guardada = (Reserva) resultado.get("reserva");
                if (guardada != null) {
                    if ("Cancelada".equalsIgnoreCase(guardada.getEstado())) {
                        whatsapp.enviar(guardada, whatsapp.resumen(guardada, "Tu reserva ISIVI fue cancelada. Si necesitas ayuda, escríbenos."));
                    } else if ("Pendiente Cancelación".equalsIgnoreCase(guardada.getEstado())) {
                        notificacionAdminService.registrarSolicitudCancelacion(guardada);
                    }
                }
                return ResponseEntity.ok(resultado);
            } catch (CancelacionNoPermitidaException ex) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "ok", false,
                        "code", "CANCELACION_NO_PERMITIDA",
                        "message", ex.getMessage(),
                        "horasMinimas", ex.getHorasMinimas()
                ));
            } catch (IllegalStateException ex) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/aprobar-cancelacion")
    public synchronized ResponseEntity<?> aprobarCancelacion(@PathVariable String id) {
        return reservaRepository.findById(id).<ResponseEntity<?>>map(existente -> {
            try {
                Reserva guardada = reservaService.aprobarSolicitudCancelacion(existente);
                whatsapp.enviar(guardada, whatsapp.resumen(guardada, "Tu solicitud de cancelación ISIVI fue aprobada. Tu cita ha sido cancelada."));
                return ResponseEntity.ok(guardada);
            } catch (IllegalStateException ex) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/rechazar-cancelacion")
    public synchronized ResponseEntity<?> rechazarCancelacion(@PathVariable String id, @RequestBody(required = false) Map<String, String> body) {
        String motivo = body != null ? body.get("motivo") : null;
        return reservaRepository.findById(id).<ResponseEntity<?>>map(existente -> {
            try {
                Reserva guardada = reservaService.rechazarSolicitudCancelacion(existente, motivo);
                whatsapp.enviar(guardada, whatsapp.resumen(guardada, "Tu solicitud de cancelación ISIVI no fue aprobada. Tu cita sigue confirmada."));
                return ResponseEntity.ok(guardada);
            } catch (IllegalStateException ex) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/admin-cancelar")
    public synchronized ResponseEntity<?> adminCancelar(@PathVariable String id, @RequestBody(required = false) Map<String, String> body) {
        String motivo = body != null ? body.get("motivo") : null;
        return reservaRepository.findById(id).<ResponseEntity<?>>map(existente -> {
            try {
                Reserva guardada = reservaService.cancelarAdministrativa(existente, motivo);
                whatsapp.enviar(guardada, whatsapp.resumen(guardada, "Tu cita ISIVI fue cancelada administrativamente. Escríbenos si deseas reprogramar."));
                return ResponseEntity.ok(guardada);
            } catch (IllegalStateException ex) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/marcar-cancelacion-vista")
    public synchronized ResponseEntity<?> marcarCancelacionVista(@PathVariable String id) {
        return reservaRepository.findById(id).<ResponseEntity<?>>map(existente -> {
            Reserva guardada = reservaService.marcarNotificacionCancelacionVista(existente);
            return ResponseEntity.ok(guardada);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/liberar-retencion")
    public synchronized ResponseEntity<?> liberarRetencion(@PathVariable String id, @RequestBody(required = false) Map<String, String> credenciales) {
        Reserva existente = reservaRepository.findById(id).orElse(null);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            String telefono = credenciales != null ? credenciales.get("telefono") : null;
            String codigoReserva = credenciales != null ? credenciales.get("codigoReserva") : null;
            Reserva liberada = reservaService.liberarRetencionProvisional(existente, telefono, codigoReserva);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Retención de horario liberada exitosamente.",
                    "id", liberada.getId(),
                    "estado", liberada.getEstado()
            ));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "NO_AUTORIZADO", "mensaje", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/items")
    public synchronized ResponseEntity<?> actualizarItems(
            @PathVariable String id,
            @RequestBody List<com.isivi.app.model.ItemReserva> nuevosItems,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String telefono,
            Authentication auth) {
        
        Reserva res = reservaRepository.findById(id).orElse(null);
        if (res == null) return ResponseEntity.notFound().build();
        
        boolean esAdmin = auth != null && auth.isAuthenticated();
        boolean esPropietario = false;
        if (codigo != null && codigo.trim().equalsIgnoreCase(res.getCodigoReserva())) {
            esPropietario = true;
        }
        if (telefono != null && normalizarTelefono(telefono).equals(normalizarTelefono(res.getTelefono()))) {
            esPropietario = true;
        }
        
        if (!esAdmin && !esPropietario) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("mensaje", "No autorizado para modificar esta reserva."));
        }
        
        if ("APROBADO".equalsIgnoreCase(res.getEstadoPago()) || "Confirmado".equalsIgnoreCase(res.getEstado()) || "Pago Confirmado".equalsIgnoreCase(res.getEstado())) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "No se puede modificar un pedido ya pagado o confirmado."));
        }
        
        try {
            reservaService.actualizarItemsDeReserva(res, nuevosItems);
            return ResponseEntity.ok(res);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "STOCK_NO_DISPONIBLE", "mensaje", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
        }
    }


    @PatchMapping("/{id}/pedido/preparar")
    public synchronized ResponseEntity<?> marcarPedidoEnPreparacion(@PathVariable String id, Authentication auth) {
        return reservaRepository.findById(id).<ResponseEntity<?>>map(existente -> {
            try {
                String adminUser = (auth != null && auth.getName() != null) ? auth.getName() : "admin";
                Reserva guardada = reservaService.marcarPedidoEnPreparacion(existente, adminUser);
                return ResponseEntity.ok(guardada);
            } catch (IllegalStateException ex) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/pedido/listo-envio")
    public synchronized ResponseEntity<?> marcarPedidoListoEnvio(@PathVariable String id, Authentication auth) {
        return reservaRepository.findById(id).<ResponseEntity<?>>map(existente -> {
            try {
                String adminUser = (auth != null && auth.getName() != null) ? auth.getName() : "admin";
                Reserva guardada = reservaService.marcarPedidoListoEnvio(existente, adminUser);
                whatsapp.enviar(guardada, whatsapp.resumen(guardada, "Tu pedido ISIVI está listo para envío a tu dirección registrada."));
                return ResponseEntity.ok(guardada);
            } catch (IllegalStateException ex) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/pedido/en-camino")
    public synchronized ResponseEntity<?> marcarPedidoEnCamino(@PathVariable String id, Authentication auth) {
        return reservaRepository.findById(id).<ResponseEntity<?>>map(existente -> {
            try {
                String adminUser = (auth != null && auth.getName() != null) ? auth.getName() : "admin";
                Reserva guardada = reservaService.marcarPedidoEnCamino(existente, adminUser);
                whatsapp.enviar(guardada, whatsapp.resumen(guardada, "Tu pedido ISIVI va en camino a tu dirección de entrega 🚚"));
                return ResponseEntity.ok(guardada);
            } catch (IllegalStateException ex) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/pedido/listo-recoger")
    public synchronized ResponseEntity<?> marcarPedidoListoParaRecoger(@PathVariable String id, Authentication auth) {
        return reservaRepository.findById(id).<ResponseEntity<?>>map(existente -> {
            try {
                String adminUser = (auth != null && auth.getName() != null) ? auth.getName() : "admin";
                Reserva guardada = reservaService.marcarPedidoListoParaRecoger(existente, adminUser);
                whatsapp.enviar(guardada, whatsapp.resumen(guardada, "Tu pedido ISIVI está listo para recoger en nuestro salón."));
                return ResponseEntity.ok(guardada);
            } catch (IllegalStateException ex) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/pedido/entregar")
    public synchronized ResponseEntity<?> marcarPedidoEntregado(@PathVariable String id, Authentication auth) {
        return reservaRepository.findById(id).<ResponseEntity<?>>map(existente -> {
            try {
                String adminUser = (auth != null && auth.getName() != null) ? auth.getName() : "admin";
                Reserva guardada = reservaService.marcarPedidoEntregado(existente, adminUser);
                return ResponseEntity.ok(guardada);
            } catch (IllegalStateException ex) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public synchronized ResponseEntity<?> eliminar(@PathVariable String id) {
        return reservaRepository.findById(id).<ResponseEntity<?>>map(reserva -> {
            boolean isPermitted = esTerminal(reserva) || "Pendiente Pago".equalsIgnoreCase(reserva.getEstado());
            if (!isPermitted) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "mensaje", "No se permite la eliminación de registros activos de la operación."
                ));
            }
            reservaService.liberarInventario(reserva);
            
            String estPago = reserva.getEstadoPago() != null ? reserva.getEstadoPago().trim().toUpperCase() : "";
            boolean tieneHuellaFinanciera = (reserva.getReferenciaWompi() != null && !reserva.getReferenciaWompi().isBlank())
                || (reserva.getTransaccionWompiId() != null && !reserva.getTransaccionWompiId().isBlank())
                || "WOMPI".equalsIgnoreCase(reserva.getMedioPago())
                || "TRANSFERENCIA".equalsIgnoreCase(reserva.getMedioPago())
                || "RECHAZADO".equals(estPago)
                || "ERROR".equals(estPago);
                
            if (tieneHuellaFinanciera) {
                reserva.setArchivada(true);
                Clock effectiveClock = this.clock != null ? this.clock : Clock.system(HorarioUtil.ZONA_BOGOTA);
                reserva.setFechaArchivado(LocalDate.now(effectiveClock));
                reservaRepository.save(reserva);
                return ResponseEntity.ok(Map.of("mensaje", "Registro archivado correctamente."));
            } else {
                reservaRepository.delete(reserva);
                return ResponseEntity.noContent().build();
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping
    public synchronized ResponseEntity<Void> eliminarTodas() {
        reservaRepository.findAll().forEach(reserva -> {
            String est = reserva.getEstado() != null ? reserva.getEstado().trim().toUpperCase() : "";
            String estPago = reserva.getEstadoPago() != null ? reserva.getEstadoPago().trim().toUpperCase() : "";
            String estPed = reserva.getEstadoPedido() != null ? reserva.getEstadoPedido().trim().toUpperCase() : "";
            
            boolean esEstadoNoEliminable = List.of("CONFIRMADO", "PAGO_CONFIRMADO", "EN_PREPARACION", "LISTO_ENVIO", "EN_CAMINO", "ENTREGADO", "LISTO_RECOGER", "RECOGIDO", "PAGO CONFIRMADO").contains(est)
                || List.of("CONFIRMADO", "PAGO_CONFIRMADO", "EN_PREPARACION", "LISTO_ENVIO", "EN_CAMINO", "ENTREGADO", "LISTO_RECOGER", "RECOGIDO", "PAGO CONFIRMADO").contains(estPed)
                || "APROBADO".equals(estPago);
                
            if (esEstadoNoEliminable) {
                return;
            }
            
            reservaService.liberarInventario(reserva);
            
            boolean tieneHuellaFinanciera = (reserva.getReferenciaWompi() != null && !reserva.getReferenciaWompi().isBlank())
                || (reserva.getTransaccionWompiId() != null && !reserva.getTransaccionWompiId().isBlank())
                || "WOMPI".equalsIgnoreCase(reserva.getMedioPago())
                || "TRANSFERENCIA".equalsIgnoreCase(reserva.getMedioPago())
                || "RECHAZADO".equals(estPago)
                || "ERROR".equals(estPago);
                
            if (tieneHuellaFinanciera) {
                reserva.setArchivada(true);
                Clock effectiveClock = this.clock != null ? this.clock : Clock.system(HorarioUtil.ZONA_BOGOTA);
                reserva.setFechaArchivado(LocalDate.now(effectiveClock));
                reservaRepository.save(reserva);
            } else {
                reservaRepository.delete(reserva);
            }
        });
        return ResponseEntity.noContent().build();
    }

    /** Se ejecuta cada minuto como limpieza de fondo secundaria (la disponibilidad evalúa expiración en tiempo real). */
    @Scheduled(fixedDelay = 60000)
    public synchronized void limpiarReservasExpiradasProgramado() {
        reservaService.expirarReservasVencidas();
    }

    /**
     * Se ejecuta todas las mañanas a las 8:00 AM para despachar recordatorios de WhatsApp
     * a clientes que tengan citas confirmadas para el día de mañana.
     * 
     * Semántica del envío: At-Least-Once. 
     * Se prioriza evitar la pérdida silenciosa de recordatorios. 
     * Si la API externa lanza una excepción antes de aceptar el mensaje (error de red o mala petición),
     * la reserva NO es marcada como enviada, permitiendo reintentos controlados en el próximo ciclo.
     * En caso de excepciones desconocidas tras intentar enviar, no asumimos éxito y el scheduler continúa.
     */
    @Scheduled(cron = "0 0 8 * * *", zone = "America/Bogota")
    public synchronized void enviarRecordatoriosWhatsAppProgramado() {
        Clock effectiveClock = this.clock != null ? this.clock : Clock.system(HorarioUtil.ZONA_BOGOTA);
        LocalDate mañana = LocalDate.now(effectiveClock).plusDays(1);
        List<Reserva> mañanasRes = reservaRepository.findByFechaCita(mañana);
        mañanasRes.stream()
                .filter(r -> r.esCita() && "Confirmado".equalsIgnoreCase(r.getEstado()))
                .filter(r -> !Boolean.TRUE.equals(r.getArchivada()))
                .filter(r -> !Boolean.TRUE.equals(r.getRecordatorioEnviado()))
                .forEach(r -> {
                    boolean exitoEnvio = false;
                    try {
                        String mensaje = "¡Hola! Te recordamos tu cita ISIVI programada para mañana a las " + r.getHoraCita() + ". Te esperamos.";
                        whatsapp.enviar(r, whatsapp.resumen(r, mensaje));
                        exitoEnvio = true;
                    } catch (Exception ex) {
                        // Desempaquetar la causa de la excepción si es necesario
                        Throwable cause = ex;
                        boolean isTimeoutOrHttp = false;
                        while (cause != null) {
                            if (cause instanceof org.springframework.web.client.ResourceAccessException || 
                                cause instanceof org.springframework.web.client.RestClientResponseException) {
                                isTimeoutOrHttp = true;
                                break;
                            }
                            cause = cause.getCause();
                        }
                        
                        if (isTimeoutOrHttp || ex instanceof org.springframework.web.client.ResourceAccessException || 
                            ex instanceof org.springframework.web.client.RestClientResponseException) {
                            // Error de conexión, Timeout o HTTP del servidor. Permitimos reintento en el siguiente ciclo.
                            exitoEnvio = false;
                        } else {
                            // Cualquier otra excepción. Por seguridad no marcamos.
                            exitoEnvio = false;
                        }
                    }

                    if (exitoEnvio) {
                        try {
                            r.setRecordatorioEnviado(true);
                            reservaRepository.save(r);
                        } catch (Exception ignored) {
                            // Si falla la persistencia local, asumimos At-Least-Once
                        }
                    }
                });
    }

    /** Se ejecuta cada madrugada y también antes de consultar la agenda. */
    @Scheduled(cron = "0 5 0 * * *", zone = "America/Bogota")
    public synchronized void archivarReservasVencidas() {
        Clock effectiveClock = this.clock != null ? this.clock : Clock.system(HorarioUtil.ZONA_BOGOTA);
        LocalDate hoy = LocalDate.now(effectiveClock);
        reservaRepository.findAll().forEach(reserva -> {
            if (Boolean.TRUE.equals(reserva.getArchivada())) return;
            if (reserva.esCita() && reserva.getFechaCita() != null && reserva.getFechaCita().isBefore(hoy)) {
                reserva.setArchivada(true);
                reserva.setFechaArchivado(hoy);
                if (!"Cancelada".equalsIgnoreCase(reserva.getEstado()) && !"Denegada".equalsIgnoreCase(reserva.getEstado()) && !"Expirada".equalsIgnoreCase(reserva.getEstado())) {
                    reserva.setEstado("Realizada");
                }
                reservaRepository.save(reserva);
            } else if (reserva.esPedidoPuro()) {
                // Solo archivar pedidos terminales (Entregado, Cancelado, Denegada, Expirada).
                // Los pedidos activos (Pago Confirmado, En preparación, Listo para recoger) NUNCA se archivan por fecha!
                String st = reserva.getEstado();
                if ("Entregado".equalsIgnoreCase(st) || "Cancelada".equalsIgnoreCase(st) || "Cancelado".equalsIgnoreCase(st) || "Denegada".equalsIgnoreCase(st) || "Expirada".equalsIgnoreCase(st)) {
                    LocalDate ref = reserva.getFechaRegistro();
                    if (ref != null && ref.isBefore(hoy)) {
                        reserva.setArchivada(true);
                        reserva.setFechaArchivado(hoy);
                        reservaRepository.save(reserva);
                    }
                }
            }
        });
    }


    private boolean coincide(Reserva reserva, GestionReservaRequest datos) { return reserva.getCodigoReserva().equalsIgnoreCase(datos.getCodigoReserva().trim()) && reserva.getTelefono().equals(normalizarTelefono(datos.getTelefono())); }
    
    public boolean bloqueaAgenda(Reserva r) {
        if (r == null || Boolean.TRUE.equals(r.getArchivada())) return false;
        String estado = r.getEstado();
        if ("Cancelada".equalsIgnoreCase(estado) || "Denegada".equalsIgnoreCase(estado) || "Expirada".equalsIgnoreCase(estado) || "CONFLICTO_PAGO_EXPIRADO".equalsIgnoreCase(estado)) {
            return false;
        }
        if ("Confirmado".equalsIgnoreCase(estado) || "Pendiente Comprobante".equalsIgnoreCase(estado) || "Pendiente Reprogramación".equalsIgnoreCase(estado) || "Solicitud Cancelación".equalsIgnoreCase(estado)) {
            return tieneServicio(r);
        }
        if ("Pendiente Pago".equalsIgnoreCase(estado)) {
            if ("APROBADO".equalsIgnoreCase(r.getEstadoPago())) {
                return tieneServicio(r);
            }
            java.time.Instant ahora = java.time.Instant.now(clock);
            if (r.getFechaExpiracionPago() != null) {
                return r.getFechaExpiracionPago().isAfter(ahora) && tieneServicio(r);
            }
            return tieneServicio(r);
        }
        return tieneServicio(r);
    }

    private boolean tieneServicio(Reserva r) {
        if (r.getFechaCita() == null && r.getHoraCita() == null) return false;
        if (r.getItemsInventario() != null && !r.getItemsInventario().isEmpty()) {
            return r.getItemsInventario().stream().anyMatch(i -> "servicio".equalsIgnoreCase(i.getTipo()));
        }
        return true;
    }
    private boolean horarioConfigurado(LocalDate fecha, String hora) {
        if (fecha == null || hora == null) return false;

        Optional<ExcepcionAgenda> excepcionOpt = excepcionAgendaRepository.findByFecha(fecha);
        if (excepcionOpt.isPresent()) {
            ExcepcionAgenda ex = excepcionOpt.get();
            if ("CERRADO".equalsIgnoreCase(ex.getTipo())) {
                return false;
            }
            if ("HORARIO_ESPECIAL".equalsIgnoreCase(ex.getTipo())) {
                return ex.getHorarios() != null && ex.getHorarios().contains(hora);
            }
        }

        return configuracionAgendaRepository.findById("principal")
                .map(configuracion -> configuracion.getDiasLaborales().contains(fecha.getDayOfWeek().getValue() % 7) && configuracion.getHorarios().contains(hora))
                .orElse(fecha.getDayOfWeek().getValue() != 1 && List.of("08:00 AM", "09:30 AM", "11:00 AM", "01:30 PM", "03:00 PM", "04:30 PM", "06:00 PM").contains(hora));
    }
    public boolean agendaOcupada(LocalDate fecha, String hora, String excluirId) {
        if (fecha == null || hora == null || hora.isBlank()) return false;
        boolean manualBloqueado = bloqueoHorarioRepository.existsByFechaCitaAndHoraCita(fecha, hora);
        if (manualBloqueado) return true;

        Optional<ExcepcionAgenda> excepcionOpt = excepcionAgendaRepository.findByFecha(fecha);
        boolean excepcionEspecial = excepcionOpt.isPresent() && "HORARIO_ESPECIAL".equalsIgnoreCase(excepcionOpt.get().getTipo());
        if (!excepcionEspecial) {
            boolean recurrenteBloqueado = bloqueoRecurrenteRepository.findByActivoTrue().stream()
                    .anyMatch(br -> br.aplicaAFecha(fecha) && br.getHorarios() != null && br.getHorarios().contains(hora));
            if (recurrenteBloqueado) return true;
        }

        return reservaRepository.findByFechaCita(fecha).stream()
                .anyMatch(r -> !Boolean.TRUE.equals(r.getArchivada()) && bloqueaAgenda(r) && hora.equals(r.getHoraCita()) && (excluirId == null || !excluirId.equals(r.getId())));
    }



    private ResponseEntity<Map<String, String>> conflicto(String mensaje) { return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "HORARIO_NO_DISPONIBLE", "mensaje", mensaje)); }
    private String generarCodigo() { String codigo; do { codigo = "ISV-" + ThreadLocalRandom.current().nextInt(1000, 10000); } while (reservaRepository.existsByCodigoReserva(codigo)); return codigo; }
    private String normalizarTelefono(String telefono) { return telefono == null ? "" : telefono.replaceAll("\\D", ""); }

}
