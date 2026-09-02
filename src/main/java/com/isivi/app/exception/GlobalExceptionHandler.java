package com.isivi.app.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CancelacionNoPermitidaException.class)
    public ResponseEntity<Map<String, Object>> handleCancelacionNoPermitida(CancelacionNoPermitidaException ex) {
        log.warn("[CANCELACION_NO_PERMITIDA] {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("ok", false);
        body.put("code", "CANCELACION_NO_PERMITIDA");
        body.put("message", ex.getMessage() != null ? ex.getMessage() : "Las cancelaciones deben realizarse con más de 24 horas de anticipación.");
        body.put("horasMinimas", ex.getHorasMinimas());
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateKey(DuplicateKeyException ex) {
        log.warn("[ERROR_CONFLICT] Registro duplicado o clave única en conflicto: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", "CONFLICTO");
        body.put("mensaje", "El registro u horario seleccionado ya se encuentra ocupado o registrado.");
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("[ERROR_API_VALIDATION] Argumento inválido: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", "DATOS_INVALIDOS");
        body.put("mensaje", ex.getMessage() != null ? ex.getMessage() : "Datos de solicitud inválidos.");
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        log.warn("[ERROR_API_STATE] Estado de negocio no válido: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", "ESTADO_INVALIDO");
        body.put("mensaje", ex.getMessage() != null ? ex.getMessage() : "La operación no es válida en el estado actual.");
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> (err.getField() != null ? err.getField() + ": " : "") + err.getDefaultMessage())
                .orElse("Revisa los datos ingresados.");
        log.warn("[ERROR_API_VALIDATION] Fallo de validación: {}", msg);
        Map<String, Object> body = new HashMap<>();
        body.put("error", "VALIDACION");
        body.put("mensaje", msg);
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("[ERROR_API_PAYLOAD] Solicitud JSON ilegible o malformada");
        Map<String, Object> body = new HashMap<>();
        body.put("error", "FORMATO_INVALIDO");
        body.put("mensaje", "El formato del cuerpo de la solicitud no es válido.");
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "METODO_NO_PERMITIDO");
        body.put("mensaje", "Método HTTP " + ex.getMethod() + " no soportado para esta ruta.");
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "NO_ENCONTRADO");
        body.put("mensaje", "El recurso solicitado no fue encontrado.");
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("[ERROR_API_INTERNAL] Error inesperado en el servidor", ex);
        Map<String, Object> body = new HashMap<>();
        body.put("error", "ERROR_INTERNO");
        body.put("mensaje", "No pudimos completar la operación. Por favor intenta nuevamente.");
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
