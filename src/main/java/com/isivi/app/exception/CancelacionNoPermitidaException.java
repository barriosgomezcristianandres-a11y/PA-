package com.isivi.app.exception;

public class CancelacionNoPermitidaException extends RuntimeException {
    private final int horasMinimas;

    public CancelacionNoPermitidaException(String message, int horasMinimas) {
        super(message);
        this.horasMinimas = horasMinimas;
    }

    public int getHorasMinimas() {
        return horasMinimas;
    }
}
