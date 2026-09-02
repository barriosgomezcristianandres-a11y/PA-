package com.isivi.app.controller;

import com.isivi.app.model.ConfiguracionNegocio;
import com.isivi.app.repository.ConfiguracionNegocioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/configuracion")
@CrossOrigin(origins = "*")
public class ConfiguracionNegocioController {

    private final ConfiguracionNegocioRepository configuracionNegocioRepository;
    private final com.isivi.app.service.WhatsAppNotificationService whatsappService;

    public ConfiguracionNegocioController(ConfiguracionNegocioRepository configuracionNegocioRepository,
                                         com.isivi.app.service.WhatsAppNotificationService whatsappService) {
        this.configuracionNegocioRepository = configuracionNegocioRepository;
        this.whatsappService = whatsappService;
    }

    @GetMapping
    public ConfiguracionNegocio obtener() {
        return configuracionNegocioRepository.findById("principal").orElseGet(ConfiguracionNegocio::new);
    }

    @PutMapping
    public ResponseEntity<?> actualizar(@RequestBody ConfiguracionNegocio configuracion) {
        if (configuracion == null) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "La configuración no puede estar vacía."));
        }
        if (configuracion.getCiudad() == null || configuracion.getCiudad().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "La ciudad es obligatoria."));
        }
        if (configuracion.getPais() == null || configuracion.getPais().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El país es obligatorio."));
        }
        if (configuracion.getDiasAtencion() == null || configuracion.getDiasAtencion().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Los días de atención son obligatorios."));
        }
        if (configuracion.getHoraApertura() == null || configuracion.getHoraApertura().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "La hora de apertura es obligatoria."));
        }
        if (configuracion.getHoraCierre() == null || configuracion.getHoraCierre().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "La hora de cierre es obligatoria."));
        }

        // Validación y normalización de número de administración cuando está habilitado
        if (configuracion.isWhatsappAdminHabilitado()) {
            if (configuracion.getWhatsappAdminNumero() == null || configuracion.getWhatsappAdminNumero().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", "El número de WhatsApp del administrador es obligatorio si las notificaciones están habilitadas."));
            }
            String num = configuracion.getWhatsappAdminNumero().replaceAll("\\D", "");
            if (num.length() < 7 || num.length() > 15) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", "El número de WhatsApp del administrador no tiene una longitud válida (debe tener entre 7 y 15 dígitos sin letras ni espacios)."));
            }
            if (num.length() == 10) num = "57" + num;
            configuracion.setWhatsappAdminNumero("+" + num);
        }

        configuracion.setId("principal");
        configuracion.setCiudad(configuracion.getCiudad().trim());
        configuracion.setPais(configuracion.getPais().trim());
        configuracion.setDiasAtencion(configuracion.getDiasAtencion().trim());
        configuracion.setHoraApertura(configuracion.getHoraApertura().trim());
        configuracion.setHoraCierre(configuracion.getHoraCierre().trim());
        if (configuracion.getTelefonoMayorista() != null) {
            configuracion.setTelefonoMayorista(configuracion.getTelefonoMayorista().trim());
        }
        if (configuracion.getTituloSitio() != null) configuracion.setTituloSitio(configuracion.getTituloSitio().trim());
        if (configuracion.getEslogan() != null) configuracion.setEslogan(configuracion.getEslogan().trim());
        if (configuracion.getDescripcion() != null) configuracion.setDescripcion(configuracion.getDescripcion().trim());
        if (configuracion.getTituloHero() != null) configuracion.setTituloHero(configuracion.getTituloHero().trim());
        if (configuracion.getDescripcionHero() != null) configuracion.setDescripcionHero(configuracion.getDescripcionHero().trim());
        if (configuracion.getTextoBotonHero() != null) configuracion.setTextoBotonHero(configuracion.getTextoBotonHero().trim());
        if (configuracion.getNombreComercial() != null) configuracion.setNombreComercial(configuracion.getNombreComercial().trim());
        if (configuracion.getWhatsapp() != null) configuracion.setWhatsapp(configuracion.getWhatsapp().trim());
        if (configuracion.getDireccion() != null) configuracion.setDireccion(configuracion.getDireccion().trim());
        if (configuracion.getHorarioAtencion() != null) configuracion.setHorarioAtencion(configuracion.getHorarioAtencion().trim());
        if (configuracion.getMensajeWhatsApp() != null) configuracion.setMensajeWhatsApp(configuracion.getMensajeWhatsApp().trim());
        if (configuracion.getTextoFooter() != null) configuracion.setTextoFooter(configuracion.getTextoFooter().trim());
        if (configuracion.getNumeroBancolombia() != null) configuracion.setNumeroBancolombia(configuracion.getNumeroBancolombia().trim());
        if (configuracion.getTitularBancolombia() != null) configuracion.setTitularBancolombia(configuracion.getTitularBancolombia().trim());
        if (configuracion.getNumeroNequi() != null) configuracion.setNumeroNequi(configuracion.getNumeroNequi().trim());
        if (configuracion.getTitularNequi() != null) configuracion.setTitularNequi(configuracion.getTitularNequi().trim());
        if (configuracion.getNumeroDaviplata() != null) configuracion.setNumeroDaviplata(configuracion.getNumeroDaviplata().trim());
        if (configuracion.getTitularDaviplata() != null) configuracion.setTitularDaviplata(configuracion.getTitularDaviplata().trim());

        ConfiguracionNegocio guardada = configuracionNegocioRepository.save(configuracion);
        return ResponseEntity.ok(guardada);
    }

    @PostMapping("/whatsapp-test")
    public ResponseEntity<?> enviarMensajePrueba() {
        ConfiguracionNegocio cfg = configuracionNegocioRepository.findById("principal").orElseGet(ConfiguracionNegocio::new);
        if (!cfg.isWhatsappAdminHabilitado() || cfg.getWhatsappAdminNumero().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Las notificaciones de WhatsApp para administradores no están habilitadas o el número está vacío."));
        }
        if (!whatsappService.credencialesValidas()) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
                    .body(Map.of("error", "CONFIG_MISSING", "mensaje", "No fue posible enviar el mensaje. Las credenciales de WhatsApp de Meta no están configuradas en el servidor."));
        }
        try {
            String testMsg = "🧪 PRUEBA DE WHATSAPP — ISIVI\n\nEste mensaje confirma que las notificaciones administrativas están correctamente configuradas.";
            whatsappService.enviarMensajeAdmin(testMsg, cfg.getWhatsappAdminNumero());
            return ResponseEntity.ok(Map.of("ok", true, "mensaje", "Mensaje de prueba enviado correctamente."));
        } catch (org.springframework.web.client.RestClientResponseException ex) {
            String body = ex.getResponseBodyAsString();
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of(
                            "ok", false,
                            "error", "WHATSAPP_META_ERROR",
                            "mensaje", "Error de Meta (HTTP " + ex.getStatusCode().value() + "): " + (body.isEmpty() ? ex.getMessage() : body)
                    ));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", "FAIL_SEND", "mensaje", "No fue posible enviar el mensaje. " + ex.getMessage()));
        }
    }
}
