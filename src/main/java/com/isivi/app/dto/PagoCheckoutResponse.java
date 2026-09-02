package com.isivi.app.dto;

import java.time.Instant;

/** Datos públicos y firmados que el navegador necesita para abrir Wompi Checkout. */
public record PagoCheckoutResponse(String referencia, long montoCentavos, String moneda,
                                   String llavePublica, String firmaIntegridad, String urlRedireccion,
                                   String reservaId, String codigoReserva, Instant fechaExpiracionPago) {

    public PagoCheckoutResponse(String referencia, long montoCentavos, String moneda,
                                String llavePublica, String firmaIntegridad, String urlRedireccion) {
        this(referencia, montoCentavos, moneda, llavePublica, firmaIntegridad, urlRedireccion, null, null, null);
    }
}
