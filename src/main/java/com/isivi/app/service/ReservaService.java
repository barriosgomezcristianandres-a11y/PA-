package com.isivi.app.service;

import com.isivi.app.exception.CancelacionNoPermitidaException;
import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Kit;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Reserva;
import com.isivi.app.model.Servicio;
import com.isivi.app.model.VarianteProducto;
import com.isivi.app.repository.KitRepository;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.repository.ServicioRepository;
import com.isivi.app.util.HorarioUtil;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Reglas de pedidos de productos y reservas de servicios. */
@Service
public class ReservaService {
    private static final Logger log = LoggerFactory.getLogger(ReservaService.class);
    public static final String PENDIENTE = "Pendiente Comprobante";
    public static final String PENDIENTE_PAGO = "Pendiente Pago";
    public static final String PENDIENTE_REPROGRAMACION = "Pendiente Reprogramación";
    public static final String CONFIRMADO = "Confirmado";
    public static final String DENEGADO = "Denegada";
    public static final String CANCELADO = "Cancelada";
    public static final String EXPIRADA = "Expirada";
    public static final String SOLICITUD_CANCELACION = "Solicitud Cancelación";

    // Estados Canónicos de Pedidos de Productos / Kits
    public static final String PAGO_CONFIRMADO = "Pago Confirmado";
    public static final String EN_PREPARACION = "En preparación";
    public static final String LISTO_ENVIO = "Listo para envío";
    public static final String EN_CAMINO = "En camino";
    public static final String LISTO_PARA_RECOGER = "Listo para recoger";
    public static final String RECOGIDO = "Recogido";
    public static final String ENTREGADO = "Entregado";

    // Estados Canónicos de Citas
    public static final String EN_CURSO = "En curso";
    public static final String REALIZADA = "Realizada";


    @Value("${isivi.reserva.pago-expiracion-minutos:15}")
    private int pagoExpiracionMinutos = 15;

    @Value("${isivi.reserva.cancelacion-horas-minimas:24}")
    private int cancelacionHorasMinimas = 24;

    @Value("${isivi.reserva.comprobante-expiracion-horas:24}")
    private int comprobanteExpiracionHoras = 24;

    private Clock clock = Clock.system(HorarioUtil.ZONA_BOGOTA);

    private final ProductoRepository productos;
    private final KitRepository kits;
    private final ServicioRepository servicios;
    private final ReservaRepository reservas;
    private final MongoTemplate mongo;
    private final EmailNotificationService emailNotificationService;
    private final WhatsAppNotificationService whatsapp;

    public ReservaService(ProductoRepository productos, KitRepository kits, ServicioRepository servicios,
                          ReservaRepository reservas, MongoTemplate mongo,
                          @Autowired(required = false) EmailNotificationService emailNotificationService,
                          @Autowired(required = false) WhatsAppNotificationService whatsapp) {
        this.productos = productos;
        this.kits = kits;
        this.servicios = servicios;
        this.reservas = reservas;
        this.mongo = mongo;
        this.emailNotificationService = emailNotificationService;
        this.whatsapp = whatsapp;
    }

    public void setClock(Clock clock) {
        this.clock = clock != null ? clock : Clock.system(HorarioUtil.ZONA_BOGOTA);
    }

    public Clock getClock() {
        return this.clock;
    }

    public void setPagoExpiracionMinutos(int minutos) {
        this.pagoExpiracionMinutos = minutos;
    }

    public int getPagoExpiracionMinutos() {
        return this.pagoExpiracionMinutos;
    }

    public void setCancelacionHorasMinimas(int horas) {
        this.cancelacionHorasMinimas = horas;
    }

    public int getCancelacionHorasMinimas() {
        return this.cancelacionHorasMinimas;
    }

    public void setComprobanteExpiracionHoras(int horas) {
        this.comprobanteExpiracionHoras = horas;
    }

    public int getComprobanteExpiracionHoras() {
        return this.comprobanteExpiracionHoras;
    }

    public boolean esCitaPasada(Reserva r) {
        if (r == null || r.getFechaCita() == null) return false;
        return HorarioUtil.isPastTimeSlot(r.getFechaCita(), r.getHoraCita(), clock);
    }

    public boolean esCancelacionTardia(Reserva r) {
        if (r == null || r.getFechaCita() == null) return false;
        java.time.LocalTime lt = HorarioUtil.parseTimeSlot(r.getHoraCita());
        if (lt == null) lt = java.time.LocalTime.of(8, 0);
        java.time.LocalDateTime citaDateTime = java.time.LocalDateTime.of(r.getFechaCita(), lt);
        java.time.ZonedDateTime citaZoned = citaDateTime.atZone(HorarioUtil.ZONA_BOGOTA);
        java.time.ZonedDateTime ahora = java.time.ZonedDateTime.now(clock.withZone(HorarioUtil.ZONA_BOGOTA));
        long horasRestantes = Duration.between(ahora, citaZoned).toHours();
        // <= cancelacionHorasMinimas -> tardía (requiere revisión administrativa)
        // > cancelacionHorasMinimas -> normal (cancelación automática directa)
        return horasRestantes <= cancelacionHorasMinimas;
    }

    @PostConstruct
    public void inicializarYVerificarIndices() {
        try {
            if (!mongo.collectionExists("reservas")) {
                mongo.createCollection("reservas");
            }
            inspeccionarDuplicadosExistentes();
            asegurarIndiceUnicoHorarioActivo();
        } catch (Exception ex) {
            log.error("Aviso al verificar/crear índices en MongoDB para reservas: {}", ex.getMessage());
        }
    }

    public void inspeccionarDuplicadosExistentes() {
        try {
            List<Reserva> activas = reservas.findAll().stream()
                    .filter(r -> !Boolean.TRUE.equals(r.getArchivada()))
                    .filter(r -> !CANCELADO.equalsIgnoreCase(r.getEstado()) && !EXPIRADA.equalsIgnoreCase(r.getEstado()) && !DENEGADO.equalsIgnoreCase(r.getEstado()))
                    .filter(r -> r.getFechaCita() != null
                            && r.getHoraCita() != null
                            && !r.getHoraCita().isBlank())
                    .toList();

            Map<String, List<Reserva>> agrupadas = activas.stream()
                    .collect(Collectors.groupingBy(r -> r.getFechaCita().toString() + "_" + r.getHoraCita().trim()));

            boolean hayDuplicados = false;
            for (Map.Entry<String, List<Reserva>> entry : agrupadas.entrySet()) {
                List<Reserva> duplicadas = entry.getValue();
                if (duplicadas.size() > 1) {
                    hayDuplicados = true;
                    log.warn("ATENCIÓN: Se encontraron reservas duplicadas para el horario {}. Archivando duplicados históricos para asegurar índice único.", entry.getKey());
                    for (int i = 1; i < duplicadas.size(); i++) {
                        Reserva d = duplicadas.get(i);
                        d.setArchivada(true);
                        d.setFechaArchivado(LocalDate.now());
                        reservas.save(d);
                    }
                }
            }
            if (!hayDuplicados) {
                log.info("Inspección de MongoDB: No se encontraron reservas duplicadas en horarios activos.");
            }
        } catch (Exception ex) {
            log.warn("No se pudo completar la inspección de duplicados: {}", ex.getMessage());
        }
    }

