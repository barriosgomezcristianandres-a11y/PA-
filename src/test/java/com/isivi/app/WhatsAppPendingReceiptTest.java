package com.isivi.app;

import com.isivi.app.model.Reserva;
import com.isivi.app.service.WhatsAppNotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class WhatsAppPendingReceiptTest {

    @Autowired
    private WhatsAppNotificationService whatsAppService;

    @Test
    @DisplayName("1. Resumen WhatsApp para cliente no contiene tokens ni datos sensibles internos")
    void testMensajeSinDatosSensibles() {
        Reserva r = new Reserva();
        r.setNombreCliente("Laura Restrepo");
        r.setTelefono("3001234567");
        r.setCodigoReserva("ISV-9988");
        r.setFechaCita(LocalDate.now().plusDays(2));
        r.setHoraCita("09:30 AM");
        r.setAnticipo(20000.0);
        r.setSaldo(60000.0);
        r.setMedioPago("TRANSFERENCIA");
        r.setEstado("Pendiente Comprobante");

        String msg = whatsAppService.resumen(r, "Revisando comprobante.");
        assertNotNull(msg);
        assertTrue(msg.contains("ISV-9988"));
        assertFalse(msg.contains("password"));
        assertFalse(msg.contains("token"));
        assertFalse(msg.contains("secret"));
        assertFalse(msg.contains("apiKey"));
    }

    @Test
    @DisplayName("2. Mensaje es resiliente ante campos nulos o faltantes")
    void testMensajeResilienteCamposFaltantes() {
        Reserva r = new Reserva();
        r.setNombreCliente(null);
        r.setTelefono("3150001122");
        r.setCodigoReserva("ISV-1111");

        String msg = whatsAppService.resumen(r, "Estado actualizado");
        assertNotNull(msg);
        assertFalse(msg.contains("undefined"));
        assertFalse(msg.contains("NaN"));
    }
}
