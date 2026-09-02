package com.isivi.app.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Document(collection = "reservas")
public class Reserva {

    @Id
    private String id;

    @Indexed(unique = true)
    private String codigoReserva;

    @NotBlank
    private String nombreCliente;

    @NotBlank
    private String telefono;

    private String email;

    private String ciudad;

    private List<String> items = new ArrayList<>();

    /** Productos y kits reservados, con su identificador. Permite descontar y devolver inventario. */
    private List<ItemReserva> itemsInventario = new ArrayList<>();

    @Indexed
    private LocalDate fechaCita;


    private String horaCita;

    private Double subtotal;

    private Double anticipo;

    private Double saldo;

    /** Evita descontar o liberar inventario más de una vez para la misma solicitud. */
    private Boolean inventarioReservado = false;

    private String medioPago;

    @Indexed(unique = true, sparse = true)
    private String referenciaWompi;

    /** PENDIENTE | APROBADO | RECHAZADO | ERROR | CANCELADO. Independiente del estado de reserva. */
    private String estadoPago;

    private Long montoPagoCentavos;
    private String transaccionWompiId;
    private String metodoPagoWompi;
    private LocalDate fechaPago;

    /** Momento hasta el cual la reserva en estado "Pendiente Pago" mantiene retenido el horario temporalmente. */
    private java.time.Instant fechaExpiracionPago;

    private String tipoEntrega;


    private String direccionEntrega;

    private String estado = "Pendiente Comprobante"; // Pendiente Comprobante | Confirmado

    /** Mantiene el registro fuera de la agenda activa sin eliminarlo. */
    private Boolean archivada = false;

    /** Trazabilidad de envío de confirmación por email (idempotencia) */
    private Boolean emailConfirmacionEnviada = false;
    private java.time.Instant fechaEnvioConfirmacion;
    private String emailErrorEnvio;

    private LocalDate fechaRegistro;

    private LocalDate fechaArchivado;

    /** Trazabilidad del sistema híbrido de cancelación */
    private java.time.Instant fechaCancelacion;
    private java.time.Instant fechaSolicitudCancelacion;
    private String canceladaPor; // CLIENTE | ADMIN | LIBERACION_PROVISIONAL | SISTEMA | CLIENTE_APROBADA_ADMIN
    private String motivoCancelacion;
    private String motivoRechazoCancelacion;
    private Boolean notificacionCancelacionVista = false;
    private String estadoPrevioCancelacion;

    /** Trazabilidad del ciclo de vida de reprogramación */
    private LocalDate fechaPropuestaReprogramacion;
    private String horaPropuestaReprogramacion;
    private String estadoPrevioReprogramacion;

    private Boolean recordatorioEnviado = false;

