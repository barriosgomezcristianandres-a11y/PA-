package com.isivi.app.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.isivi.app.dto.PagoCheckoutResponse;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.service.ReservaService;
import com.isivi.app.service.WhatsAppNotificationService;
import com.isivi.app.service.WompiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/** Endpoints de Wompi. La confirmación ocurre solo desde un webhook autenticado. */
@RestController
@RequestMapping("/api/pagos/wompi")
@CrossOrigin(origins = "*")
public class PagoController {
    private final ReservaRepository reservas;
    private final ReservaService reservaService;
    private final WompiService wompi;
    private final WhatsAppNotificationService whatsapp;
    private final ReservaController reservaController;
    private final com.isivi.app.service.NotificacionAdminService notificacionAdminService;

    public PagoController(ReservaRepository reservas, ReservaService reservaService, WompiService wompi,
                          WhatsAppNotificationService whatsapp, ReservaController reservaController,
                          com.isivi.app.service.NotificacionAdminService notificacionAdminService) {
        this.reservas = reservas;
        this.reservaService = reservaService;
        this.wompi = wompi;
        this.whatsapp = whatsapp;
        this.reservaController = reservaController;
        this.notificacionAdminService = notificacionAdminService;
    }

    /** Solo expone disponibilidad; nunca devuelve llaves ni secretos. */
    @GetMapping("/disponible")
    public Map<String, Boolean> disponible() { return Map.of("disponible", wompi.configurado()); }

    @PostMapping("/preparar/{reservaId}")
    public synchronized ResponseEntity<?> preparar(@PathVariable String reservaId) {
        try {
            Reserva reserva = reservas.findById(reservaId).orElse(null);
            if (reserva == null) return ResponseEntity.notFound().build();
            if (reservaService.estaExpirada(reserva)) {
                reservaService.marcarExpirada(reserva);
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "RESERVA_EXPIRADA",
                        "mensaje", "El tiempo de retención para este horario ha expirado. Por favor selecciona otro turno."
                ));
            }
            if (!wompi.configurado()) return ResponseEntity.status(503).body(Map.of("mensaje", "Wompi no está configurado en el servidor."));
            String referencia;
            do { referencia = "ISV-" + reserva.getCodigoReserva() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(); }
            while (reservas.existsByReferenciaWompi(referencia));
            reserva = reservaService.prepararPagoWompi(reserva, referencia);
            return ResponseEntity.ok(new PagoCheckoutResponse(reserva.getReferenciaWompi(), reserva.getMontoPagoCentavos(), "COP", wompi.llavePublica(), wompi.firmaIntegridad(reserva.getReferenciaWompi(), reserva.getMontoPagoCentavos()), wompi.urlRedireccion(), reserva.getId(), reserva.getCodigoReserva(), reserva.getFechaExpiracionPago()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
        }
    }

    @PostMapping("/retomar/{reservaId}")
    public synchronized ResponseEntity<?> retomar(@PathVariable String reservaId, @RequestBody(required = false) Map<String, String> credenciales) {
        try {
            Reserva reserva = reservas.findById(reservaId).orElse(null);
            if (reserva == null) return ResponseEntity.notFound().build();
            if (reservaService.estaExpirada(reserva)) {
                reservaService.marcarExpirada(reserva);
                String msg = reserva.esPedidoPuro() 
                    ? "El tiempo límite para pagar tu pedido ha expirado y el stock ha sido liberado." 
                    : "El tiempo de retención para este horario ha expirado. Por favor selecciona otro turno.";
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "RESERVA_EXPIRADA",
                        "mensaje", msg
                ));
            }
            if (!wompi.configurado()) return ResponseEntity.status(503).body(Map.of("mensaje", "Wompi no está configurado en el servidor."));

            String telefono = credenciales != null ? credenciales.get("telefono") : null;
            String codigoReserva = credenciales != null ? credenciales.get("codigoReserva") : null;

            String referencia = reserva.getReferenciaWompi();
            if (referencia == null || referencia.isBlank()) {
                do { referencia = "ISV-" + reserva.getCodigoReserva() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(); }
                while (reservas.existsByReferenciaWompi(referencia));
            }