    public void asegurarIndiceUnicoHorarioActivo() {
        try {
            for (Document doc : mongo.getCollection("reservas").listIndexes()) {
                if ("reserva_fecha_hora_activa_idx".equals(doc.getString("name"))) {
                    try {
                        mongo.getCollection("reservas").dropIndex("reserva_fecha_hora_activa_idx");
                    } catch (Exception ignored) {}
                    break;
                }
            }

            Document keys = new Document("fechaCita", 1).append("horaCita", 1);
            Document partialFilter = new Document("archivada", false)
                    .append("fechaCita", new Document("$type", "date"))
                    .append("horaCita", new Document("$type", "string"))
                    .append("estado", new Document("$in", List.of(CONFIRMADO, PENDIENTE, PENDIENTE_REPROGRAMACION, PENDIENTE_PAGO, SOLICITUD_CANCELACION)));

            com.mongodb.client.model.IndexOptions options = new com.mongodb.client.model.IndexOptions()
                    .name("reserva_fecha_hora_activa_idx")
                    .unique(true)
                    .partialFilterExpression(partialFilter);

            mongo.getCollection("reservas").createIndex(keys, options);
            log.info("Índice único compuesto parcial 'reserva_fecha_hora_activa_idx' asegurado con éxito en MongoDB.");
        } catch (Exception ex) {
            log.warn("Nota sobre creación de índice único: {}", ex.getMessage());
        }
    }

    public Reserva prepararNuevaReserva(Reserva solicitud) {
        List<ItemReserva> items = normalizarItems(solicitud.getItemsInventario());
        boolean tieneServicios = items.stream().anyMatch(i -> "servicio".equals(i.getTipo()));
        List<String> nombres = new ArrayList<>();
        double total = 0;
        double anticipo = 0;
        for (ItemReserva item : items) {
            double precio = precioActual(item);
            item.setPrecioUnitario(precio);
            double sub = precio * item.getCantidad();
            item.setSubtotal(sub);
            total += sub;
            nombres.add(item.getNombre() + (item.getCantidad() > 1 ? " x" + item.getCantidad() : ""));
            
            if (tieneServicios) {
                if ("servicio".equals(item.getTipo())) {
                    anticipo += sub * 0.25;
                } else {
                    anticipo += sub;
                }
            }
        }
        solicitud.setItemsInventario(items);
        solicitud.setItems(nombres);
        solicitud.setSubtotal(total);
        solicitud.setAnticipo(anticipo);
        solicitud.setSaldo(total - anticipo);
        solicitud.setEstado(PENDIENTE);
        solicitud.setInventarioReservado(false);
        reservarInventario(solicitud);
        return solicitud;
    }

    public Reserva aprobar(Reserva reserva) {
        if (CONFIRMADO.equalsIgnoreCase(reserva.getEstado()) || PAGO_CONFIRMADO.equalsIgnoreCase(reserva.getEstado())) return reserva; // Idempotencia
        String target = reserva.esPedidoPuro() ? PAGO_CONFIRMADO : CONFIRMADO;
        validarTransicion(reserva, target);
        reserva.setEstado(target);
        reserva.setEstadoPago("APROBADO");
        if (reserva.esPedidoPuro() && reserva.getSubtotal() != null) {
            reserva.setAnticipo(reserva.getSubtotal());
            reserva.setSaldo(0.0);
        }
        Reserva guardada = reservas.save(reserva);
        if (emailNotificationService != null) {
            emailNotificationService.enviarConfirmacion(guardada);
        }
        return guardada;
    }

    public Reserva prepararPagoWompi(Reserva reserva, String referencia) {
        if (CONFIRMADO.equals(reserva.getEstado()) || PAGO_CONFIRMADO.equals(reserva.getEstado())) throw new IllegalStateException("La reserva o pedido ya fue confirmado.");
        if (CANCELADO.equals(reserva.getEstado())) throw new IllegalStateException("La reserva se encuentra cancelada.");
        if (estaExpirada(reserva)) {
            if (reserva.esPedidoPuro()) {
                reserva.setEstado(PENDIENTE_PAGO);
                reserva.setEstadoPago("PENDIENTE");
            } else {
                marcarExpirada(reserva);
                throw new IllegalStateException("El tiempo de retención para este horario ha expirado. Por favor selecciona otro turno.");
            }
        }
        boolean tieneServicio = reserva.getItemsInventario().stream().anyMatch(i -> "servicio".equals(i.getTipo()));
        boolean tieneProducto = reserva.getItemsInventario().stream().anyMatch(i -> !"servicio".equals(i.getTipo()));
        double valSubtotal = reserva.getSubtotal() != null ? reserva.getSubtotal() : 0.0;
        double valAnticipo = reserva.getAnticipo() != null ? reserva.getAnticipo() : 0.0;
        if (tieneServicio && valAnticipo == 0.0 && valSubtotal > 0.0) {
            double calcAnticipo = 0.0;
            for (ItemReserva item : reserva.getItemsInventario()) {
                double price = item.getSubtotal() != null ? item.getSubtotal() : 0.0;
                if ("servicio".equals(item.getTipo())) {
                    calcAnticipo += price * 0.25;
                } else {
                    calcAnticipo += price;
                }
            }
            valAnticipo = calcAnticipo;
        }
        long monto = Math.round((tieneServicio ? valAnticipo : valSubtotal) * 100d);
        if (monto <= 0) throw new IllegalStateException("El monto de pago no es válido.");

        if (!Boolean.TRUE.equals(reserva.getInventarioReservado()) && tieneProducto) {
            try {
                reservarInventario(reserva);
            } catch (IllegalStateException ex) {
                if (reserva.esPedidoPuro()) {
                    throw new IllegalStateException("El producto ya no está disponible.");
                }
                throw ex;
            }
        }

        Instant ahora = Instant.now(clock);
        reserva.setFechaExpiracionPago(ahora.plus(Duration.ofMinutes(pagoExpiracionMinutos)));
        reserva.setReferenciaWompi(referencia);
        reserva.setMontoPagoCentavos(monto);
        reserva.setEstadoPago("PENDIENTE");
        reserva.setMedioPago("WOMPI");
        reserva.setEstado(PENDIENTE_PAGO);
        return reservas.save(reserva);
    }

    public Reserva confirmarPagoWompi(Reserva reserva) {
        if (("Confirmado".equalsIgnoreCase(reserva.getEstado()) || PAGO_CONFIRMADO.equalsIgnoreCase(reserva.getEstado()))
                && "APROBADO".equalsIgnoreCase(reserva.getEstadoPago())) {
            return reserva; 
        }
        if (!PENDIENTE_PAGO.equalsIgnoreCase(reserva.getEstado()) && !"Expirada".equalsIgnoreCase(reserva.getEstado()) && !PENDIENTE.equalsIgnoreCase(reserva.getEstado())) {
            throw new IllegalStateException("La reserva no está pendiente de pago Wompi.");
        }
        reserva.setEstadoPago("APROBADO");
        reserva.setEstado(reserva.esPedidoPuro() ? PAGO_CONFIRMADO : CONFIRMADO);
        reserva.setFechaExpiracionPago(null); 

        boolean tieneServicio = reserva.getItemsInventario() != null && reserva.getItemsInventario().stream().anyMatch(i -> "servicio".equals(i.getTipo()));
        if (!tieneServicio && reserva.getSubtotal() != null) {
            reserva.setAnticipo(reserva.getSubtotal());
            reserva.setSaldo(0.0);
        }
        Reserva guardada = reservas.save(reserva);
        if (emailNotificationService != null) {
            emailNotificationService.enviarConfirmacion(guardada);
        }
        return guardada;
    }

    public Reserva marcarPedidoEnPreparacion(Reserva reserva, String adminUser) {
        if (reserva == null) throw new IllegalArgumentException("El pedido no existe.");
        if ("EN_PREPARACION".equalsIgnoreCase(reserva.getEstadoPedido())) return reserva; 
        validarTransicionLogistica(reserva, "EN_PREPARACION");
        reserva.setEstadoPedido("EN_PREPARACION");
        reserva.setFechaEnPreparacion(Instant.now(clock));
        if (adminUser != null && !adminUser.isBlank()) reserva.setAtendidoPor(adminUser);
        return reservas.save(reserva);
    }

