package com.isivi.app;

import com.isivi.app.controller.PagoController;
import com.isivi.app.controller.ReservaController;
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
public class ReservationPaymentResumeTest {

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

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private KitRepository kitRepository;

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
        kitRepository.deleteAll();

        servicioPrueba = new Servicio("Tratamiento Capilar", "Tratamientos", 100000.0, "60 min", "Desc", "");
        servicioPrueba = servicioRepository.save(servicioPrueba);

        productoPrueba = new Producto("Shampoo Artesanal", 50000.0, "Desc", "", true, 20);
        productoPrueba = productoRepository.save(productoPrueba);

        fechaPrueba = LocalDate.now(zonaBogota).plusDays(2);
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
        kitRepository.deleteAll();
    }

    @Test
    @DisplayName("1. Cliente retoma pago de su propia reserva pendiente con teléfono válido")
    void testRetomarPagoPropietarioExitoso() {
        Reserva req = new Reserva();
        req.setNombreCliente("Ana María Gómez");
        req.setTelefono("3001234567");
        req.setEmail("ana@example.com");
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

        // Cliente inicia Wompi por primera vez -> pasa a Pendiente Pago
        ResponseEntity<?> respPrep = pagoController.preparar(creada.getId());
        assertEquals(HttpStatus.OK, respPrep.getStatusCode());

        Reserva enPendientePago = reservaRepository.findById(creada.getId()).orElseThrow();
        assertEquals("Pendiente Pago", enPendientePago.getEstado());
        assertNotNull(enPendientePago.getFechaExpiracionPago());

        // Retomar con el mismo teléfono
        Map<String, String> credenciales = Map.of("telefono", "3001234567");
        ResponseEntity<?> respRetomar = pagoController.retomar(creada.getId(), credenciales);

        assertEquals(HttpStatus.OK, respRetomar.getStatusCode());
        PagoCheckoutResponse checkout = (PagoCheckoutResponse) respRetomar.getBody();
        assertNotNull(checkout);
        assertNotNull(checkout.referencia());
        assertTrue(checkout.referencia().startsWith("ISV-" + creada.getCodigoReserva()));

        // Verificar que no se creó otra reserva en BD
        assertEquals(1, reservaRepository.count());
    }

    @Test
    @DisplayName("2. Cliente retoma pago usando código de reserva")
    void testRetomarPagoConCodigoReserva() {
        Reserva req = new Reserva();
        req.setNombreCliente("Carlos Mendoza");
        req.setTelefono("3119876543");
        req.setEmail("carlos@example.com");
        req.setMedioPago("WOMPI");
        req.setFechaCita(fechaPrueba);
        req.setHoraCita(horaPrueba);
        req.setItemsInventario(List.of(
                new ItemReserva(servicioPrueba.getId(), "servicio", 1)
        ));

        ResponseEntity<?> respCrear = reservaController.crear(req);
        Reserva creada = (Reserva) respCrear.getBody();

        pagoController.preparar(creada.getId());

        // Retomar con código de reserva
        Map<String, String> credenciales = Map.of("codigoReserva", creada.getCodigoReserva());
        ResponseEntity<?> respRetomar = pagoController.retomar(creada.getId(), credenciales);

        assertEquals(HttpStatus.OK, respRetomar.getStatusCode());
    }

    @Test
    @DisplayName("3. Rechazo de intento de apropiación por otro cliente (teléfono no coincide)")
    void testRechazoApropiacionOtroCliente() {
        Reserva req = new Reserva();
        req.setNombreCliente("Cliente Legítimo");
        req.setTelefono("3001112233");
        req.setEmail("cliente@example.com");
        req.setMedioPago("WOMPI");
        req.setFechaCita(fechaPrueba);
        req.setHoraCita(horaPrueba);
        req.setItemsInventario(List.of(
                new ItemReserva(servicioPrueba.getId(), "servicio", 1)
        ));

        ResponseEntity<?> respCrear = reservaController.crear(req);
        Reserva creada = (Reserva) respCrear.getBody();
        pagoController.preparar(creada.getId());

        // Intento de retoma con teléfono de otra persona
        Map<String, String> credencialesTercero = Map.of("telefono", "3998887766");
        ResponseEntity<?> respRetomar = pagoController.retomar(creada.getId(), credencialesTercero);

        assertEquals(HttpStatus.FORBIDDEN, respRetomar.getStatusCode());
    }

    @Test
    @DisplayName("4. No se puede retomar una reserva cuya retención temporal ya expiró")
    void testNoSePuedeRetomarReservaExpirada() {
        Reserva req = new Reserva();
        req.setNombreCliente("Cliente Demorado");
        req.setTelefono("3155554433");
        req.setEmail("demorado@example.com");
        req.setMedioPago("WOMPI");
        req.setFechaCita(fechaPrueba);
        req.setHoraCita(horaPrueba);
        req.setItemsInventario(List.of(
                new ItemReserva(servicioPrueba.getId(), "servicio", 1)
        ));

        ResponseEntity<?> respCrear = reservaController.crear(req);
        Reserva creada = (Reserva) respCrear.getBody();
        pagoController.preparar(creada.getId());

        // Forzar expiración
        creada = reservaRepository.findById(creada.getId()).orElseThrow();
        creada.setFechaExpiracionPago(Instant.now().minus(5, ChronoUnit.MINUTES));
        reservaRepository.save(creada);

        Map<String, String> credenciales = Map.of("telefono", "3155554433");
        ResponseEntity<?> respRetomar = pagoController.retomar(creada.getId(), credenciales);

        assertEquals(HttpStatus.BAD_REQUEST, respRetomar.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) respRetomar.getBody();
        assertEquals("RESERVA_EXPIRADA", body.get("error"));

        // El estado en BD pasó a Expirada
        Reserva actualizada = reservaRepository.findById(creada.getId()).orElseThrow();
        assertEquals("Expirada", actualizada.getEstado());
    }

    @Test
    @DisplayName("5. Inventario de productos es estrictamente idempotente al retomar pago")
    void testInventarioNoSeDuplicaAlRetomar() {
        int stockInicial = productoPrueba.getCantidad();

        Reserva req = new Reserva();
        req.setNombreCliente("Comprador Producto");
        req.setTelefono("3201112233");
        req.setEmail("comprador@example.com");
        req.setMedioPago("WOMPI");
        req.setTipoEntrega("pickup");
        req.setItemsInventario(List.of(
                new ItemReserva(productoPrueba.getId(), "producto", 2)
        ));

        ResponseEntity<?> respCrear = reservaController.crear(req);
        assertEquals(HttpStatus.CREATED, respCrear.getStatusCode());
        Reserva creada = (Reserva) respCrear.getBody();

        // Verificar que se descontaron 2 unidades
        Producto prodTrasCrear = productoRepository.findById(productoPrueba.getId()).orElseThrow();
        assertEquals(stockInicial - 2, prodTrasCrear.getCantidad());

        pagoController.preparar(creada.getId());

        // Retomar 3 veces consecutivas
        Map<String, String> credenciales = Map.of("telefono", "3201112233");
        pagoController.retomar(creada.getId(), credenciales);
        pagoController.retomar(creada.getId(), credenciales);
        pagoController.retomar(creada.getId(), credenciales);

        // El stock NO debe haberse vuelto a descontar
        Producto prodTrasRetomar = productoRepository.findById(productoPrueba.getId()).orElseThrow();
        assertEquals(stockInicial - 2, prodTrasRetomar.getCantidad());
    }

    @Test
    @DisplayName("6. Horario retenido bloquea a un tercero pero el titular puede retomar")
    void testConcurrenciaTerceroBloqueadoTitularRetoma() {
        Reserva req1 = new Reserva();
        req1.setNombreCliente("Titular Original");
        req1.setTelefono("3001239999");
        req1.setEmail("titular@example.com");
        req1.setMedioPago("WOMPI");
        req1.setFechaCita(fechaPrueba);
        req1.setHoraCita(horaPrueba);
        req1.setItemsInventario(List.of(new ItemReserva(servicioPrueba.getId(), "servicio", 1)));

        ResponseEntity<?> resp1 = reservaController.crear(req1);
        assertEquals(HttpStatus.CREATED, resp1.getStatusCode());
        Reserva reservaTitular = (Reserva) resp1.getBody();

        pagoController.preparar(reservaTitular.getId());

        // Un tercero intenta reservar el mismo horario
        Reserva req2 = new Reserva();
        req2.setNombreCliente("Tercero");
        req2.setTelefono("3117778888");
        req2.setEmail("tercero@example.com");
        req2.setMedioPago("WOMPI");
        req2.setFechaCita(fechaPrueba);
        req2.setHoraCita(horaPrueba);
        req2.setItemsInventario(List.of(new ItemReserva(servicioPrueba.getId(), "servicio", 1)));

        ResponseEntity<?> resp2 = reservaController.crear(req2);
        assertEquals(HttpStatus.CONFLICT, resp2.getStatusCode());

        // El titular retoma su pago sin conflicto
        ResponseEntity<?> respRetomar = pagoController.retomar(reservaTitular.getId(), Map.of("telefono", "3001239999"));
        assertEquals(HttpStatus.OK, respRetomar.getStatusCode());
    }
}
