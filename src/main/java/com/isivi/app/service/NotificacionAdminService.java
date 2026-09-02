package com.isivi.app.service;

import com.isivi.app.model.ConfiguracionNegocio;
import com.isivi.app.model.NotificacionAdminLog;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.ConfiguracionNegocioRepository;
import com.isivi.app.repository.NotificacionAdminLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executor;

@Service
public class NotificacionAdminService {
    private static final Logger logger = LoggerFactory.getLogger(NotificacionAdminService.class);

    private final NotificacionAdminLogRepository repository;
    private final ConfiguracionNegocioRepository configRepository;
    private final WhatsAppNotificationService whatsappService;
    private final MongoTemplate mongoTemplate;
    private final EmailNotificationService emailNotificationService;

    public NotificacionAdminService(NotificacionAdminLogRepository repository,
                                    ConfiguracionNegocioRepository configRepository,
                                    WhatsAppNotificationService whatsappService,
                                    MongoTemplate mongoTemplate,
                                    @org.springframework.context.annotation.Lazy EmailNotificationService emailNotificationService) {
        this.repository = repository;
        this.configRepository = configRepository;
        this.whatsappService = whatsappService;
        this.mongoTemplate = mongoTemplate;
        this.emailNotificationService = emailNotificationService;
    }

    public void registrarNuevaCita(Reserva r) {
        String notifKey = "NUEVA_CITA:" + r.getId();
        String total = r.getSubtotal() != null ? String.valueOf(r.getSubtotal()) : "0";
        String msg = "🔔 NUEVA CITA — ISIVI\n\n"
                   + "Cliente: " + r.getNombreCliente() + "\n"
                   + "Cita: " + r.getFechaCita() + " - " + r.getHoraCita() + "\n"
                   + "Total: $" + total + "\n"
                   + "Pago: " + r.getEstadoPago() + "\n\n"
                   + "Revisa la cita en el panel administrativo.";
        registrar(notifKey, "NUEVA_CITA", r.getId(), msg);
    }

    public void registrarNuevaCompra(Reserva r) {
        String notifKey = "NUEVA_COMPRA:" + r.getId();
        String total = r.getSubtotal() != null ? String.valueOf(r.getSubtotal()) : "0";
        String metodo = r.getMetodoEntrega();
        String msg = "🛒 NUEVA COMPRA — ISIVI\n\n"
                   + "Cliente: " + r.getNombreCliente() + "\n"
                   + "Total: $" + total + "\n"
                   + "Entrega: " + metodo + "\n"
                   + "Pago: " + r.getEstadoPago() + "\n\n"
                   + "Revisa el pedido en el panel administrativo.";
        registrar(notifKey, "NUEVA_COMPRA", r.getId(), msg);
    }

    public void registrarSolicitudReprogramacion(Reserva r) {
        String propFecha = r.getFechaPropuestaReprogramacion() != null ? r.getFechaPropuestaReprogramacion().toString() : "";
        String propHora = r.getHoraPropuestaReprogramacion() != null ? r.getHoraPropuestaReprogramacion().replace(":", "") : "";
        String notifKey = "SOLICITUD_REPROGRAMACION:" + r.getId() + ":" + propFecha + "_" + propHora;
        String msg = "🔄 REPROGRAMACIÓN SOLICITADA — ISIVI\n\n"
                   + "Cliente: " + r.getNombreCliente() + "\n"
                   + "Cita actual: " + r.getFechaCita() + " - " + r.getHoraCita() + "\n"
                   + "Nueva propuesta: " + r.getFechaPropuestaReprogramacion() + " a las " + r.getHoraPropuestaReprogramacion() + "\n\n"
                   + "Requiere aprobación administrativa.";
        registrar(notifKey, "SOLICITUD_REPROGRAMACION", r.getId(), msg);
    }