    public Reserva marcarPedidoListoEnvio(Reserva reserva, String adminUser) {
        if (reserva == null) throw new IllegalArgumentException("El pedido no existe.");
        if (!reserva.esDomicilio()) {
            throw new IllegalStateException("Esta acción solo aplica a pedidos con entrega a domicilio.");
        }
        if ("LISTO_ENVIO".equalsIgnoreCase(reserva.getEstadoPedido())) return reserva; 
        validarTransicionLogistica(reserva, "LISTO_ENVIO");
        reserva.setEstadoPedido("LISTO_ENVIO");
        reserva.setFechaListoEnvio(Instant.now(clock));
        if (adminUser != null && !adminUser.isBlank()) reserva.setAtendidoPor(adminUser);
        return reservas.save(reserva);
    }

    public Reserva marcarPedidoEnCamino(Reserva reserva, String adminUser) {
        if (reserva == null) throw new IllegalArgumentException("El pedido no existe.");
        if (!reserva.esDomicilio()) {
            throw new IllegalStateException("Esta acción solo aplica a pedidos con entrega a domicilio.");
        }
        if ("EN_CAMINO".equalsIgnoreCase(reserva.getEstadoPedido())) return reserva; 
        validarTransicionLogistica(reserva, "EN_CAMINO");
        reserva.setEstadoPedido("EN_CAMINO");
        reserva.setFechaEnCamino(Instant.now(clock));
        if (adminUser != null && !adminUser.isBlank()) reserva.setAtendidoPor(adminUser);
        return reservas.save(reserva);
    }

    public Reserva marcarPedidoListoParaRecoger(Reserva reserva, String adminUser) {
        if (reserva == null) throw new IllegalArgumentException("El pedido no existe.");
        if (reserva.esDomicilio()) {
            throw new IllegalStateException("Esta acción solo aplica a pedidos para recoger en el local (pickup).");
        }
        if ("LISTO_RECOGER".equalsIgnoreCase(reserva.getEstadoPedido())) return reserva; 
        validarTransicionLogistica(reserva, "LISTO_RECOGER");
        reserva.setEstadoPedido("LISTO_RECOGER");
        reserva.setFechaListoParaRecoger(Instant.now(clock));
        if (adminUser != null && !adminUser.isBlank()) reserva.setAtendidoPor(adminUser);
        Reserva guardada = reservas.save(reserva);
        if (emailNotificationService != null) {
            emailNotificationService.enviarPedidoListoParaRecoger(guardada);
        }
        return guardada;
    }

    public Reserva marcarPedidoEntregado(Reserva reserva, String adminUser) {
        if (reserva == null) throw new IllegalArgumentException("El pedido no existe.");
        String estadoPedidoDestino = reserva.esDomicilio() ? "ENTREGADO" : "RECOGIDO";
        if (estadoPedidoDestino.equalsIgnoreCase(reserva.getEstadoPedido())) return reserva; 
        validarTransicionLogistica(reserva, estadoPedidoDestino);
        reserva.setEstadoPedido(estadoPedidoDestino);
        reserva.setFechaEntregado(Instant.now(clock));
        reserva.setArchivada(true);
        reserva.setFechaArchivado(LocalDate.now(clock));
        if (adminUser != null && !adminUser.isBlank()) reserva.setAtendidoPor(adminUser);
        return reservas.save(reserva);
    }

    public int calcularDuracionMinutos(Reserva reserva) {
        if (reserva == null || reserva.getItemsInventario() == null || reserva.getItemsInventario().isEmpty()) {
            return 60;
        }
        int totalMinutos = 0;
        for (ItemReserva item : reserva.getItemsInventario()) {
            if ("servicio".equalsIgnoreCase(item.getTipo())) {
                try {
                    Servicio s = servicios.findById(item.getId()).orElse(null);
                    if (s != null && s.getDuracion() != null) {
                        totalMinutos += HorarioUtil.parseDurationMinutes(s.getDuracion());
                    } else {
                        totalMinutos += 60;
                    }
                } catch (Exception e) {
                    totalMinutos += 60;
                }
            }
        }
        return totalMinutos > 0 ? totalMinutos : 60;
    }

    public boolean citaFinalizada(Reserva reserva) {
        if (reserva == null || !reserva.esCita() || reserva.getFechaCita() == null || reserva.getHoraCita() == null) {
            return false;
        }
        String st = reserva.getEstado();
        if (CANCELADO.equalsIgnoreCase(st) || DENEGADO.equalsIgnoreCase(st) || EXPIRADA.equalsIgnoreCase(st) || SOLICITUD_CANCELACION.equalsIgnoreCase(st)) {
            return false;
        }
        int duracion = calcularDuracionMinutos(reserva);
        return HorarioUtil.isAppointmentCompleted(reserva.getFechaCita(), reserva.getHoraCita(), duracion, clock);
    }

    public boolean citaEnCurso(Reserva reserva) {
        if (reserva == null || !reserva.esCita() || reserva.getFechaCita() == null || reserva.getHoraCita() == null) {
            return false;
        }
        String st = reserva.getEstado();
        if (CANCELADO.equalsIgnoreCase(st) || DENEGADO.equalsIgnoreCase(st) || EXPIRADA.equalsIgnoreCase(st) || SOLICITUD_CANCELACION.equalsIgnoreCase(st)) {
            return false;
        }
        int duracion = calcularDuracionMinutos(reserva);
        return HorarioUtil.isAppointmentInProgress(reserva.getFechaCita(), reserva.getHoraCita(), duracion, clock);
    }


    public Reserva denegar(Reserva reserva) {
        if (DENEGADO.equals(reserva.getEstado())) return reserva; 
        validarTransicion(reserva, DENEGADO);
        liberarInventario(reserva);
        reserva.setEstado(DENEGADO);
        reserva.setFechaExpiracionPago(null);
        return reservas.save(reserva);
    }

    public Reserva cancelar(Reserva reserva) {
        return cancelar(reserva, "CLIENTE", null);
    }

    public Reserva cancelar(Reserva reserva, String origenCancelacion, String motivo) {
        if (CANCELADO.equals(reserva.getEstado())) return reserva; 
        validarTransicion(reserva, CANCELADO);
        liberarInventario(reserva);
        reserva.setEstado(CANCELADO);
        reserva.setFechaExpiracionPago(null);
        if (reserva.getFechaCancelacion() == null) {
            reserva.setFechaCancelacion(Instant.now(clock));
        }
        reserva.setCanceladaPor(origenCancelacion != null ? origenCancelacion : "CLIENTE");
        if (motivo != null && !motivo.isBlank()) {
            reserva.setMotivoCancelacion(motivo.trim());
        } else if (reserva.getMotivoCancelacion() == null) {
            reserva.setMotivoCancelacion("Sin motivo indicado");
        }
        if ("ADMIN".equalsIgnoreCase(origenCancelacion) || "LIBERACION_PROVISIONAL".equalsIgnoreCase(origenCancelacion)) {
            reserva.setNotificacionCancelacionVista(true);
        } else {
            reserva.setNotificacionCancelacionVista(false);
        }
        return reservas.save(reserva);
    }

    public Map<String, Object> procesarCancelacionCliente(Reserva reserva, String motivo) {
        if (reserva == null) throw new IllegalArgumentException("La reserva no existe.");
        if (CANCELADO.equalsIgnoreCase(reserva.getEstado())) {
            return Map.of("estado", CANCELADO, "tipoProceso", "CANCELADA", "mensaje", "Esta cita ya fue cancelada.", "reserva", reserva);
        }
        if (DENEGADO.equalsIgnoreCase(reserva.getEstado())) {
            throw new IllegalStateException("La reserva se encuentra denegada y no puede cancelarse.");
        }
        if (esCitaPasada(reserva)) {
            throw new IllegalStateException("Esta cita ya pasó y no puede cancelarse.");
        }

        if (PENDIENTE_PAGO.equalsIgnoreCase(reserva.getEstado()) && !"APROBADO".equalsIgnoreCase(reserva.getEstadoPago())) {
            Reserva guardada = cancelar(reserva, "CLIENTE", motivo);
            return Map.of("estado", CANCELADO, "tipoProceso", "CANCELADA", "mensaje", "Tu cita ha sido cancelada exitosamente y el horario fue liberado.", "reserva", guardada);
        }

        if (esCancelacionTardia(reserva)) {
            throw new CancelacionNoPermitidaException(
                    "Las cancelaciones deben realizarse con más de " + cancelacionHorasMinimas + " horas de anticipación.",
                    cancelacionHorasMinimas
            );
        } else {
            Reserva guardada = cancelar(reserva, "CLIENTE", motivo);
            if (emailNotificationService != null) {
                try {
                    emailNotificationService.enviarCancelacion(guardada);
                } catch (Exception ex) {
                    log.error("Error al enviar email de cancelación para {}: {}", guardada.getCodigoReserva(), ex.getMessage());
                }
            }
            return Map.of("estado", CANCELADO, "tipoProceso", "CANCELADA", "mensaje", "Tu cita ha sido cancelada exitosamente y el horario fue liberado.", "reserva", guardada);
        }
    }