    public Reserva() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCodigoReserva() { return codigoReserva; }
    public void setCodigoReserva(String codigoReserva) { this.codigoReserva = codigoReserva; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public List<String> getItems() { 
        if (items == null) items = new ArrayList<>();
        return items; 
    }
    public void setItems(List<String> items) { this.items = items; }

    public List<ItemReserva> getItemsInventario() { 
        if (itemsInventario == null) itemsInventario = new ArrayList<>();
        return itemsInventario; 
    }
    public void setItemsInventario(List<ItemReserva> itemsInventario) { this.itemsInventario = itemsInventario; }


    public LocalDate getFechaCita() { return fechaCita; }
    public void setFechaCita(LocalDate fechaCita) { this.fechaCita = fechaCita; }

    public String getHoraCita() { return horaCita; }
    public void setHoraCita(String horaCita) { this.horaCita = horaCita; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Double getAnticipo() { return anticipo; }
    public void setAnticipo(Double anticipo) { this.anticipo = anticipo; }
    public Double getSaldo() { return saldo; }
    public void setSaldo(Double saldo) { this.saldo = saldo; }
    public Boolean getInventarioReservado() { return inventarioReservado; }
    public void setInventarioReservado(Boolean inventarioReservado) { this.inventarioReservado = inventarioReservado; }

    public String getMedioPago() { return medioPago; }
    public void setMedioPago(String medioPago) { this.medioPago = medioPago; }
    public String getReferenciaWompi() { return referenciaWompi; }
    public void setReferenciaWompi(String referenciaWompi) { this.referenciaWompi = referenciaWompi; }
    public String getEstadoPago() { return estadoPago; }
    public void setEstadoPago(String estadoPago) { this.estadoPago = estadoPago; }
    public Long getMontoPagoCentavos() { return montoPagoCentavos; }
    public void setMontoPagoCentavos(Long montoPagoCentavos) { this.montoPagoCentavos = montoPagoCentavos; }
    public String getTransaccionWompiId() { return transaccionWompiId; }
    public void setTransaccionWompiId(String transaccionWompiId) { this.transaccionWompiId = transaccionWompiId; }
    public String getMetodoPagoWompi() { return metodoPagoWompi; }
    public void setMetodoPagoWompi(String metodoPagoWompi) { this.metodoPagoWompi = metodoPagoWompi; }
    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }

    public java.time.Instant getFechaExpiracionPago() { return fechaExpiracionPago; }
    public void setFechaExpiracionPago(java.time.Instant fechaExpiracionPago) { this.fechaExpiracionPago = fechaExpiracionPago; }

    public String getTipoEntrega() { return tipoEntrega; }

    public void setTipoEntrega(String tipoEntrega) { this.tipoEntrega = tipoEntrega; }

    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Boolean getArchivada() { return archivada; }
    public void setArchivada(Boolean archivada) { this.archivada = archivada; }

    public Boolean getEmailConfirmacionEnviada() { return emailConfirmacionEnviada; }
    public void setEmailConfirmacionEnviada(Boolean emailConfirmacionEnviada) { this.emailConfirmacionEnviada = emailConfirmacionEnviada; }

    public java.time.Instant getFechaEnvioConfirmacion() { return fechaEnvioConfirmacion; }
    public void setFechaEnvioConfirmacion(java.time.Instant fechaEnvioConfirmacion) { this.fechaEnvioConfirmacion = fechaEnvioConfirmacion; }

    public String getEmailErrorEnvio() { return emailErrorEnvio; }
    public void setEmailErrorEnvio(String emailErrorEnvio) { this.emailErrorEnvio = emailErrorEnvio; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public LocalDate getFechaArchivado() { return fechaArchivado; }
    public void setFechaArchivado(LocalDate fechaArchivado) { this.fechaArchivado = fechaArchivado; }

    public java.time.Instant getFechaCancelacion() { return fechaCancelacion; }
    public void setFechaCancelacion(java.time.Instant fechaCancelacion) { this.fechaCancelacion = fechaCancelacion; }

    public java.time.Instant getFechaSolicitudCancelacion() { return fechaSolicitudCancelacion; }
    public void setFechaSolicitudCancelacion(java.time.Instant fechaSolicitudCancelacion) { this.fechaSolicitudCancelacion = fechaSolicitudCancelacion; }

    public String getCanceladaPor() { return canceladaPor; }
    public void setCanceladaPor(String canceladaPor) { this.canceladaPor = canceladaPor; }

    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }

    public String getMotivoRechazoCancelacion() { return motivoRechazoCancelacion; }
    public void setMotivoRechazoCancelacion(String motivoRechazoCancelacion) { this.motivoRechazoCancelacion = motivoRechazoCancelacion; }

    public String getEstadoPrevioCancelacion() { return estadoPrevioCancelacion; }
    public void setEstadoPrevioCancelacion(String estadoPrevioCancelacion) { this.estadoPrevioCancelacion = estadoPrevioCancelacion; }

    public Boolean getNotificacionCancelacionVista() { return notificacionCancelacionVista; }
    public void setNotificacionCancelacionVista(Boolean notificacionCancelacionVista) { this.notificacionCancelacionVista = notificacionCancelacionVista; }

    /** Trazabilidad del ciclo de vida operativo de pedidos */
    private String estadoPedido; // PENDIENTE_PREPARACION | EN_PREPARACION | LISTO_ENVIO | EN_CAMINO | ENTREGADO | LISTO_RECOGER | RECOGIDO | CANCELADO
    private java.time.Instant fechaEnPreparacion;
    private java.time.Instant fechaListoEnvio;
    private java.time.Instant fechaEnCamino;
    private java.time.Instant fechaListoParaRecoger;
    private java.time.Instant fechaEntregado;
    private String atendidoPor;

    public String getEstadoPedido() {
        if (!esPedido()) {
            return null;
        }
        if (estadoPedido != null && !estadoPedido.isBlank()) {
            return estadoPedido;
        }
        // Fallback inteligente para registros previos
        if (estado != null) {
            String est = estado.trim().toUpperCase();
            if (est.contains("PAGO CONFIRMADO") || est.equals("CONFIRMADO")) return "PENDIENTE_PREPARACION";
            if (est.contains("PREPARACION") || est.contains("PREPARACIÓN")) return "EN_PREPARACION";
            if (est.contains("LISTO PARA RECOGER")) return "LISTO_RECOGER";
            if (est.contains("LISTO PARA ENVIO") || est.contains("LISTO PARA ENVÍO")) return "LISTO_ENVIO";
            if (est.contains("EN CAMINO")) return "EN_CAMINO";
            if (est.contains("RECOGIDO")) return "RECOGIDO";
            if (est.contains("ENTREGADO")) return esDomicilio() ? "ENTREGADO" : "RECOGIDO";
            if (est.contains("CANCELAD")) return "CANCELADO";
            if (est.contains("PAGO PENDIENTE") || est.contains("PENDIENTE PAGO")) return "PENDIENTE_PAGO";
            if (est.contains("COMPROBANTE") || est.equals("PENDIENTE")) return "PENDIENTE_COMPROBANTE";
        }
        return null;
    }

    public void setEstadoPedido(String estadoPedido) {
        this.estadoPedido = estadoPedido;
    }

    public java.time.Instant getFechaEnPreparacion() { return fechaEnPreparacion; }
    public void setFechaEnPreparacion(java.time.Instant fechaEnPreparacion) { this.fechaEnPreparacion = fechaEnPreparacion; }

    public java.time.Instant getFechaListoEnvio() { return fechaListoEnvio; }
    public void setFechaListoEnvio(java.time.Instant fechaListoEnvio) { this.fechaListoEnvio = fechaListoEnvio; }

    public java.time.Instant getFechaEnCamino() { return fechaEnCamino; }
    public void setFechaEnCamino(java.time.Instant fechaEnCamino) { this.fechaEnCamino = fechaEnCamino; }

    public java.time.Instant getFechaListoParaRecoger() { return fechaListoParaRecoger; }
    public void setFechaListoParaRecoger(java.time.Instant fechaListoParaRecoger) { this.fechaListoParaRecoger = fechaListoParaRecoger; }

    public java.time.Instant getFechaEntregado() { return fechaEntregado; }
    public void setFechaEntregado(java.time.Instant fechaEntregado) { this.fechaEntregado = fechaEntregado; }

    public String getAtendidoPor() { return atendidoPor; }
    public void setAtendidoPor(String atendidoPor) { this.atendidoPor = atendidoPor; }

    /** Métodos de dominio para distinguir cita, pedido y compra mixta */
    public boolean esCita() {
        if (fechaCita != null && horaCita != null && !horaCita.isBlank()) return true;
        if (itemsInventario != null && !itemsInventario.isEmpty()) {
            return itemsInventario.stream().anyMatch(i -> "servicio".equalsIgnoreCase(i.getTipo()));
        }
        return false;
    }

    public boolean esPedido() {
        if (itemsInventario != null && !itemsInventario.isEmpty()) {
            return itemsInventario.stream().anyMatch(i -> "producto".equalsIgnoreCase(i.getTipo()) || "kit".equalsIgnoreCase(i.getTipo()));
        }
        return !esCita();
    }

    public boolean esMixto() {
        if (itemsInventario == null || itemsInventario.isEmpty()) return false;
        boolean tieneServ = itemsInventario.stream().anyMatch(i -> "servicio".equalsIgnoreCase(i.getTipo()));
        boolean tieneProd = itemsInventario.stream().anyMatch(i -> !"servicio".equalsIgnoreCase(i.getTipo()));
        return tieneServ && tieneProd;
    }

    public boolean esPedidoPuro() {
        return esPedido() && !esCita();
    }

    public boolean esDomicilio() {
        return tipoEntrega != null && (tipoEntrega.equalsIgnoreCase("domicilio") || tipoEntrega.equalsIgnoreCase("delivery"));
    }

    public boolean esRecogida() {
        return !esDomicilio();
    }

    public String getMetodoEntrega() {
        return esDomicilio() ? "Domicilio" : "Recoger en el local";
    }

    public void setMetodoEntrega(String metodo) {
        this.tipoEntrega = metodo;
    }

    public String getDireccion() {
        return direccionEntrega;
    }

    public void setDireccion(String direccion) {
        this.direccionEntrega = direccion;
    }

    public LocalDate getFechaPropuestaReprogramacion() { return fechaPropuestaReprogramacion; }
    public void setFechaPropuestaReprogramacion(LocalDate fechaPropuestaReprogramacion) { this.fechaPropuestaReprogramacion = fechaPropuestaReprogramacion; }

    public String getHoraPropuestaReprogramacion() { return horaPropuestaReprogramacion; }
    public void setHoraPropuestaReprogramacion(String horaPropuestaReprogramacion) { this.horaPropuestaReprogramacion = horaPropuestaReprogramacion; }

    public String getEstadoPrevioReprogramacion() { return estadoPrevioReprogramacion; }
    public void setEstadoPrevioReprogramacion(String estadoPrevioReprogramacion) { this.estadoPrevioReprogramacion = estadoPrevioReprogramacion; }

    public Boolean getRecordatorioEnviado() { return recordatorioEnviado != null ? recordatorioEnviado : false; }
    public void setRecordatorioEnviado(Boolean recordatorioEnviado) { this.recordatorioEnviado = recordatorioEnviado; }
}