    public void registrarSolicitudCancelacion(Reserva r) {
        String notifKey = "SOLICITUD_CANCELACION:" + r.getId();
        String msg = "⚠️ CANCELACIÓN SOLICITADA — ISIVI\n\n"
                   + "Cliente: " + r.getNombreCliente() + "\n"
                   + "Reserva: " + r.getCodigoReserva() + "\n"
                   + "Fecha: " + r.getFechaCita() + "\n\n"
                   + "Requiere acción administrativa.";
        registrar(notifKey, "SOLICITUD_CANCELACION", r.getId(), msg);
    }

    public void registrarPagoAprobado(String referenciaWompi, Reserva r) {
        String notifKey = "PAGO_APROBADO:" + referenciaWompi;
        String total = r.getSubtotal() != null ? String.valueOf(r.getSubtotal()) : "0";
        String msg = "💰 PAGO APROBADO WOMPI — ISIVI\n\n"
                   + "Reserva: " + r.getCodigoReserva() + "\n"
                   + "Cliente: " + r.getNombreCliente() + "\n"
                   + "Monto: $" + total + "\n"
                   + "Referencia: " + referenciaWompi;
        registrar(notifKey, "PAGO_APROBADO", r.getId(), msg);
    }

    public void registrarPagoRechazado(String referenciaWompi, Reserva r) {
        String notifKey = "PAGO_RECHAZADO:" + referenciaWompi;
        String total = r.getSubtotal() != null ? String.valueOf(r.getSubtotal()) : "0";
        String msg = "❌ PAGO RECHAZADO WOMPI — ISIVI\n\n"
                   + "Reserva: " + r.getCodigoReserva() + "\n"
                   + "Cliente: " + r.getNombreCliente() + "\n"
                   + "Monto: $" + total + "\n"
                   + "Referencia: " + referenciaWompi;
        registrar(notifKey, "PAGO_RECHAZADO", r.getId(), msg);
    }

    public void registrarConflictoPago(Reserva r, String motivo) {
        String notifKey = "CONFLICTO_PAGO:" + r.getId();
        String msg = "🚨 CONFLICTO DE PAGO — ISIVI\n\n"
                   + "Reserva: " + r.getCodigoReserva() + "\n"
                   + "Cliente: " + r.getNombreCliente() + "\n"
                   + "Motivo: " + motivo + "\n\n"
                   + "Requiere intervención administrativa inmediata.";
        registrar(notifKey, "CONFLICTO_PAGO", r.getId(), msg);
    }

    public void registrarAtencionAhora(Reserva r, String tipo) {
        String notifKey = "ATENCION_AHORA:" + r.getId() + ":" + tipo;
        String msg = "📢 ISIVI REQUIERE TU ATENCIÓN\n\n"
                   + "Reserva: " + r.getCodigoReserva() + "\n"
                   + "Cliente: " + r.getNombreCliente() + "\n"
                   + "Alerta: " + tipo;
        registrar(notifKey, "ATENCION_AHORA", r.getId(), msg);
    }