    public Reserva aprobarSolicitudCancelacion(Reserva reserva) {
        if (reserva == null) throw new IllegalArgumentException("La reserva no existe.");
        if (CANCELADO.equals(reserva.getEstado())) return reserva; 
        if (!SOLICITUD_CANCELACION.equals(reserva.getEstado())) {
            throw new IllegalStateException("La reserva no se encuentra en estado de Solicitud de Cancelación.");
        }
        Reserva cancelada = cancelar(reserva, "CLIENTE_APROBADA_ADMIN", reserva.getMotivoCancelacion());
        cancelada.setNotificacionCancelacionVista(true);
        return reservas.save(cancelada);
    }

    public Reserva rechazarSolicitudCancelacion(Reserva reserva, String motivoRechazo) {
        if (reserva == null) throw new IllegalArgumentException("La reserva no existe.");
        if (!SOLICITUD_CANCELACION.equals(reserva.getEstado())) {
            return reserva; 
        }
        String previo = (reserva.getEstadoPrevioCancelacion() != null && !reserva.getEstadoPrevioCancelacion().isBlank())
                ? reserva.getEstadoPrevioCancelacion() : CONFIRMADO;
        validarTransicion(reserva, previo);
        reserva.setEstado(previo);
        reserva.setMotivoRechazoCancelacion(motivoRechazo != null && !motivoRechazo.isBlank() ? motivoRechazo.trim() : "Sin motivo indicado");
        reserva.setFechaSolicitudCancelacion(null);
        reserva.setNotificacionCancelacionVista(true);
        return reservas.save(reserva);
    }

    public Reserva cancelarAdministrativa(Reserva reserva, String motivo) {
        if (reserva == null) throw new IllegalArgumentException("La reserva no existe.");
        return cancelar(reserva, "ADMIN", motivo);
    }

    public Reserva marcarNotificacionCancelacionVista(Reserva reserva) {
        if (reserva == null) return null;
        reserva.setNotificacionCancelacionVista(true);
        return reservas.save(reserva);
    }

    public Reserva liberarRetencionProvisional(Reserva reserva, String telefono, String codigoReserva) {
        if (reserva == null) throw new IllegalArgumentException("La reserva no existe.");
        if (CONFIRMADO.equals(reserva.getEstado()) || "APROBADO".equalsIgnoreCase(reserva.getEstadoPago())) {
            throw new IllegalStateException("No se puede liberar una reserva ya confirmada y pagada.");
        }
        if (CANCELADO.equals(reserva.getEstado()) || EXPIRADA.equals(reserva.getEstado()) || DENEGADO.equals(reserva.getEstado())) {
            return reserva; 
        }

        if (telefono != null && !telefono.isBlank()) {
            String telNorm = normalizarTelefono(telefono);
            if (!telNorm.equals(normalizarTelefono(reserva.getTelefono()))) {
                throw new SecurityException("Los datos de contacto no coinciden con el titular de la reserva.");
            }
        }
        if (codigoReserva != null && !codigoReserva.isBlank()) {
            if (!codigoReserva.trim().equalsIgnoreCase(reserva.getCodigoReserva())) {
                throw new SecurityException("El código de reserva no coincide con el titular de la reserva.");
            }
        }

        liberarInventario(reserva); 
        reserva.setEstado(CANCELADO);
        reserva.setFechaExpiracionPago(null);
        if (reserva.getFechaCancelacion() == null) {
            reserva.setFechaCancelacion(Instant.now(clock));
        }
        reserva.setCanceladaPor("LIBERACION_PROVISIONAL");
        reserva.setNotificacionCancelacionVista(true);
        return reservas.save(reserva);
    }


    public Reserva retomarPagoWompi(Reserva reserva, String telefono, String codigoReserva, String nuevaReferencia) {
        if (reserva == null) throw new IllegalArgumentException("La reserva no existe.");
        if (Boolean.TRUE.equals(reserva.getArchivada())) throw new IllegalStateException("La reserva se encuentra archivada.");
        if (CONFIRMADO.equals(reserva.getEstado()) || "APROBADO".equalsIgnoreCase(reserva.getEstadoPago())) {
            throw new IllegalStateException("La reserva ya se encuentra confirmada y pagada.");
        }
        if (DENEGADO.equals(reserva.getEstado()) || "RECHAZADO".equalsIgnoreCase(reserva.getEstadoPago())) {
            if (!reserva.esPedidoPuro()) {
                throw new IllegalStateException("La reserva fue denegada o rechazada. Por favor inicia un nuevo proceso.");
            }
        }
        if (CANCELADO.equals(reserva.getEstado())) {
            throw new IllegalStateException("La reserva se encuentra cancelada.");
        }
        if (estaExpirada(reserva)) {
            if (reserva.esPedidoPuro()) {
                reserva.setEstado(PENDIENTE_PAGO);
                reserva.setEstadoPago("PENDIENTE");
            } else {
                marcarExpirada(reserva);
                throw new IllegalStateException("El tiempo de retención para este horario ha expirado. Por favor selecciona otro turno.");
            }
        }

        if (telefono != null && !telefono.isBlank()) {
            String telNorm = normalizarTelefono(telefono);
            if (!telNorm.equals(normalizarTelefono(reserva.getTelefono()))) {
                throw new SecurityException("Los datos de contacto no coinciden con el titular de la reserva.");
            }
        }
        if (codigoReserva != null && !codigoReserva.isBlank()) {
            if (!codigoReserva.trim().equalsIgnoreCase(reserva.getCodigoReserva())) {
                throw new SecurityException("El código de reserva no coincide con el titular de la reserva.");
            }
        }

        boolean tieneServicio = reserva.getItemsInventario() != null && reserva.getItemsInventario().stream().anyMatch(i -> "servicio".equals(i.getTipo()));
        boolean tieneProducto = reserva.getItemsInventario() != null && reserva.getItemsInventario().stream().anyMatch(i -> !"servicio".equals(i.getTipo()));

        double valSubtotal = reserva.getSubtotal() != null ? reserva.getSubtotal() : 0.0;
        double valAnticipo = reserva.getAnticipo() != null ? reserva.getAnticipo() : 0.0;
        if (tieneServicio && valAnticipo == 0.0 && valSubtotal > 0.0) {
            valAnticipo = valSubtotal * 0.25;
        }
        long monto = Math.round((tieneServicio ? valAnticipo : valSubtotal) * 100d);
        if (monto <= 0) throw new IllegalStateException("El monto de pago no es válido.");

        if (!Boolean.TRUE.equals(reserva.getInventarioReservado()) && tieneProducto) {
            try {
                reservarInventario(reserva);
            } catch (IllegalStateException ex) {
                if (reserva.esPedidoPuro()) {
                    throw new IllegalStateException("El producto ya no está disponible.");
                }
                throw ex;
            }
        }

        reserva.setReferenciaWompi(nuevaReferencia);
        reserva.setMontoPagoCentavos(monto);
        reserva.setEstadoPago("PENDIENTE");
        reserva.setMedioPago("WOMPI");
        reserva.setEstado(PENDIENTE_PAGO);
        return reservas.save(reserva);
    }

