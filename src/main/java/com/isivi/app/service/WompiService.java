package com.isivi.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Iterator;

/** Genera firmas para Checkout y valida los eventos firmados por Wompi. */
@Service
public class WompiService {
    @Value("${isivi.wompi.public-key:}") private String llavePublica;
    @Value("${isivi.wompi.integrity-secret:}") private String secretoIntegridad;
    @Value("${isivi.wompi.events-secret:}") private String secretoEventos;
    @Value("${isivi.wompi.redirect-url:}") private String urlRedireccion;
    @Value("${isivi.wompi.sandbox:true}") private boolean sandbox;

    public boolean configurado() { return !llavePublica().isBlank() && !secretoIntegridad().isBlank() && !secretoEventos().isBlank(); }
    public String llavePublica() { return llavePublica == null ? "" : llavePublica.trim(); }
    public String secretoIntegridad() { return secretoIntegridad == null ? "" : secretoIntegridad.trim(); }
    public String secretoEventos() { return secretoEventos == null ? "" : secretoEventos.trim(); }
    public String urlRedireccion() { return urlRedireccion == null ? "" : urlRedireccion.trim(); }
    public boolean sandbox() { return sandbox; }

    public String firmaIntegridad(String referencia, long montoCentavos) {
        exigirConfiguracion();
        return sha256(referencia.trim() + montoCentavos + "COP" + secretoIntegridad());
    }

    /** Implementa signature.properties + timestamp + secreto de eventos, según Wompi. */
    public boolean eventoAutentico(JsonNode evento, String checksumCabecera) {
        exigirConfiguracion();
        JsonNode signature = evento.path("signature");
        String checksum = checksumCabecera == null || checksumCabecera.isBlank() ? signature.path("checksum").asText() : checksumCabecera;
        if (checksum.isBlank() || !signature.path("properties").isArray() || !evento.has("timestamp")) return false;
        StringBuilder valores = new StringBuilder();
        Iterator<JsonNode> propiedades = signature.path("properties").elements();
        while (propiedades.hasNext()) {
            JsonNode valor = valorEn(evento.path("data"), propiedades.next().asText());
            if (valor == null || valor.isMissingNode() || valor.isNull()) return false;
            valores.append(valor.asText());
        }
        valores.append(evento.path("timestamp").asLong()).append(secretoEventos());
        return MessageDigest.isEqual(sha256(valores.toString()).getBytes(StandardCharsets.UTF_8), checksum.toLowerCase().getBytes(StandardCharsets.UTF_8));
    }

    private JsonNode valorEn(JsonNode nodo, String ruta) {
        for (String parte : ruta.split("\\.")) nodo = nodo.path(parte);
        return nodo;
    }
    private void exigirConfiguracion() {
        if (!configurado()) throw new IllegalStateException("Wompi Sandbox no está configurado. Define las variables WOMPI_PUBLIC_KEY, WOMPI_INTEGRITY_SECRET y WOMPI_EVENTS_SECRET.");
    }
    private String sha256(String valor) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception ex) { throw new IllegalStateException("No se pudo generar la firma de Wompi.", ex); }
    }
}
