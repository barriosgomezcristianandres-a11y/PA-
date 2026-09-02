package com.isivi.app;

import com.isivi.app.controller.PagoController;
import com.isivi.app.controller.ProductoController;
import com.isivi.app.controller.ReservaController;
import com.isivi.app.controller.ServicioController;
import com.isivi.app.dto.PagoCheckoutResponse;
import com.isivi.app.model.*;
import com.isivi.app.repository.*;
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

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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
public class ReservationHoldReleaseTest {

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private PagoController pagoController;

    @Autowired
    private ServicioController servicioController;

    @Autowired
    private ProductoController productoController;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    private Servicio servicioPrueba;
    private Producto productoPrueba;
    private LocalDate fechaPrueba;
    private final String horaPrueba = "09:30 AM";
    private final ZoneId zonaBogota = ZoneId.of("America/Bogota");


    @BeforeEach
    void setUp() {
        reservaRepository.deleteAll();
        servicioRepository.deleteAll();
        productoRepository.deleteAll();

        servicioPrueba = new Servicio("Corte y Estilismo", "Peluquería", 80000.0, "45 min", "Desc", "");
        servicioPrueba = servicioRepository.save(servicioPrueba);

        productoPrueba = new Producto("Aceite Capilar", 40000.0, "Desc", "", true, 10);
        productoPrueba = productoRepository.save(productoPrueba);

        fechaPrueba = LocalDate.now(zonaBogota).plusDays(3);
        while (fechaPrueba.getDayOfWeek() == DayOfWeek.MONDAY) {
            fechaPrueba = fechaPrueba.plusDays(1);
        }

        reservaService.setPagoExpiracionMinutos(15);
        reservaController.setClock(Clock.system(zonaBogota));
    }

    @AfterEach
    void tearDown() {
        reservaController.setClock(Clock.system(zonaBogota));
        reservaRepository.deleteAll();
        servicioRepository.deleteAll();
        productoRepository.deleteAll();
    }

    @Test
    @DisplayName("1. Elegir otro horario: libera el horario inmediatamente en backend y restaura disponibilidad")
    void testLiberarPorElegirOtroHorario() {
        Reserva req = new Reserva();
        req.setNombreCliente("Andrea Castro");
        req.setTelefono("3181112233");
        req.setMedioPago("WOMPI");
        req.setFechaCita(fechaPrueba);
        req.setHoraCita(horaPrueba);
        req.setItemsInventario(List.of(
                new ItemReserva(servicioPrueba.getId(), "servicio", 1)
        ));

        ResponseEntity<?> respCrear = reservaController.crear(req);
        assertEquals(HttpStatus.CREATED, respCrear.getStatusCode());
        Reserva creada = (Reserva) respCrear.getBody();
        assertNotNull(creada);

        // Cliente inicia pasarela Wompi
        pagoController.preparar(creada.getId());

        // Verificar que el horario aparece ocupado
        List<String> ocupados = reservaController.disponibilidad(fechaPrueba.toString());
        assertTrue(ocupados.contains(horaPrueba), "El horario debe estar ocupado mientras la retención esté activa");

        // Cliente pulsa 'Elegir otro horario' -> libera retención
        ResponseEntity<?> respLiberar = reservaController.liberarRetencion(creada.getId(), Map.of("telefono", "3181112233"));
        assertEquals(HttpStatus.OK, respLiberar.getStatusCode());

        // Verificar que el horario ahora está disponible inmediatamente para otro cliente
        List<String> ocupadosDespues = reservaController.disponibilidad(fechaPrueba.toString());
        assertFalse(ocupadosDespues.contains(horaPrueba), "El horario debe estar libre inmediatamente tras la liberación");


        // Verificar trazabilidad histórica en MongoDB
        Reserva rCancelada = reservaRepository.findById(creada.getId()).orElseThrow();
        assertEquals("Cancelada", rCancelada.getEstado());
        assertNull(rCancelada.getFechaExpiracionPago());
    }

