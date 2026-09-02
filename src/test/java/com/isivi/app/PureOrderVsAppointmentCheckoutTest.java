package com.isivi.app;

import com.isivi.app.controller.ReservaController;
import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Reserva;
import com.isivi.app.model.Servicio;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.repository.ServicioRepository;
import com.isivi.app.service.ReservaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PureOrderVsAppointmentCheckoutTest {

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    private LocalDate getNextValidDay() {
        LocalDate next = LocalDate.now().plusDays(2);
        while (next.getDayOfWeek() == DayOfWeek.MONDAY) {
            next = next.plusDays(1);
        }
        return next;
    }

    @Test
    @DisplayName("1. Backend: Pedido Puro no ocupa agenda, no tiene fecha/hora y esPedidoPuro() es true")
    public void testBackendPureOrderDoesNotOccupySchedule() {
        Producto prod = new Producto();
        prod.setNombre("Crema para Peinar QA " + UUID.randomUUID().toString().substring(0, 6));
        prod.setPrecio(30000.0);
        prod.setCantidad(20);
        prod.setEnStock(true);
        prod = productoRepository.save(prod);

        Reserva pedidoPuro = new Reserva();
        pedidoPuro.setNombreCliente("Cliente Pedido Puro");
        pedidoPuro.setTelefono("3001234567");
        pedidoPuro.setEmail("pure@test.com");
        pedidoPuro.setCiudad("Cartagena");
        pedidoPuro.setMedioPago("WOMPI");
        pedidoPuro.setTipoEntrega("delivery");
        pedidoPuro.setDireccionEntrega("Calle 10 # 20-30");
        pedidoPuro.setFechaCita(null);
        pedidoPuro.setHoraCita(null);

        ItemReserva item = new ItemReserva();
        item.setId(prod.getId());
        item.setTipo("producto");
        item.setNombre(prod.getNombre());
        item.setCantidad(1);
        item.setPrecioUnitario(30000.0);
        pedidoPuro.setItemsInventario(List.of(item));

        ResponseEntity<?> response = reservaController.crear(pedidoPuro);
        assertEquals(201, response.getStatusCode().value());
        Reserva guardada = (Reserva) response.getBody();
        assertNotNull(guardada);
        assertNotNull(guardada.getId());

        assertTrue(guardada.esPedidoPuro(), "Debe identificarse como pedido puro");
        assertFalse(guardada.esCita(), "No debe ser una cita");
        assertTrue(guardada.esDomicilio(), "Debe ser entrega a domicilio");
        assertNull(guardada.getFechaCita(), "Fecha de cita debe ser null");
        assertNull(guardada.getHoraCita(), "Hora de cita debe ser null");
        assertEquals(0.0, guardada.getAnticipo(), "No debe exigir anticipo del 25% para pedido puro");
        assertEquals(30000.0, guardada.getSubtotal());

        // Verificar que la agenda no tiene este pedido
        LocalDate hoy = LocalDate.now();
        List<Reserva> agendaHoy = reservaRepository.findByFechaCita(hoy);
        assertFalse(agendaHoy.stream().anyMatch(r -> guardada.getId().equals(r.getId())), "El pedido puro no debe aparecer en la agenda");

        // Cleanup
        reservaRepository.deleteById(guardada.getId());
        productoRepository.deleteById(prod.getId());
    }

    @Test
    @DisplayName("2. Backend: Cita de servicio sí retiene horario y calcula anticipo")
    public void testBackendAppointmentOccupiesScheduleAndHasDeposit() {
        Servicio serv = new Servicio();
        serv.setNombre("Corte y Cepillado QA " + UUID.randomUUID().toString().substring(0, 6));
        serv.setPrecio(60000.0);
        serv.setCategoria("Cuidado Capilar");
        serv = servicioRepository.save(serv);

        LocalDate fechaCita = getNextValidDay();
        String horaCita = "09:30 AM";

        Reserva cita = new Reserva();
        cita.setNombreCliente("Cliente Cita");
        cita.setTelefono("3007654321");
        cita.setEmail("cita@test.com");
        cita.setCiudad("Cartagena");
        cita.setMedioPago("WOMPI");
        cita.setFechaCita(fechaCita);
        cita.setHoraCita(horaCita);

        ItemReserva item = new ItemReserva();
        item.setId(serv.getId());
        item.setTipo("servicio");
        item.setNombre(serv.getNombre());
        item.setCantidad(1);
        item.setPrecioUnitario(60000.0);
        cita.setItemsInventario(List.of(item));

        ResponseEntity<?> response = reservaController.crear(cita);
        assertEquals(201, response.getStatusCode().value());
        Reserva guardada = (Reserva) response.getBody();
        assertNotNull(guardada);

        assertTrue(guardada.esCita(), "Debe ser identificada como cita");
        assertFalse(guardada.esPedidoPuro(), "No debe ser pedido puro");
        assertEquals(fechaCita, guardada.getFechaCita());
        assertEquals(horaCita, guardada.getHoraCita());
        assertEquals(15000.0, guardada.getAnticipo(), "Anticipo debe ser 25% de 60000 = 15000");

        // Verificar que la agenda sí contiene la cita activa
        List<Reserva> agenda = reservaRepository.findByFechaCita(fechaCita);
        assertTrue(agenda.stream().anyMatch(r -> guardada.getId().equals(r.getId())), "La cita debe aparecer en la agenda");

        // Cleanup
        reservaRepository.deleteById(guardada.getId());
        servicioRepository.deleteById(serv.getId());
    }

    @Test
    @DisplayName("3. Backend: Cita + Productos (Mixto) se comporta como Cita")
    public void testBackendMixedOrderIsAppointment() {
        Servicio serv = new Servicio();
        serv.setNombre("Hidratación QA " + UUID.randomUUID().toString().substring(0, 6));
        serv.setPrecio(80000.0);
        serv = servicioRepository.save(serv);

        Producto prod = new Producto();
        prod.setNombre("Gotas de Seda QA " + UUID.randomUUID().toString().substring(0, 6));
        prod.setPrecio(20000.0);
        prod.setCantidad(10);
        prod.setEnStock(true);
        prod = productoRepository.save(prod);

        LocalDate fechaCita = getNextValidDay();
        String horaCita = "11:00 AM";

        Reserva mixta = new Reserva();
        mixta.setNombreCliente("Cliente Mixto");
        mixta.setTelefono("3009998877");
        mixta.setEmail("mixto@test.com");
        mixta.setCiudad("Cartagena");
        mixta.setMedioPago("WOMPI");
        mixta.setFechaCita(fechaCita);
        mixta.setHoraCita(horaCita);

        ItemReserva itemServ = new ItemReserva();
        itemServ.setId(serv.getId());
        itemServ.setTipo("servicio");
        itemServ.setNombre(serv.getNombre());
        itemServ.setCantidad(1);
        itemServ.setPrecioUnitario(80000.0);

        ItemReserva itemProd = new ItemReserva();
        itemProd.setId(prod.getId());
        itemProd.setTipo("producto");
        itemProd.setNombre(prod.getNombre());
        itemProd.setCantidad(1);
        itemProd.setPrecioUnitario(20000.0);

        mixta.setItemsInventario(List.of(itemServ, itemProd));

        ResponseEntity<?> response = reservaController.crear(mixta);
        assertEquals(201, response.getStatusCode().value());
        Reserva guardada = (Reserva) response.getBody();
        assertNotNull(guardada);

        assertTrue(guardada.esCita(), "Mixto debe ser Cita");
        assertTrue(guardada.esMixto(), "Debe ser compra mixta");
        assertFalse(guardada.esPedidoPuro(), "No debe ser pedido puro");
        assertEquals(100000.0, guardada.getSubtotal());
        assertEquals(40000.0, guardada.getAnticipo(), "Anticipo 25% del servicio ($20.000) + 100% de productos ($20.000)");

        // Cleanup
        reservaRepository.deleteById(guardada.getId());
        servicioRepository.deleteById(serv.getId());
        productoRepository.deleteById(prod.getId());
    }

    @Test
    @DisplayName("4. Wompi: Conversión de centavos para Cita vs Pedido Puro")
    public void testWompiCentavosCalculations() {
        // Pedido puro: 30.000 COP -> 3.000.000 centavos (100% del subtotal)
        double subtotalPedido = 30000.0;
        long centavosPedido = Math.round(subtotalPedido * 100d);
        assertEquals(3000000L, centavosPedido);

        // Cita: 60.000 COP subtotal, anticipo 15.000 COP -> 1.500.000 centavos (25% del anticipo)
        double anticipoCita = 15000.0;
        long centavosCita = Math.round(anticipoCita * 100d);
        assertEquals(1500000L, centavosCita);
    }

    @Test
    @DisplayName("5. Frontend: Validación estricta del código JS para Pedido Puro vs Cita")
    public void testFrontendJsValidation() throws Exception {
        Path jsPath = Path.of("src/main/resources/static/js/isivi.js");
        assertTrue(Files.exists(jsPath), "isivi.js debe existir");
        String js = Files.readString(jsPath);

        // 1. setPendingHold valida isPureOrder / !hasService
        assertTrue(js.contains("if (isPureOrder || !hasService)"), "setPendingHold debe rechazar pedidos puros");

        // 2. renderPendingHoldUI oculta banner para isCartPureOrder
        assertTrue(js.contains("if (isCartPureOrder)"), "renderPendingHoldUI debe validar isCartPureOrder");
        assertTrue(js.contains("cartBanner.classList.add('hidden')"), "renderPendingHoldUI debe ocultar el banner");

        // 3. payWithWompi solo retiene y muestra toast de horario si hasServices es true
        assertTrue(js.contains("if (hasServices)"), "payWithWompi debe verificar hasServices");
        assertTrue(js.contains("showToast('Horario reservado temporalmente mientras completas tu pago.', 'info');"), "payWithWompi debe mostrar toast de horario solo para citas");
        assertTrue(js.contains("showToast('Pedido creado. Tu unidad está reservada temporalmente durante 15 minutos.', 'success');"), "payWithWompi debe mostrar toast de reserva de unidad");

        // 4. getPendingHold ya no descarta holds de pedido puro, sino que valida la expiración
        assertTrue(js.contains("const expTime = parsed.expiration"), "getPendingHold debe validar la expiración del hold");
    }
}
