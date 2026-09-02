package com.isivi.app;

import com.isivi.app.controller.PagoController;
import com.isivi.app.controller.ReservaController;
import com.isivi.app.dto.PagoCheckoutResponse;
import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Reserva;
import com.isivi.app.model.Servicio;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.repository.ServicioRepository;
import com.isivi.app.service.ReservaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "isivi.wompi.public-key=pub_test_QA_ISIVI_12345",
        "isivi.wompi.private-key=prv_test_QA_SECRET_98765",
        "isivi.wompi.integrity-secret=test_integrity_QA_SECRET_123",
        "isivi.wompi.events-secret=test_events_QA_SECRET_456",
        "isivi.wompi.sandbox=true",
        "isivi.wompi.base-url=https://sandbox.wompi.co/v1"
})
public class WompiRecoveryTest {

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private PagoController pagoController;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    private Servicio servicio;
    private final ZoneId zonaBogota = ZoneId.of("America/Bogota");

    @BeforeEach
    void setUp() {
        reservaRepository.deleteAll();
        servicioRepository.deleteAll();
        servicio = servicioRepository.save(new Servicio("Corte", "Peluquería", 50000.0, "30 min", "Desc", ""));
    }

    @AfterEach
    void tearDown() {
        reservaRepository.deleteAll();
        servicioRepository.deleteAll();
    }

    @Test
    @DisplayName("1. Retomar pago reutiliza la misma reserva sin crear una segunda reserva ni duplicar turnos")
    void testRetomarPagoMismaReserva() {
        LocalDate fecha = LocalDate.now(zonaBogota).plusDays(4);
        while (fecha.getDayOfWeek() == java.time.DayOfWeek.MONDAY) {
            fecha = fecha.plusDays(1);
        }

        Reserva req = new Reserva();
        req.setNombreCliente("Valentina Gómez");
        req.setTelefono("3109998877");
        req.setEmail("valentina@example.com");
        req.setMedioPago("WOMPI");
        req.setFechaCita(fecha);
        req.setHoraCita("09:30 AM");
        req.setItemsInventario(List.of(new ItemReserva(servicio.getId(), "servicio", 1)));

        ResponseEntity<?> respCrear = reservaController.crear(req);
        assertEquals(HttpStatus.CREATED, respCrear.getStatusCode());
        Reserva creada = (Reserva) respCrear.getBody();

        // 1. Primera preparación
        ResponseEntity<?> respPrep = pagoController.preparar(creada.getId());
        assertEquals(HttpStatus.OK, respPrep.getStatusCode());

        // 2. Reintento de pago (simulando que el widget tardó o el usuario cerró y retomó)
        ResponseEntity<?> respRetomar = pagoController.retomar(creada.getId(), Map.of(
                "telefono", "3109998877",
                "codigoReserva", creada.getCodigoReserva()
        ));
        assertEquals(HttpStatus.OK, respRetomar.getStatusCode());
        PagoCheckoutResponse checkout = (PagoCheckoutResponse) respRetomar.getBody();
        assertNotNull(checkout);
        assertEquals(creada.getId(), checkout.reservaId());

        // Verificar que solo existe exactamente 1 reserva en MongoDB
        List<Reserva> todas = reservaRepository.findAll();
        assertEquals(1, todas.size(), "No debe existir ninguna reserva duplicada");
    }
}

