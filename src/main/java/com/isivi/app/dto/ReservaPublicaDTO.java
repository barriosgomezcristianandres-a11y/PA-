package com.isivi.app.dto;

import com.isivi.app.model.Reserva;
import java.time.LocalDate;

public class ReservaPublicaDTO extends Reserva {

    public ReservaPublicaDTO() {
        super();
    }

    public ReservaPublicaDTO(Reserva r) {
        super();
        if (r != null) {
            this.setId(r.getId());
            this.setCodigoReserva(r.getCodigoReserva());
            this.setNombreCliente(r.getNombreCliente());
            this.setItems(r.getItems());
            this.setItemsInventario(r.getItemsInventario());
            this.setFechaCita(r.getFechaCita());
            this.setHoraCita(r.getHoraCita());
            this.setSubtotal(r.getSubtotal());
            this.setAnticipo(r.getAnticipo());
            this.setSaldo(r.getSaldo());
            this.setTipoEntrega(r.getTipoEntrega());
            this.setEstado(r.getEstado());
            this.setEstadoPedido(r.getEstadoPedido());
            
            // Campos de estado de cancelación
            this.setFechaCancelacion(r.getFechaCancelacion());
            this.setFechaSolicitudCancelacion(r.getFechaSolicitudCancelacion());
            this.setCanceladaPor(r.getCanceladaPor());
            this.setMotivoCancelacion(r.getMotivoCancelacion());
            this.setMotivoRechazoCancelacion(r.getMotivoRechazoCancelacion());
            this.setEstadoPrevioCancelacion(r.getEstadoPrevioCancelacion());
            this.setNotificacionCancelacionVista(r.getNotificacionCancelacionVista());
            
            // Confirmaciones por email
            this.setEmailConfirmacionEnviada(r.getEmailConfirmacionEnviada());
            this.setFechaEnvioConfirmacion(r.getFechaEnvioConfirmacion());
            this.setEmailErrorEnvio(r.getEmailErrorEnvio());
            this.setMedioPago(r.getMedioPago());
            this.setFechaExpiracionPago(r.getFechaExpiracionPago());
        }
    }

    // Sobrescribir getters de datos sensibles para retornar null en serialización pública
    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public String getTelefono() {
        return null;
    }

    @Override
    public String getDireccionEntrega() {
        return null;
    }

    @Override
    public String getReferenciaWompi() {
        return null;
    }

    @Override
    public String getTransaccionWompiId() {
        return null;
    }

    @Override
    public String getMetodoPagoWompi() {
        return null;
    }

    @Override
    public LocalDate getFechaPago() {
        return null;
    }

    @Override
    public String getAtendidoPor() {
        return null;
    }

    @Override
    public String getDireccion() {
        return null;
    }
}