    private String normalizarTelefono(String tel) {
        return tel == null ? "" : tel.replaceAll("\\D", "");
    }


    public Instant getFechaCreacion(Reserva r) {
        if (r == null) return Instant.now(clock);
        if (r.getId() != null && r.getId().length() == 24) {
            try {
                long timestamp = Long.parseLong(r.getId().substring(0, 8), 16);
                return Instant.ofEpochSecond(timestamp);
            } catch (Exception ignored) {}
        }
        if (r.getFechaRegistro() != null) {
            return r.getFechaRegistro().atStartOfDay(HorarioUtil.ZONA_BOGOTA).toInstant();
        }
        return Instant.now(clock);
    }

    public boolean estaExpirada(Reserva r) {
        if (r == null) return false;
        if (EXPIRADA.equalsIgnoreCase(r.getEstado()) || CANCELADO.equalsIgnoreCase(r.getEstado())) return true;
        
        Instant ahora = Instant.now(clock);
        
        if (PENDIENTE_PAGO.equalsIgnoreCase(r.getEstado()) && !"APROBADO".equalsIgnoreCase(r.getEstadoPago())) {
            return r.getFechaExpiracionPago() != null && !r.getFechaExpiracionPago().isAfter(ahora);
        }
        
        if (PENDIENTE.equalsIgnoreCase(r.getEstado())) {
            Instant fechaCreacion = getFechaCreacion(r);
            Instant fechaExpiracion = fechaCreacion.plus(Duration.ofHours(comprobanteExpiracionHoras));
            return !fechaExpiracion.isAfter(ahora);
        }
        
        return false;
    }

    public Reserva marcarExpirada(Reserva reserva) {
        if (reserva == null) return null;
        if (EXPIRADA.equals(reserva.getEstado())) return reserva; // Idempotencia
        
        if (reserva.esMixto()) {
            // Es una operación mixta. La expiración del hold de productos:
            // 1. Libera inventario
            liberarInventario(reserva);
            
            // 2. Conservar solo los servicios en la reserva
            List<ItemReserva> soloServicios = reserva.getItemsInventario().stream()
                    .filter(i -> "servicio".equals(i.getTipo()))
                    .toList();
            reserva.setItemsInventario(soloServicios);
            
            // 3. Recalcular montos
            recalcularMontosYGuardar(reserva);
            
            // 4. Conservar la cita:
            // - Cambiar a PENDIENTE (Pendiente Comprobante) para que siga las reglas de vencimiento de comprobante
            // - Limpiar fechaExpiracionPago para evitar bucles. La cita seguirá las reglas de agenda
            reserva.setFechaExpiracionPago(null);
            reserva.setEstado(PENDIENTE);
            
            log.info("Expiración de hold de productos en mixto {}: productos liberados, cita conservada.", reserva.getCodigoReserva());
            return reservas.save(reserva);
        } else {
            liberarInventario(reserva); // Idempotente: solo libera si inventarioReservado == true
            reserva.setEstado(EXPIRADA);
            return reservas.save(reserva);
        }
    }

    public void expirarReservasVencidas() {
        try {
            Instant ahora = Instant.now(clock);
            List<Reserva> pendientes = reservas.findAll().stream()
                    .filter(r -> !Boolean.TRUE.equals(r.getArchivada()))
                    .filter(r -> PENDIENTE_PAGO.equalsIgnoreCase(r.getEstado()) || PENDIENTE.equalsIgnoreCase(r.getEstado()))
                    .filter(r -> !"APROBADO".equalsIgnoreCase(r.getEstadoPago()))
                    .filter(this::estaExpirada)
                    .toList();

            for (Reserva r : pendientes) {
                try {
                    marcarExpirada(r);
                } catch (Exception ex) {
                    log.warn("Aviso al expirar reserva {}: {}", r.getCodigoReserva(), ex.getMessage());
                }
            }
        } catch (Exception ex) {
            log.warn("Aviso durante escaneo de reservas expiradas: {}", ex.getMessage());
        }
    }

    public void reservarInventario(Reserva reserva) {
        if (Boolean.TRUE.equals(reserva.getInventarioReservado())) return;
        List<ItemReserva> reservados = new ArrayList<>();
        try {
            for (ItemReserva item : reserva.getItemsInventario()) {
                if (!"servicio".equals(item.getTipo())) {
                    if (!descontarAtomico(item)) {
                        int stockActual = 0;
                        boolean existe = false;
                        if ("producto".equals(item.getTipo())) {
                            Producto p = productos.findById(item.getId()).orElse(null);
                            if (p != null) {
                                existe = true;
                                if ("VARIANTES".equalsIgnoreCase(p.getTipoPrecio()) && item.getVarianteId() != null) {
                                    VarianteProducto v = p.getVariantes().stream().filter(it -> it.getId().equals(item.getVarianteId())).findFirst().orElse(null);
                                    stockActual = v != null ? v.getCantidad() : 0;
                                } else {
                                    stockActual = p.getCantidad() != null ? p.getCantidad() : 0;
                                }
                            }
                        } else {
                            Kit k = kits.findById(item.getId()).orElse(null);
                            if (k != null) {
                                existe = true;
                                stockActual = k.getCantidad() != null ? k.getCantidad() : 0;
                            }
                        }

                        if (!existe) {
                            throw new IllegalStateException("El producto ya no está disponible.");
                        } else if (stockActual <= 0) {
                            throw new IllegalStateException("El producto acaba de agotarse. Actualizamos tu carrito.");
                        } else {
                            throw new IllegalStateException("Solo quedan " + stockActual + " unidades disponibles.");
                        }
                    }
                    reservados.add(item);
                }
            }
            reserva.setInventarioReservado(true);
        } catch (RuntimeException ex) {
            reservados.forEach(this::sumarStock);
            throw ex;
        }
    }

    public void liberarInventario(Reserva reserva) {
        if (!Boolean.TRUE.equals(reserva.getInventarioReservado())) return;
        if (reserva.getItemsInventario() != null) {
            reserva.getItemsInventario().stream().filter(i -> !"servicio".equals(i.getTipo())).forEach(this::sumarStock);
        }
        reserva.setInventarioReservado(false);
    }

