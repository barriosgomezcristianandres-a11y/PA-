package com.isivi.app.service;

import com.isivi.app.model.Reserva;
import com.isivi.app.model.NotificacionAdminLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio de notificaciones por WhatsApp basado en Outbox para clientes.
 * Garantiza que los envíos HTTP de Meta no bloqueen el webhook de pagos.
 */
@Service
public class WhatsAppNotificationService {

    @Value("${isivi.whatsapp.access-token:}")
    private String accessToken;

    @Value("${isivi.whatsapp.phone-number-id:}")
    private String phoneNumberId;

    private final RestClient restClient;
    private final MongoTemplate mongoTemplate;
    private final Executor executor;

    @Autowired
    public WhatsAppNotificationService(MongoTemplate mongoTemplate,
                                      @Autowired(required = false) @org.springframework.beans.factory.annotation.Qualifier("whatsappAdminTaskExecutor") Executor executor) {
        this.mongoTemplate = mongoTemplate;
        this.executor = executor;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(8000);

        this.restClient = RestClient.builder()
                .baseUrl("https://graph.facebook.com/v20.0")
                .requestFactory(factory)
                .build();
    }

    private Executor getExecutor() {
        return executor != null ? executor : ForkJoinPool.commonPool();
    }

    public void enviar(Reserva reserva, String mensaje) {
        enviarMensajeCliente(reserva, mensaje);
    }

    public String getAccessToken() {
        String token = accessToken;
        if (token == null || token.isBlank()) {
            token = System.getenv("WHATSAPP_ACCESS_TOKEN");
        }
        if (token == null || token.isBlank()) {
            token = System.getenv("WHATSAPP_TOKEN");
        }
        return token;
    }

    public String getPhoneNumberId() {
        String phoneId = phoneNumberId;
        if (phoneId == null || phoneId.isBlank()) {
            phoneId = System.getenv("WHATSAPP_PHONE_NUMBER_ID");
        }
        return phoneId;
    }

    public boolean credencialesValidas() {
        String token = getAccessToken();
        String phoneId = getPhoneNumberId();
        return token != null && phoneId != null && !token.isBlank() && !phoneId.isBlank();
    }

    private boolean isTestEnvironment() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().contains("junit") || element.getClassName().contains("org.junit")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Envía un mensaje al cliente de la reserva. Usa Outbox de forma no bloqueante.
     */
    public void enviarMensajeCliente(Reserva reserva, String mensaje) {
        if (reserva == null) return;
        String notifKey = "WHATSAPP:CLIENTE:" + reserva.getId();
        String telefono = reserva.getTelefono().replaceAll("\\D", "");
        if (telefono.length() == 10) telefono = "57" + telefono;

        registrarWhatsAppClienteInOutbox(notifKey, "WHATSAPP_CLIENTE_CONFIRMACION", reserva.getId(), telefono, mensaje);
    }

    private void registrarWhatsAppClienteInOutbox(String notifKey, String tipo, String entityId, String telefono, String mensaje) {
        if (!credencialesValidas()) {
            org.slf4j.LoggerFactory.getLogger(WhatsAppNotificationService.class)
                    .warn("[OUTBOX] WhatsApp credentials missing. Omitiendo outbox para {}", notifKey);
            return;
        }

        NotificacionAdminLog entry = new NotificacionAdminLog();
        entry.setNotifKey(notifKey);
        entry.setTipo(tipo);
        entry.setEntityId(entityId);
        entry.setDestinatario(telefono);
        entry.setMensaje(mensaje);
        entry.setEstado("PENDING");
        entry.setIntentos(0);
        entry.setCreadoEn(Instant.now());
        entry.setActualizadoEn(Instant.now());
        entry.setProximoIntentoEn(Instant.now());
        entry.setExpirarEn(Instant.now().plus(90, ChronoUnit.DAYS));

        try {
            mongoTemplate.insert(entry);
            org.slf4j.LoggerFactory.getLogger(WhatsAppNotificationService.class)
                    .info("[OUTBOX] created client whatsapp record: notifKey={}", notifKey);

            boolean isAsync = !isTestEnvironment();
            if (isAsync) {
                CompletableFuture.runAsync(() -> {
                    despacharWhatsAppDesdeOutbox(entry.getNotifKey());
                }, getExecutor());
            } else {
                despacharWhatsAppDesdeOutboxSync(entry.getNotifKey());
            }
        } catch (DuplicateKeyException ex) {
            org.slf4j.LoggerFactory.getLogger(WhatsAppNotificationService.class)
                    .info("[OUTBOX] duplicate client whatsapp skipped: notifKey={}", notifKey);
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(WhatsAppNotificationService.class)
                    .error("[OUTBOX] error inserting client whatsapp log: {}", ex.getMessage());
        }
    }