    @Test
    @DisplayName("2. Seguridad y Anti-Apropiación: tercero no puede liberar una reserva ajena (403 Forbidden)")
    void testTerceroNoPuedeLiberarReservaAjena() {
        Reserva req = new Reserva();
        req.setNombreCliente("Titular Legítimo");
        req.setTelefono("3127778899");
        req.setMedioPago("WOMPI");
        req.setFechaCita(fechaPrueba);
        req.setHoraCita(horaPrueba);
        req.setItemsInventario(List.of(new ItemReserva(servicioPrueba.getId(), "servicio", 1)));

        ResponseEntity<?> respCrear = reservaController.crear(req);
        assertEquals(HttpStatus.CREATED, respCrear.getStatusCode());
        Reserva creada = (Reserva) respCrear.getBody();
        pagoController.preparar(creada.getId());

        // Intento de liberación con credenciales ajenas
        ResponseEntity<?> respTercero = reservaController.liberarRetencion(creada.getId(), Map.of("telefono", "3000000000"));
        assertEquals(HttpStatus.FORBIDDEN, respTercero.getStatusCode());

        // El horario sigue protegido
        List<String> ocupados = reservaController.disponibilidad(fechaPrueba.toString());
        assertTrue(ocupados.contains(horaPrueba));
    }

    @Test
    @DisplayName("3. Idempotencia y doble click: liberaciones consecutivas no duplican inventario")
    void testIdempotenciaLiberacionMultiple() {
        Reserva req = new Reserva();
        req.setNombreCliente("Carlos Mendoza");
        req.setTelefono("3165554433");
        req.setMedioPago("WOMPI");
        req.setItemsInventario(List.of(new ItemReserva(productoPrueba.getId(), "producto", 3)));

        ResponseEntity<?> respCrear = reservaController.crear(req);
        assertEquals(HttpStatus.CREATED, respCrear.getStatusCode());
        Reserva creada = (Reserva) respCrear.getBody();
        pagoController.preparar(creada.getId());

        Producto stockAntes = productoRepository.findById(productoPrueba.getId()).orElseThrow();
        assertEquals(7, stockAntes.getCantidad());

        // Primera llamada de liberación
        reservaController.liberarRetencion(creada.getId(), Map.of("telefono", "3165554433"));
        Producto stockLiberado = productoRepository.findById(productoPrueba.getId()).orElseThrow();
        assertEquals(10, stockLiberado.getCantidad());

        // Segunda llamada consecutiva (simulando doble clic)
        reservaController.liberarRetencion(creada.getId(), Map.of("telefono", "3165554433"));
        Producto stockDespues = productoRepository.findById(productoPrueba.getId()).orElseThrow();
        assertEquals(10, stockDespues.getCantidad(), "El stock no debe incrementarse doblemente");
    }

    @Test
    @DisplayName("4. Nuevo cliente puede reservar el horario inmediatamente tras la liberación")
    void testNuevoClienteReservaHorarioLiberado() {
        Reserva req1 = new Reserva();
        req1.setNombreCliente("Cliente Original");
        req1.setTelefono("3114445566");
        req1.setMedioPago("WOMPI");
        req1.setFechaCita(fechaPrueba);
        req1.setHoraCita(horaPrueba);
        req1.setItemsInventario(List.of(new ItemReserva(servicioPrueba.getId(), "servicio", 1)));

        ResponseEntity<?> respCrear1 = reservaController.crear(req1);
        assertEquals(HttpStatus.CREATED, respCrear1.getStatusCode());
        Reserva creada1 = (Reserva) respCrear1.getBody();
        pagoController.preparar(creada1.getId());


        // Cliente 1 libera su horario
        reservaController.liberarRetencion(creada1.getId(), Map.of("telefono", "3114445566"));

        // Cliente 2 solicita el mismo turno
        Reserva req2 = new Reserva();
        req2.setNombreCliente("Cliente Nuevo");
        req2.setTelefono("3178889900");
        req2.setMedioPago("WOMPI");
        req2.setFechaCita(fechaPrueba);
        req2.setHoraCita(horaPrueba);
        req2.setItemsInventario(List.of(new ItemReserva(servicioPrueba.getId(), "servicio", 1)));

        ResponseEntity<?> respCrear2 = reservaController.crear(req2);
        assertEquals(HttpStatus.CREATED, respCrear2.getStatusCode());
        Reserva creada2 = (Reserva) respCrear2.getBody();
        assertNotNull(creada2);
        assertEquals(creada2.getFechaCita(), fechaPrueba);
        assertEquals(creada2.getHoraCita(), horaPrueba);
    }
}