    public void validarTransicion(Reserva reserva, String destino) {
        String origen = reserva.getEstado();
        if (destino.equalsIgnoreCase(origen)) return; // Idempotente

        boolean esDomicilio = reserva.esDomicilio();
        boolean esRecogida = !esDomicilio;

        // Reglas de exclusión estricta según forma de entrega en pedidos
        if (reserva.esPedido()) {
            if (esDomicilio && (LISTO_PARA_RECOGER.equalsIgnoreCase(destino) || RECOGIDO.equalsIgnoreCase(destino) || "LISTO_RECOGER".equalsIgnoreCase(destino) || "RECOGIDO".equalsIgnoreCase(destino))) {
                throw new IllegalStateException("Los pedidos con entrega a domicilio no pueden pasar a estados de recogida en local.");
            }
            if (esRecogida && (LISTO_ENVIO.equalsIgnoreCase(destino) || EN_CAMINO.equalsIgnoreCase(destino) || ENTREGADO.equalsIgnoreCase(destino) || "ENTREGADO".equalsIgnoreCase(destino))) {
                throw new IllegalStateException("Los pedidos para recoger en el local no pueden pasar a estados de envío o entrega a domicilio.");
            }
        }

        boolean esPendiente = PENDIENTE.equalsIgnoreCase(origen) || PENDIENTE_REPROGRAMACION.equalsIgnoreCase(origen) || PENDIENTE_PAGO.equalsIgnoreCase(origen);
        boolean esConfirmado = CONFIRMADO.equalsIgnoreCase(origen) || PAGO_CONFIRMADO.equalsIgnoreCase(origen);
        boolean esEnPreparacion = EN_PREPARACION.equalsIgnoreCase(origen);
        boolean esListoEnvio = LISTO_ENVIO.equalsIgnoreCase(origen);
        boolean esEnCamino = EN_CAMINO.equalsIgnoreCase(origen);
        boolean esListoRecoger = LISTO_PARA_RECOGER.equalsIgnoreCase(origen);
        boolean esSolicitudCancel = SOLICITUD_CANCELACION.equalsIgnoreCase(origen);
        boolean esEnCurso = EN_CURSO.equalsIgnoreCase(origen);

        boolean valida = (esPendiente && (CONFIRMADO.equalsIgnoreCase(destino) || PAGO_CONFIRMADO.equalsIgnoreCase(destino) || DENEGADO.equalsIgnoreCase(destino) || CANCELADO.equalsIgnoreCase(destino) || EXPIRADA.equalsIgnoreCase(destino) || SOLICITUD_CANCELACION.equalsIgnoreCase(destino)))
                || (esConfirmado && (EN_PREPARACION.equalsIgnoreCase(destino) || (esRecogida && LISTO_PARA_RECOGER.equalsIgnoreCase(destino)) || (esDomicilio && LISTO_ENVIO.equalsIgnoreCase(destino)) || EN_CURSO.equalsIgnoreCase(destino) || REALIZADA.equalsIgnoreCase(destino) || CANCELADO.equalsIgnoreCase(destino) || SOLICITUD_CANCELACION.equalsIgnoreCase(destino)))
                || (esEnPreparacion && ((esDomicilio && (LISTO_ENVIO.equalsIgnoreCase(destino) || EN_CAMINO.equalsIgnoreCase(destino) || ENTREGADO.equalsIgnoreCase(destino))) || (esRecogida && (LISTO_PARA_RECOGER.equalsIgnoreCase(destino) || RECOGIDO.equalsIgnoreCase(destino))) || CANCELADO.equalsIgnoreCase(destino)))
                || (esListoEnvio && (EN_CAMINO.equalsIgnoreCase(destino) || ENTREGADO.equalsIgnoreCase(destino) || CANCELADO.equalsIgnoreCase(destino)))
                || (esEnCamino && (ENTREGADO.equalsIgnoreCase(destino) || CANCELADO.equalsIgnoreCase(destino)))
                || (esListoRecoger && (RECOGIDO.equalsIgnoreCase(destino) || CANCELADO.equalsIgnoreCase(destino)))
                || (esEnCurso && (REALIZADA.equalsIgnoreCase(destino) || CANCELADO.equalsIgnoreCase(destino)))
                || (esSolicitudCancel && (CANCELADO.equalsIgnoreCase(destino) || CONFIRMADO.equalsIgnoreCase(destino) || PENDIENTE.equalsIgnoreCase(destino) || PENDIENTE_REPROGRAMACION.equalsIgnoreCase(destino) || DENEGADO.equalsIgnoreCase(destino)));

        if (!valida) throw new IllegalStateException("No se puede realizar esta transición de estado de '" + origen + "' a '" + destino + "'.");
    }

    public void validarTransicionLogistica(Reserva reserva, String destinoPedido) {
        String estado = reserva.getEstado();
        boolean esConfirmado = CONFIRMADO.equalsIgnoreCase(estado) || PAGO_CONFIRMADO.equalsIgnoreCase(estado) || "APROBADO".equalsIgnoreCase(reserva.getEstadoPago());
        if (!esConfirmado) {
            throw new IllegalStateException("El pedido no está confirmado o pagado, por lo que no puede iniciar su flujo logístico.");
        }

        String origenPedido = reserva.getEstadoPedido();
        if (origenPedido == null || origenPedido.isBlank()) {
            origenPedido = "PENDIENTE_PREPARACION";
        }
        if (destinoPedido.equalsIgnoreCase(origenPedido)) return; // Idempotencia

        boolean esDomicilio = reserva.esDomicilio();
        boolean esRecogida = !esDomicilio;

        // Reglas de exclusión según forma de entrega en pedidos
        if (esDomicilio && (LISTO_PARA_RECOGER.equalsIgnoreCase(destinoPedido) || RECOGIDO.equalsIgnoreCase(destinoPedido) || "LISTO_RECOGER".equalsIgnoreCase(destinoPedido) || "RECOGIDO".equalsIgnoreCase(destinoPedido))) {
            throw new IllegalStateException("Los pedidos con entrega a domicilio no pueden pasar a estados de recogida en local.");
        }
        if (esRecogida && (LISTO_ENVIO.equalsIgnoreCase(destinoPedido) || EN_CAMINO.equalsIgnoreCase(destinoPedido) || ENTREGADO.equalsIgnoreCase(destinoPedido) || "ENTREGADO".equalsIgnoreCase(destinoPedido))) {
            throw new IllegalStateException("Los pedidos para recoger en el local no pueden pasar a estados de envío o entrega a domicilio.");
        }

        boolean esPendiente = "PENDIENTE_PREPARACION".equalsIgnoreCase(origenPedido);
        boolean esEnPreparacion = EN_PREPARACION.equalsIgnoreCase(origenPedido) || "EN_PREPARACION".equalsIgnoreCase(origenPedido);
        boolean esListoEnvio = LISTO_ENVIO.equalsIgnoreCase(origenPedido) || "LISTO_ENVIO".equalsIgnoreCase(origenPedido);
        boolean esEnCamino = EN_CAMINO.equalsIgnoreCase(origenPedido) || "EN_CAMINO".equalsIgnoreCase(origenPedido);
        boolean esListoRecoger = LISTO_PARA_RECOGER.equalsIgnoreCase(origenPedido) || "LISTO_RECOGER".equalsIgnoreCase(origenPedido);

        boolean valida = (esPendiente && (EN_PREPARACION.equalsIgnoreCase(destinoPedido) || "EN_PREPARACION".equalsIgnoreCase(destinoPedido)))
                || (esEnPreparacion && ((esDomicilio && (LISTO_ENVIO.equalsIgnoreCase(destinoPedido) || "LISTO_ENVIO".equalsIgnoreCase(destinoPedido))) || (esRecogida && (LISTO_PARA_RECOGER.equalsIgnoreCase(destinoPedido) || "LISTO_RECOGER".equalsIgnoreCase(destinoPedido)))))
                || (esListoEnvio && (EN_CAMINO.equalsIgnoreCase(destinoPedido) || "EN_CAMINO".equalsIgnoreCase(destinoPedido)))
                || (esEnCamino && (ENTREGADO.equalsIgnoreCase(destinoPedido) || "ENTREGADO".equalsIgnoreCase(destinoPedido)))
                || (esListoRecoger && (RECOGIDO.equalsIgnoreCase(destinoPedido) || "RECOGIDO".equalsIgnoreCase(destinoPedido)));

        if (!valida) throw new IllegalStateException("No se puede realizar esta transición de estado de '" + origenPedido + "' a '" + destinoPedido + "'.");
    }



    private List<ItemReserva> normalizarItems(List<ItemReserva> enviados) {
        if (enviados == null || enviados.isEmpty()) throw new IllegalArgumentException("Debes seleccionar al menos un producto, kit o servicio.");
        Map<String, ItemReserva> consolidados = new LinkedHashMap<>();
        for (ItemReserva item : enviados) {
            if (item.getId() == null || item.getTipo() == null) throw new IllegalArgumentException("El artículo solicitado no es válido.");
            String tipo = item.getTipo().toLowerCase();
            if (!List.of("producto", "kit", "servicio").contains(tipo)) throw new IllegalArgumentException("El tipo de artículo no es válido.");
            int cantidad = item.getCantidad() == null ? 1 : item.getCantidad();
            if (cantidad < 1) throw new IllegalArgumentException("La cantidad solicitada no es válida.");
            String varKey = item.getVarianteId() != null ? item.getVarianteId().trim() : "";
            String clave = tipo + ":" + item.getId() + ":" + varKey;
            ItemReserva actual = consolidados.computeIfAbsent(clave, k -> {
                ItemReserva nuevo = new ItemReserva();
                nuevo.setId(item.getId());
                nuevo.setTipo(tipo);
                nuevo.setVarianteId(item.getVarianteId());
                nuevo.setVarianteNombre(item.getVarianteNombre());
                nuevo.setCantidad(0);
                return nuevo;
            });
            actual.setCantidad(actual.getCantidad() + cantidad);
        }
        return new ArrayList<>(consolidados.values());
    }

