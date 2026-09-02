package com.isivi.app;

import com.isivi.app.model.Reserva;
import com.isivi.app.service.EmailNotificationService;
import com.isivi.app.service.WhatsAppNotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class EmailAndWhatsAppResilienceTest {

    @Autowired
    private EmailNotificationService emailService;

    @Autowired
    private WhatsAppNotificationService whatsappService;

    @Test
    @DisplayName("1. Fallo de WhatsApp cliente (credenciales vacías o error) no propaga excepción")
    void testWhatsAppFailureNoPropaga() {
        Reserva r = new Reserva();
        r.setNombreCliente("Cliente Resiliente");
        r.setTelefono("3009998877");
        r.setSubtotal(100000.0);

        // No debe lanzar excepciones aunque no esté configurado
        assertDoesNotThrow(() -> whatsappService.enviarMensajeCliente(r, "Mensaje de prueba"));
    }

    @Test
    @DisplayName("2. Fallo de Email (no configurado) no lanza excepción y retorna silenciosamente")
    void testEmailFailureNoPropaga() {
        Reserva r = new Reserva();
        r.setNombreCliente("Cliente Correo");
        r.setCodigoReserva("ISV-MOCK-EMAIL");
        r.setEmail("correo-invalido@isivi.com");
        r.setEstado("Confirmado");

        // No debe lanzar excepciones si no está configurado
        assertDoesNotThrow(() -> emailService.enviarConfirmacion(r));
    }
}