            reserva = reservaService.retomarPagoWompi(reserva, telefono, codigoReserva, referencia);
            return ResponseEntity.ok(new PagoCheckoutResponse(reserva.getReferenciaWompi(), reserva.getMontoPagoCentavos(), "COP", wompi.llavePublica(), wompi.firmaIntegridad(reserva.getReferenciaWompi(), reserva.getMontoPagoCentavos()), wompi.urlRedireccion(), reserva.getId(), reserva.getCodigoReserva(), reserva.getFechaExpiracionPago()));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "NO_AUTORIZADO", "mensaje", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
        }
    }





    @PostMapping("/webhook")
    public synchronized ResponseEntity<?> webhook(@RequestBody JsonNode evento, @RequestHeader(value = "X-Event-Checksum", required = false) String checksum) {
        try {
            if (!"transaction.updated".equals(evento.path("event").asText())) return ResponseEntity.ok().build();
            if (wompi.sandbox() && !"test".equals(evento.path("environment").asText())) return ResponseEntity.status(400).body(Map.of("mensaje", "El evento no corresponde a Sandbox."));
            if (!wompi.sandbox() && !"prod".equals(evento.path("environment").asText())) return ResponseEntity.status(400).body(Map.of("mensaje", "El evento no corresponde a Producción."));
            if (!wompi.eventoAutentico(evento, checksum)) return ResponseEntity.status(401).body(Map.of("mensaje", "Firma de evento Wompi inválida."));
            JsonNode tx = evento.path("data").path("transaction");
            String referencia = tx.path("reference").asText();
            Reserva reserva = reservas.findByReferenciaWompi(referencia).orElse(null);
            if (reserva == null) return ResponseEntity.status(404).body(Map.of("mensaje", "Referencia Wompi no encontrada."));
            if (tx.path("amount_in_cents").asLong(-1) != reserva.getMontoPagoCentavos() || !"COP".equals(tx.path("currency").asText())) return ResponseEntity.status(400).body(Map.of("mensaje", "Monto o moneda de Wompi no coinciden."));
            String transactionId = tx.path("id").asText();
            if (transactionId.isBlank()) return ResponseEntity.badRequest().body(Map.of("mensaje", "La transacción Wompi no es válida."));
            if ("APROBADO".equals(reserva.getEstadoPago())) return ResponseEntity.ok().build(); // idempotencia
            String estado = tx.path("status").asText();
            reserva.setTransaccionWompiId(transactionId);
            reserva.setMetodoPagoWompi(tx.path("payment_method_type").asText("WOMPI"));
            if ("APPROVED".equals(estado)) {
                reserva.setFechaPago(LocalDate.now(java.time.ZoneId.of("America/Bogota")));
                boolean expirada = "Expirada".equalsIgnoreCase(reserva.getEstado()) || reservaService.estaExpirada(reserva);
                if (expirada) {
                    boolean ocupado = false;
                    if (reserva.esCita()) {
                        ocupado = reservaController.agendaOcupada(reserva.getFechaCita(), reserva.getHoraCita(), reserva.getId());
                    }
                    if (ocupado) {
                        reserva.setEstadoPago("APROBADO");
                        reserva.setEstado("CONFLICTO_PAGO_EXPIRADO");
                        reserva.setFechaExpiracionPago(null);
                        Reserva guardada = reservas.save(reserva);
                        
                        // Notificación administrativa de conflicto
                        notificacionAdminService.registrarConflictoPago(guardada, "Pago aprobado tarde y el horario ya no está disponible.");
                        
                        whatsapp.enviar(guardada, whatsapp.resumen(guardada, "Tu pago fue aprobado tarde y el horario ya no está disponible. Nos comunicaremos contigo."));
                    } else {
                        // Slot libre, confirmar normalmente
                        Reserva confirmada = reservaService.confirmarPagoWompi(reserva);
                        notificacionAdminService.registrarPagoAprobado(referencia, confirmada);
                        String msg = confirmada.esPedido() ? "Comprobante confirmado. Pedido listo para preparación." : "Tu pago Wompi fue aprobado y tu solicitud ISIVI está confirmada.";
                        whatsapp.enviar(confirmada, whatsapp.resumen(confirmada, msg));
                    }
                } else {
                    Reserva confirmada = reservaService.confirmarPagoWompi(reserva);
                    notificacionAdminService.registrarPagoAprobado(referencia, confirmada);
                    String msg = confirmada.esPedido() ? "Comprobante confirmado. Pedido listo para preparación." : "Tu pago Wompi fue aprobado y tu solicitud ISIVI está confirmada.";
                    whatsapp.enviar(confirmada, whatsapp.resumen(confirmada, msg));
                }
            } else {
                boolean rechazado = "DECLINED".equals(estado) || "VOIDED".equals(estado);
                boolean esError = "ERROR".equals(estado);
                reserva.setEstadoPago(rechazado ? "RECHAZADO" : esError ? "ERROR" : "PENDIENTE");
                if (rechazado || esError) {
                    if (reserva.esPedidoPuro()) {
                        reserva.setEstado(ReservaService.PENDIENTE_PAGO);
                        reservaService.liberarInventario(reserva);
                    } else {
                        reserva.setEstado("Denegada");
                        reservaService.liberarInventario(reserva);
                    }
                    
                    // Notificación administrativa de pago rechazado/error
                    notificacionAdminService.registrarPagoRechazado(referencia, reserva);
                }
                reservas.save(reserva);
            }
            return ResponseEntity.ok().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "DATOS_INVALIDOS", "mensaje", ex.getMessage()));
        }
    }

    @GetMapping("/estado/{referencia}")
    public ResponseEntity<?> estado(@PathVariable String referencia) {
        return reservas.findByReferenciaWompi(referencia)
                .or(() -> reservas.findByTransaccionWompiId(referencia))
                .<ResponseEntity<?>>map(r -> ResponseEntity.ok(new com.isivi.app.dto.PagoStatusPublicResponse(
                        r.getReferenciaWompi() != null ? r.getReferenciaWompi() : "",
                        r.getEstadoPago() == null ? "PENDIENTE" : r.getEstadoPago(),
                        r.getEstado() != null ? r.getEstado() : "Pendiente",
                        r.getCodigoReserva() != null ? r.getCodigoReserva() : "",
                        r.getMontoPagoCentavos() == null ? 0 : r.getMontoPagoCentavos(),
                        "COP",
                        r.getId()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/transaccion/{transactionId}")
    public ResponseEntity<?> transaccion(@PathVariable String transactionId) {
        return reservas.findByTransaccionWompiId(transactionId)
                .or(() -> reservas.findByReferenciaWompi(transactionId))
                .<ResponseEntity<?>>map(r -> ResponseEntity.ok(new com.isivi.app.dto.PagoStatusPublicResponse(
                        r.getReferenciaWompi() != null ? r.getReferenciaWompi() : "",
                        r.getEstadoPago() == null ? "PENDIENTE" : r.getEstadoPago(),
                        r.getEstado() != null ? r.getEstado() : "Pendiente",
                        r.getCodigoReserva() != null ? r.getCodigoReserva() : "",
                        r.getMontoPagoCentavos() == null ? 0 : r.getMontoPagoCentavos(),
                        "COP",
                        r.getId()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}
