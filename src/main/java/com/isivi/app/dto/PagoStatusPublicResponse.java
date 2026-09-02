package com.isivi.app.dto;

public record PagoStatusPublicResponse(
    String referencia,
    String estadoPago,
    String estadoReserva,
    String codigoReserva,
    long montoCentavos,
    String moneda,
    String reservaId
) {}
