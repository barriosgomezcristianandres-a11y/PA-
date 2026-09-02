package com.isivi.app.controller;

import com.isivi.app.service.EmailNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoint de diagnóstico técnico de correo transaccional Brevo / SMTP.
 * Exclusivo para administradores autenticados con JWT.
 * No interactúa con reservas, inventario ni pasarelas de pago.
 */
@RestController
@RequestMapping("/api/admin/email")
@CrossOrigin(origins = "*")
public class AdminEmailDiagnosticController {

    private final EmailNotificationService emailNotificationService;

    public AdminEmailDiagnosticController(@Autowired(required = false) EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @PostMapping("/test")
    public ResponseEntity<?> testEnvioEmail(@RequestBody(required = false) Map<String, String> body, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "ok", false,
                    "code", "UNAUTHORIZED",
                    "provider", "smtp",
                    "message", "Acceso denegado: se requiere autenticación de administrador."
            ));
        }

        String emailDestino = body != null ? body.get("email") : null;
        if (emailDestino == null || emailDestino.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "ok", false,
                    "code", "MISSING_DESTINATION_EMAIL",
                    "provider", "smtp",
                    "message", "El campo 'email' es obligatorio para la prueba de diagnóstico."
            ));
        }

        if (emailNotificationService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "ok", false,
                    "code", "SERVICE_UNAVAILABLE",
                    "provider", "smtp",
                    "message", "EmailNotificationService no está disponible en el contexto Spring."
            ));
        }

        Map<String, Object> resultado = emailNotificationService.enviarEmailDiagnostico(emailDestino);
        boolean ok = Boolean.TRUE.equals(resultado.get("ok"));
        return ResponseEntity.status(ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(resultado);
    }

    @GetMapping("/config")
    public ResponseEntity<?> obtenerConfiguracion(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "ok", false,
                    "code", "UNAUTHORIZED",
                    "message", "Acceso denegado: se requiere autenticación de administrador."
            ));
        }

        if (emailNotificationService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "ok", false,
                    "code", "SERVICE_UNAVAILABLE",
                    "message", "EmailNotificationService no está disponible en el contexto Spring."
            ));
        }

        return ResponseEntity.ok(emailNotificationService.obtenerConfiguracionDiagnostico());
    }

    @GetMapping("/brevo-auth-test")
    public ResponseEntity<?> testAutenticacionBrevo(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "ok", false,
                    "code", "UNAUTHORIZED",
                    "message", "Acceso denegado: se requiere autenticación de administrador."
            ));
        }

        if (emailNotificationService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "ok", false,
                    "code", "SERVICE_UNAVAILABLE",
                    "message", "EmailNotificationService no está disponible en el contexto Spring."
            ));
        }

        Map<String, Object> resultado = emailNotificationService.validarAutenticacionBrevo();
        boolean ok = Boolean.TRUE.equals(resultado.get("ok"));
        return ResponseEntity.status(ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(resultado);
    }
}