    private void registrar(String notifKey, String tipo, String entityId, String mensaje) {
        ConfiguracionNegocio cfg = configRepository.findById("principal").orElseGet(ConfiguracionNegocio::new);
        if (!cfg.isWhatsappAdminHabilitado() || cfg.getWhatsappAdminNumero().isBlank()) {
            return;
        }

        boolean habilitado = switch (tipo) {
            case "NUEVA_CITA" -> cfg.isNotificarNuevaCita();
            case "NUEVA_COMPRA" -> cfg.isNotificarNuevaCompra();
            case "SOLICITUD_REPROGRAMACION" -> cfg.isNotificarSolicitudReprogramacion();
            case "SOLICITUD_CANCELACION" -> cfg.isNotificarSolicitudCancelacion();
            case "PAGO_APROBADO" -> cfg.isNotificarPagoAprobado();
            case "PAGO_RECHAZADO" -> cfg.isNotificarPagoRechazado();
            case "CONFLICTO_PAGO" -> cfg.isNotificarConflictoPago();
            case "ATENCION_AHORA" -> cfg.isNotificarAtencionAhora();
            default -> false;
        };

        if (!habilitado) {
            return;
        }

        NotificacionAdminLog entry = new NotificacionAdminLog();
        entry.setNotifKey(notifKey);
        entry.setTipo(tipo);
        entry.setEntityId(entityId);
        entry.setDestinatario(cfg.getWhatsappAdminNumero());
        entry.setMensaje(mensaje);
        entry.setEstado("PENDING");
        entry.setIntentos(0);
        entry.setCreadoEn(Instant.now());
        entry.setActualizadoEn(Instant.now());
        entry.setProximoIntentoEn(Instant.now());
        entry.setExpirarEn(Instant.now().plus(90, ChronoUnit.DAYS));

        try {
            mongoTemplate.insert(entry);
            logger.info("[WHATSAPP ADMIN] Registrada notificación {} en Outbox.", notifKey);
            despacharAsincronamente(entry.getNotifKey());
        } catch (DuplicateKeyException ex) {
            logger.info("[WHATSAPP ADMIN] Duplicado evitado para la notifKey: {}.", notifKey);
        } catch (Exception ex) {
            logger.error("[WHATSAPP ADMIN] Error registrando notificación: {}", ex.getMessage(), ex);
        }
    }

    @Async("whatsappAdminTaskExecutor")
    public void despacharAsincronamente(String notifKey) {
        procesarEnvio(notifKey);
    }

    private void procesarEnvio(String notifKey) {
        Query query = new Query(Criteria.where("notifKey").is(notifKey).and("estado").is("PENDING"));
        Update update = new Update().set("estado", "SENDING").set("actualizadoEn", Instant.now());
        NotificacionAdminLog log = mongoTemplate.findAndModify(query, update, NotificacionAdminLog.class);
        if (log == null) return;

        int nuevosIntentos = log.getIntentos() + 1;
        try {
            whatsappService.enviarMensajeAdmin(log.getMensaje(), log.getDestinatario());
            Query updateQuery = new Query(Criteria.where("id").is(log.getId()));
            Update updateSent = new Update()
                    .set("estado", "SENT")
                    .set("intentos", nuevosIntentos)
                    .set("actualizadoEn", Instant.now());
            mongoTemplate.updateFirst(updateQuery, updateSent, NotificacionAdminLog.class);
            logger.info("[WHATSAPP ADMIN] Notificación {} enviada exitosamente.", notifKey);
        } catch (org.springframework.web.client.RestClientResponseException ex) {
            int statusCode = ex.getStatusCode().value();
            boolean permanente = (statusCode == 400 || statusCode == 401 || statusCode == 403 || statusCode == 404);
            registrarFallo(log, ex.getMessage(), nuevosIntentos, permanente, ex.getResponseHeaders() != null ? ex.getResponseHeaders().getFirst("Retry-After") : null);
        } catch (Exception ex) {
            registrarFallo(log, ex.getMessage(), nuevosIntentos, false, null);
        }
    }

    private void registrarFallo(NotificacionAdminLog log, String error, int intentos, boolean permanente, String retryAfterHeader) {
        Query query = new Query(Criteria.where("id").is(log.getId()));
        Update update = new Update().set("ultimoError", error).set("actualizadoEn", Instant.now());

        if (permanente || intentos >= 3) {
            update.set("estado", "FAILED");
            update.set("intentos", intentos);
            logger.error("[WHATSAPP ADMIN] Envío fallido de forma permanente para {}: {}", log.getNotifKey(), error);
        } else {
            update.set("estado", "ERROR");
            update.set("intentos", intentos);
            long delaySegundos = (intentos == 1) ? 60 : 300;
            if (retryAfterHeader != null) {
                try {
                    delaySegundos = Long.parseLong(retryAfterHeader);
                } catch (Exception e) {
                    // ignore
                }
            }
            update.set("proximoIntentoEn", Instant.now().plus(delaySegundos, ChronoUnit.SECONDS));
            logger.warn("[WHATSAPP ADMIN] Error temporal enviando {}. Intento {}/3. Reintento programado en {} seg.", log.getNotifKey(), intentos, delaySegundos);
        }
        mongoTemplate.updateFirst(query, update, NotificacionAdminLog.class);
    }

