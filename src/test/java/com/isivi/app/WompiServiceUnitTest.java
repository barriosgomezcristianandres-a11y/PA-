package com.isivi.app;

import com.isivi.app.service.WompiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class WompiServiceUnitTest {

    @InjectMocks
    private WompiService wompiService;

    @Test
    @DisplayName("Prueba de Lógica de Negocio: firmaIntegridad genera un SHA-256 válido")
    void testFirmaIntegridadGeneracionCorrecta() {
        ReflectionTestUtils.setField(wompiService, "llavePublica", "pub_test_123");
        ReflectionTestUtils.setField(wompiService, "secretoIntegridad", "secret_integrity_test");
        ReflectionTestUtils.setField(wompiService, "secretoEventos", "secret_events_test");

        assertTrue(wompiService.configurado(), "WompiService debe estar configurado cuando las llaves están presentes");

        String firma = wompiService.firmaIntegridad("REF-12345", 500000);
        assertNotNull(firma, "La firma calculada no debe ser nula");
        assertEquals(64, firma.length(), "La firma SHA-256 hex debe tener 64 caracteres");
    }

    @Test
    @DisplayName("Prueba de Lógica de Negocio: sin llaves configuradas lanza IllegalStateException")
    void testFirmaIntegridadSinConfiguracionLanzaExcepcion() {
        ReflectionTestUtils.setField(wompiService, "llavePublica", "");
        assertFalse(wompiService.configurado(), "WompiService debe indicar no configurado cuando las llaves están vacías");

        assertThrows(IllegalStateException.class, () -> wompiService.firmaIntegridad("REF-99999", 1000),
                "Debe lanzar IllegalStateException al intentar generar firma sin configuración");
    }
}