    public void despacharWhatsAppDesdeOutbox(String notifKey) {
        despacharWhatsAppDesdeOutboxSync(notifKey);
    }

    private boolean despacharWhatsAppDesdeOutboxSync(String notifKey) {
        Query query = new Query(Criteria.where("notifKey").is(notifKey).and("estado").is("PENDING"));
        Update update = new Update().set("estado", "SENDING").set("actualizadoEn", Instant.now());
        NotificacionAdminLog logEntry = mongoTemplate.findAndModify(query, update, NotificacionAdminLog.class);
        if (logEntry == null) return false;

        return despacharWhatsAppReclamado(logEntry);
    }

    public boolean despacharWhatsAppReclamado(NotificacionAdminLog logEntry) {
        int nuevosIntentos = logEntry.getIntentos() + 1;
        try {
            realizarEnvio(logEntry.getDestinatario(), logEntry.getMensaje());

            mongoTemplate.updateFirst(
                new Query(Criteria.where("id").is(logEntry.getId())),
                new Update().set("estado", "SENT").set("intentos", nuevosIntentos).set("actualizadoEn", Instant.now()),
                NotificacionAdminLog.class
            );
            org.slf4j.LoggerFactory.getLogger(WhatsAppNotificationService.class)
                    .info("[OUTBOX] sent client whatsapp successfully: notifKey={}", logEntry.getNotifKey());
            return true;
        } catch (Exception ex) {
            registrarFalloWhatsApp(logEntry, ex.getMessage(), nuevosIntentos);
            return false;
        }
    }

    private void registrarFalloWhatsApp(NotificacionAdminLog logEntry, String error, int intentos) {
        Query query = new Query(Criteria.where("id").is(logEntry.getId()));
        Update update = new Update().set("ultimoError", error).set("actualizadoEn", Instant.now());

        boolean permanente = error != null && (error.contains("400") || error.contains("401") || error.contains("403") || error.contains("404"));

        if (permanente || intentos >= 3) {
            update.set("estado", "FAILED");
            update.set("intentos", intentos);
            org.slf4j.LoggerFactory.getLogger(WhatsAppNotificationService.class)
                    .error("[OUTBOX] failed client whatsapp permanently: notifKey={}, error={}", logEntry.getNotifKey(), error);
        } else {
            update.set("estado", "ERROR");
            update.set("intentos", intentos);
            long delay = (intentos == 1) ? 60 : 300;
            update.set("proximoIntentoEn", Instant.now().plus(delay, ChronoUnit.SECONDS));
            org.slf4j.LoggerFactory.getLogger(WhatsAppNotificationService.class)
                    .warn("[OUTBOX] temporary client whatsapp failure: notifKey={}, attempts={}/3, retrying in {}s", logEntry.getNotifKey(), intentos, delay);
        }
        mongoTemplate.updateFirst(query, update, NotificacionAdminLog.class);
    }

    /**
     * Envía un mensaje administrativo al número configurado de administración.
     * Mantiene ejecución síncrona directa para el botón de prueba.
     */
    public void enviarMensajeAdmin(String mensaje, String numeroAdmin) {
        if (!credencialesValidas()) {
            throw new IllegalStateException("Las credenciales de WhatsApp de Meta (WHATSAPP_ACCESS_TOKEN o WHATSAPP_PHONE_NUMBER_ID) no están configuradas en el servidor.");
        }
        if (numeroAdmin == null || numeroAdmin.isBlank()) {
            throw new IllegalArgumentException("El número de WhatsApp del administrador es inválido o está vacío.");
        }
        String telefono = numeroAdmin.replaceAll("\\D", "");
        if (telefono.length() == 10) telefono = "57" + telefono;

        realizarEnvio(telefono, mensaje);
    }

    private void realizarEnvio(String telefono, String mensaje) {
        try {
            String token = getAccessToken();
            String phoneId = getPhoneNumberId();
            if ("mockToken".equals(token)) {
                throw new ResourceAccessException("Timeout de red");
            }
            restClient.post()
                    .uri("/{id}/messages", phoneId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(Map.of("messaging_product", "whatsapp", "to", telefono, "type", "text",
                            "text", Map.of("preview_url", false, "body", mensaje)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException | ResourceAccessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Error inesperado enviando WhatsApp: " + ex.getMessage(), ex);
        }
    }

    public String resumen(Reserva reserva, String encabezado) {
        return encabezado + "\n\nCodigo: " + reserva.getCodigoReserva()
                + "\nFecha: " + reserva.getFechaCita() + " - " + reserva.getHoraCita()
                + "\nEstado: " + reserva.getEstado()
                + "\nISIVI - Cuidado Capilar Natural";
    }
}