    @Scheduled(fixedDelay = 300000) // Cada 5 minutos (300.000 ms)
    public void ejecutarSchedulerRecuperacion() {
        logger.info("[OUTBOX SCHEDULER] Iniciando pasada de recuperación de contingencia...");

        Instant diezMinutosAtras = Instant.now().minus(10, ChronoUnit.MINUTES);
        Query sendingColgadosQuery = new Query(Criteria.where("estado").is("SENDING").and("actualizadoEn").lt(diezMinutosAtras));
        Update resetPending = new Update().set("estado", "PENDING").set("actualizadoEn", Instant.now());
        long resetCount = mongoTemplate.updateMulti(sendingColgadosQuery, resetPending, NotificacionAdminLog.class).getModifiedCount();
        if (resetCount > 0) {
            logger.warn("[OUTBOX SCHEDULER] Recuperados {} registros en SENDING huérfanos.", resetCount);
        }

        Query reintentarQuery = new Query(Criteria.where("estado").in("PENDING", "ERROR")
                .and("proximoIntentoEn").lte(Instant.now())
                .and("intentos").lt(3));

        List<NotificacionAdminLog> listos = mongoTemplate.find(reintentarQuery, NotificacionAdminLog.class);
        for (NotificacionAdminLog logEntry : listos) {
            Query claimQuery = new Query(Criteria.where("id").is(logEntry.getId()).and("estado").in("PENDING", "ERROR"));
            Update claimUpdate = new Update().set("estado", "SENDING").set("actualizadoEn", Instant.now());
            NotificacionAdminLog claimed = mongoTemplate.findAndModify(claimQuery, claimUpdate, NotificacionAdminLog.class);
            if (claimed != null) {
                try {
                    if (claimed.getTipo().startsWith("EMAIL_")) {
                        emailNotificationService.despacharEmailReclamado(claimed);
                    } else if (claimed.getTipo().startsWith("WHATSAPP_CLIENTE_")) {
                        whatsappService.despacharWhatsAppReclamado(claimed);
                    } else {
                        procesarEnvioReclamado(claimed);
                    }
                } catch (Exception ex) {
                    logger.error("[OUTBOX SCHEDULER] Error inesperado en reintento de {}: {}", logEntry.getNotifKey(), ex.getMessage());
                }
            }
        }
    }

    private void procesarEnvioReclamado(NotificacionAdminLog log) {
        int nuevosIntentos = log.getIntentos() + 1;
        try {
            whatsappService.enviarMensajeAdmin(log.getMensaje(), log.getDestinatario());
            Query updateQuery = new Query(Criteria.where("id").is(log.getId()));
            Update updateSent = new Update()
                    .set("estado", "SENT")
                    .set("intentos", nuevosIntentos)
                    .set("actualizadoEn", Instant.now());
            mongoTemplate.updateFirst(updateQuery, updateSent, NotificacionAdminLog.class);
            logger.info("[OUTBOX SCHEDULER] Notificación {} reintentada y enviada exitosamente.", log.getNotifKey());
        } catch (org.springframework.web.client.RestClientResponseException ex) {
            int statusCode = ex.getStatusCode().value();
            boolean permanente = (statusCode == 400 || statusCode == 401 || statusCode == 403 || statusCode == 404);
            registrarFallo(log, ex.getMessage(), nuevosIntentos, permanente, ex.getResponseHeaders() != null ? ex.getResponseHeaders().getFirst("Retry-After") : null);
        } catch (Exception ex) {
            registrarFallo(log, ex.getMessage(), nuevosIntentos, false, null);
        }
    }
}