    private double precioActual(ItemReserva item) {
        return precioActual(item, true);
    }

    private double precioActual(ItemReserva item, boolean validarStock) {
        return switch (item.getTipo()) {
            case "producto" -> {
                Producto p = productos.findById(item.getId()).orElseThrow(() -> new IllegalArgumentException("El producto no existe."));
                if ("VARIANTES".equalsIgnoreCase(p.getTipoPrecio()) && p.getVariantes() != null && !p.getVariantes().isEmpty()) {
                    String varId = item.getVarianteId();
                    String varNom = item.getVarianteNombre();
                    if ((varId == null || varId.isBlank()) && (varNom == null || varNom.isBlank())) {
                        throw new IllegalArgumentException("Debes seleccionar una variante o modelo para el producto " + p.getNombre() + ".");
                    }
                    VarianteProducto v = p.getVariantes().stream()
                            .filter(it -> (varId != null && !varId.isBlank() && varId.equals(it.getId()))
                                    || (varNom != null && !varNom.isBlank() && varNom.equalsIgnoreCase(it.getNombre())))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("La variante seleccionada no existe para el producto " + p.getNombre() + "."));

                    if (Boolean.FALSE.equals(v.getActivo())) {
                        throw new IllegalArgumentException("La variante " + v.getNombre() + " se encuentra inactiva actualmente.");
                    }
                    if (validarStock) {
                        if (Boolean.FALSE.equals(v.getEnStock()) || (v.getCantidad() != null && v.getCantidad() < item.getCantidad())) {
                            int stockActual = v.getCantidad() != null ? v.getCantidad() : 0;
                            if (stockActual <= 0) {
                                throw new IllegalStateException("El producto acaba de agotarse. Actualizamos tu carrito.");
                            } else {
                                throw new IllegalStateException("Solo quedan " + stockActual + " unidades disponibles.");
                            }
                        }
                    }
                    item.setVarianteId(v.getId());
                    item.setVarianteNombre(v.getNombre());
                    item.setNombre(p.getNombre() + " (" + v.getNombre() + ")");
                    yield v.getPrecio();
                } else {
                    if (validarStock) {
                        if (Boolean.FALSE.equals(p.getEnStock()) || (p.getCantidad() != null && p.getCantidad() < item.getCantidad())) {
                            int stockActual = p.getCantidad() != null ? p.getCantidad() : 0;
                            if (stockActual <= 0) {
                                throw new IllegalStateException("El producto acaba de agotarse. Actualizamos tu carrito.");
                            } else {
                                throw new IllegalStateException("Solo quedan " + stockActual + " unidades disponibles.");
                            }
                        }
                    }
                    item.setNombre(p.getNombre());
                    item.setVarianteId(null);
                    item.setVarianteNombre(null);
                    yield p.getPrecio();
                }
            }
            case "kit" -> {
                Kit k = kits.findById(item.getId()).orElseThrow(() -> new IllegalArgumentException("El kit no existe."));
                if (validarStock) {
                    if (Boolean.FALSE.equals(k.getEnStock()) || (k.getCantidad() != null && k.getCantidad() < item.getCantidad())) {
                        int stockActual = k.getCantidad() != null ? k.getCantidad() : 0;
                        if (stockActual <= 0) {
                            throw new IllegalStateException("El producto acaba de agotarse. Actualizamos tu carrito.");
                        } else {
                            throw new IllegalStateException("Solo quedan " + stockActual + " unidades disponibles.");
                        }
                    }
                }
                item.setNombre(k.getNombre());
                yield k.getPrecio();
            }
            case "servicio" -> {
                Servicio s = servicios.findById(item.getId()).orElseThrow(() -> new IllegalArgumentException("El servicio no existe."));
                item.setNombre(s.getNombre());
                yield s.getPrecio();
            }
            default -> throw new IllegalArgumentException("El tipo de artículo no es válido.");
        };
    }

    private boolean descontarAtomico(ItemReserva item) {
        if ("producto".equals(item.getTipo())) {
            Producto p = productos.findById(item.getId()).orElse(null);
            if (p == null) return false;
            if ("VARIANTES".equalsIgnoreCase(p.getTipoPrecio()) && item.getVarianteId() != null && !item.getVarianteId().isBlank()) {
                VarianteProducto v = p.getVariantes().stream().filter(it -> it.getId().equals(item.getVarianteId())).findFirst().orElse(null);
                if (v == null) return false;
                int oldStock = v.getCantidad() != null ? v.getCantidad() : 0;
                int newStock = oldStock - item.getCantidad();

                // Intento 1: match exacto donde la cantidad es igual a lo solicitado (stock pasará a 0)
                Query queryExact = Query.query(Criteria.where("id").is(item.getId())
                        .and("variantes").elemMatch(Criteria.where("id").is(item.getVarianteId())
                                .and("cantidad").is(item.getCantidad())));
                Update updateToZero = new Update()
                        .inc("variantes.$.cantidad", -item.getCantidad())
                        .set("variantes.$.enStock", false)
                        .inc("cantidad", -item.getCantidad());
                if (mongo.updateFirst(queryExact, updateToZero, Producto.class).getModifiedCount() == 1) {
                    // Si el stock total del producto queda en <= 0, actualizar su enStock general
                    mongo.updateFirst(
                        Query.query(Criteria.where("id").is(item.getId()).and("cantidad").lte(0)),
                        new Update().set("enStock", false),
                        Producto.class
                    );
                    if (oldStock > 2 && newStock <= 2 && emailNotificationService != null) {
                        emailNotificationService.enviarAlertaStockBajo(p.getNombre(), v.getNombre(), newStock);
                    }
                    return true;
                }

                // Intento 2: decremento normal donde la cantidad es mayor a lo solicitado
                Query queryGreater = Query.query(Criteria.where("id").is(item.getId())
                        .and("variantes").elemMatch(Criteria.where("id").is(item.getVarianteId())
                                .and("cantidad").gt(item.getCantidad())));
                Update updateNormal = new Update()
                        .inc("variantes.$.cantidad", -item.getCantidad())
                        .inc("cantidad", -item.getCantidad());
                if (mongo.updateFirst(queryGreater, updateNormal, Producto.class).getModifiedCount() == 1) {
                    if (oldStock > 2 && newStock <= 2 && emailNotificationService != null) {
                        emailNotificationService.enviarAlertaStockBajo(p.getNombre(), v.getNombre(), newStock);
                    }
                    return true;
                }
                return false;
            } else {
                // Producto único
                int oldStock = p.getCantidad() != null ? p.getCantidad() : 0;
                int newStock = oldStock - item.getCantidad();

                Query queryExact = Query.query(Criteria.where("id").is(item.getId()).and("cantidad").is(item.getCantidad()));
                Update updateToZero = new Update().inc("cantidad", -item.getCantidad()).set("enStock", false);
                if (mongo.updateFirst(queryExact, updateToZero, Producto.class).getModifiedCount() == 1) {
                    if (oldStock > 2 && newStock <= 2 && emailNotificationService != null) {
                        emailNotificationService.enviarAlertaStockBajo(p.getNombre(), null, newStock);
                    }
                    return true;
                }

                Query queryGreater = Query.query(Criteria.where("id").is(item.getId()).and("cantidad").gt(item.getCantidad()));
                Update updateNormal = new Update().inc("cantidad", -item.getCantidad());
                if (mongo.updateFirst(queryGreater, updateNormal, Producto.class).getModifiedCount() == 1) {
                    if (oldStock > 2 && newStock <= 2 && emailNotificationService != null) {
                        emailNotificationService.enviarAlertaStockBajo(p.getNombre(), null, newStock);
                    }
                    return true;
                }
                return false;
            }
        } else {
            // Kit
            Kit k = kits.findById(item.getId()).orElse(null);
            if (k == null) return false;
            int oldStock = k.getCantidad() != null ? k.getCantidad() : 0;
            int newStock = oldStock - item.getCantidad();

            Query queryExact = Query.query(Criteria.where("id").is(item.getId()).and("cantidad").is(item.getCantidad()));
            Update updateToZero = new Update().inc("cantidad", -item.getCantidad()).set("enStock", false);
            if (mongo.updateFirst(queryExact, updateToZero, Kit.class).getModifiedCount() == 1) {
                if (oldStock > 2 && newStock <= 2 && emailNotificationService != null) {
                    emailNotificationService.enviarAlertaStockBajo(k.getNombre(), null, newStock);
                }
                return true;
            }

            Query queryGreater = Query.query(Criteria.where("id").is(item.getId()).and("cantidad").gt(item.getCantidad()));
            Update updateNormal = new Update().inc("cantidad", -item.getCantidad());
            if (mongo.updateFirst(queryGreater, updateNormal, Kit.class).getModifiedCount() == 1) {
                if (oldStock > 2 && newStock <= 2 && emailNotificationService != null) {
                    emailNotificationService.enviarAlertaStockBajo(k.getNombre(), null, newStock);
                }
                return true;
            }
            return false;
        }
    }

    private void sumarStock(ItemReserva item) {
        if ("producto".equals(item.getTipo())) {
            if (item.getVarianteId() != null && !item.getVarianteId().isBlank()) {
                Query query = Query.query(Criteria.where("id").is(item.getId()).and("variantes.id").is(item.getVarianteId()));
                mongo.updateFirst(query, new Update()
                        .inc("variantes.$.cantidad", item.getCantidad())
                        .set("variantes.$.enStock", true)
                        .inc("cantidad", item.getCantidad())
                        .set("enStock", true), Producto.class);
            } else {
                Query query = Query.query(Criteria.where("id").is(item.getId()));
                mongo.updateFirst(query, new Update().inc("cantidad", item.getCantidad()).set("enStock", true), Producto.class);
            }
        } else {
            Query query = Query.query(Criteria.where("id").is(item.getId()));
            mongo.updateFirst(query, new Update().inc("cantidad", item.getCantidad()).set("enStock", true), Kit.class);
        }
    }

    public synchronized void actualizarItemsDeReserva(Reserva reserva, List<ItemReserva> nuevosItems) {
        if (!Boolean.TRUE.equals(reserva.getInventarioReservado())) {
            reserva.setItemsInventario(normalizarItems(nuevosItems));
            reservarInventario(reserva);
            recalcularMontosYGuardar(reserva);
            return;
        }

        List<ItemReserva> oldItems = reserva.getItemsInventario();
        List<ItemReserva> normalizedNew = normalizarItems(nuevosItems);

        Map<String, Integer> oldQtys = new HashMap<>();
        Map<String, ItemReserva> oldItemObjs = new HashMap<>();
        for (ItemReserva item : oldItems) {
            if (!"servicio".equals(item.getTipo())) {
                String key = item.getId() + ":" + item.getTipo() + ":" + (item.getVarianteId() != null ? item.getVarianteId() : "");
                oldQtys.put(key, item.getCantidad());
                oldItemObjs.put(key, item);
            }
        }

        Map<String, Integer> newQtys = new HashMap<>();
        Map<String, ItemReserva> newItemObjs = new HashMap<>();
        for (ItemReserva item : normalizedNew) {
            if (!"servicio".equals(item.getTipo())) {
                String key = item.getId() + ":" + item.getTipo() + ":" + (item.getVarianteId() != null ? item.getVarianteId() : "");
                newQtys.put(key, item.getCantidad());
                newItemObjs.put(key, item);
            }
        }

        List<ItemReserva> reservadosEnEstaOp = new ArrayList<>();
        try {
            for (Map.Entry<String, Integer> entry : newQtys.entrySet()) {
                String key = entry.getKey();
                int newQty = entry.getValue();
                int oldQty = oldQtys.getOrDefault(key, 0);

                if (newQty > oldQty) {
                    int diff = newQty - oldQty;
                    ItemReserva template = newItemObjs.get(key);
                    ItemReserva diffItem = new ItemReserva(template.getId(), template.getTipo(), diff);
                    diffItem.setVarianteId(template.getVarianteId());
                    diffItem.setNombre(template.getNombre());
                    
                    if (!descontarAtomico(diffItem)) {
                        int stockActual = 0;
                        if ("producto".equals(template.getTipo())) {
                            Producto p = productos.findById(template.getId()).orElse(null);
                            if (p != null) {
                                if ("VARIANTES".equalsIgnoreCase(p.getTipoPrecio()) && template.getVarianteId() != null) {
                                    VarianteProducto v = p.getVariantes().stream().filter(it -> it.getId().equals(template.getVarianteId())).findFirst().orElse(null);
                                    stockActual = v != null ? v.getCantidad() : 0;
                                } else {
                                    stockActual = p.getCantidad() != null ? p.getCantidad() : 0;
                                }
                            }
                        } else {
                            Kit k = kits.findById(template.getId()).orElse(null);
                            if (k != null) {
                                stockActual = k.getCantidad() != null ? k.getCantidad() : 0;
                            }
                        }
                        throw new IllegalStateException("Solo quedan " + stockActual + " unidades disponibles para " + template.getNombre() + ".");
                    }
                    reservadosEnEstaOp.add(diffItem);
                }
            }
        } catch (RuntimeException ex) {
            for (ItemReserva diffItem : reservadosEnEstaOp) {
                sumarStock(diffItem);
            }
            throw ex;
        }

        for (Map.Entry<String, Integer> entry : oldQtys.entrySet()) {
            String key = entry.getKey();
            int oldQty = entry.getValue();
            int newQty = newQtys.getOrDefault(key, 0);

            if (newQty < oldQty) {
                int diff = oldQty - newQty;
                ItemReserva template = oldItemObjs.get(key);
                ItemReserva diffItem = new ItemReserva(template.getId(), template.getTipo(), diff);
                diffItem.setVarianteId(template.getVarianteId());
                diffItem.setNombre(template.getNombre());
                sumarStock(diffItem);
            }
        }

        reserva.setItemsInventario(normalizedNew);
        recalcularMontosYGuardar(reserva);
    }

    private void recalcularMontosYGuardar(Reserva reserva) {
        List<ItemReserva> items = reserva.getItemsInventario();
        boolean tieneServicios = items.stream().anyMatch(i -> "servicio".equals(i.getTipo()));
        List<String> nombres = new ArrayList<>();
        double total = 0;
        double anticipo = 0;
        for (ItemReserva item : items) {
            double precio = precioActual(item, false);
            item.setPrecioUnitario(precio);
            double sub = precio * item.getCantidad();
            item.setSubtotal(sub);
            total += sub;
            nombres.add(item.getNombre() + (item.getCantidad() > 1 ? " x" + item.getCantidad() : ""));
            
            if (tieneServicios) {
                if ("servicio".equals(item.getTipo())) {
                    anticipo += sub * 0.25;
                } else {
                    anticipo += sub;
                }
            }
        }
        reserva.setItems(nombres);
        reserva.setSubtotal(total);
        reserva.setAnticipo(anticipo);
        reserva.setSaldo(total - reserva.getAnticipo());
        reserva.setInventarioReservado(true);
        reservas.save(reserva);
    }
}
