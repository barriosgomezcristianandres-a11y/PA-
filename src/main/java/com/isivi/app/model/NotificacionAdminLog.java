package com.isivi.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "notificaciones_whatsapp_log")
public class NotificacionAdminLog {
    @Id
    private String id;

    @Indexed(unique = true)
    private String notifKey;

    private String tipo;
    private String entityId;
    private String destinatario;
    private String mensaje;
    private String estado; // PENDING, SENDING, SENT, ERROR, FAILED
    private int intentos;
    private String ultimoError;
    private Instant proximoIntentoEn;
    private Instant creadoEn;
    private Instant actualizadoEn;
    
    private String asunto;

    @Indexed(expireAfterSeconds = 0) // TTL dinámico basado en expirarEn
    private Instant expirarEn;

    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }

    public NotificacionAdminLog() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNotifKey() { return notifKey; }
    public void setNotifKey(String notifKey) { this.notifKey = notifKey; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getIntentos() { return intentos; }
    public void setIntentos(int intentos) { this.intentos = intentos; }

    public String getUltimoError() { return ultimoError; }
    public void setUltimoError(String ultimoError) { this.ultimoError = ultimoError; }

    public Instant getProximoIntentoEn() { return proximoIntentoEn; }
    public void setProximoIntentoEn(Instant proximoIntentoEn) { this.proximoIntentoEn = proximoIntentoEn; }

    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }

    public Instant getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(Instant actualizadoEn) { this.actualizadoEn = actualizadoEn; }

    public Instant getExpirarEn() { return expirarEn; }
    public void setExpirarEn(Instant expirarEn) { this.expirarEn = expirarEn; }
}
