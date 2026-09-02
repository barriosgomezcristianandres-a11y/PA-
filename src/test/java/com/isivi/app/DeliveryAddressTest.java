package com.isivi.app;

import com.isivi.app.controller.DashboardController;
import com.isivi.app.controller.ReservaController;
import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Kit;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.KitRepository;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DeliveryAddressTest {

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private DashboardController dashboardController;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private KitRepository kitRepository;

    private Producto testProducto;
    private Kit testKit;

    @BeforeEach
    void setUp() {
        reservaRepository.deleteAll();

        testProducto = new Producto();
        testProducto.setNombre("Óleo Reparador Test " + System.currentTimeMillis());
        testProducto.setCategoriaId("oleos");
        testProducto.setPrecio(45000.0);
        testProducto.setCantidad(50);
        testProducto.setEnStock(true);
        testProducto = productoRepository.save(testProducto);

        testKit = new Kit();
        testKit.setNombre("Kit Capilar Test " + System.currentTimeMillis());
        testKit.setPrecio(95000.0);
        testKit.setCantidad(20);
        testKit.setEnStock(true);
        testKit = kitRepository.save(testKit);
    }

    @Test
    @DisplayName("1. Domicilio sin dirección debe fallar con 400 y DIRECCION_REQUERIDA")
    void testDomicilioSinDireccionFallaCon400() {
        Reserva order = new Reserva();
        order.setNombreCliente("Laura Gómez");
        order.setTelefono("3001234567");
        order.setEmail("laura@test.com");
        order.setCiudad("Cartagena");
        order.setTipoEntrega("domicilio");
        order.setDireccionEntrega(""); // Vacío
        order.setItems(List.of(testProducto.getNombre()));
        ItemReserva item = new ItemReserva(testProducto.getId(), "producto", 1);
        item.setNombre(testProducto.getNombre());
        item.setPrecioUnitario(45000.0);
        order.setItemsInventario(List.of(item));
        order.setSubtotal(45000.0);
        order.setAnticipo(45000.0);

        ResponseEntity<?> response = reservaController.crear(order);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("DIRECCION_REQUERIDA", body.get("code"));
        assertEquals(false, body.get("ok"));
    }

    @Test
    @DisplayName("2. Domicilio con dirección válida debe crearse y persistirse correctamente con 201")
    void testDomicilioConDireccionValidaExito() {
        Reserva order = new Reserva();
        order.setNombreCliente("Laura Gómez");
        order.setTelefono("3001234567");
        order.setEmail("laura@test.com");
        order.setCiudad("Cartagena");
        order.setTipoEntrega("domicilio");
        order.setDireccionEntrega("Bocagrande, Carrera 3 # 6-45, Apto 802");
        order.setItems(List.of(testProducto.getNombre()));
        ItemReserva item = new ItemReserva(testProducto.getId(), "producto", 1);
        item.setNombre(testProducto.getNombre());
        item.setPrecioUnitario(45000.0);
        order.setItemsInventario(List.of(item));
        order.setSubtotal(45000.0);
        order.setAnticipo(45000.0);

        ResponseEntity<?> response = reservaController.crear(order);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        Reserva creada = (Reserva) response.getBody();
        assertNotNull(creada);
        assertTrue(creada.esDomicilio());
        assertFalse(creada.esRecogida());
        assertTrue(creada.esPedidoPuro());
        assertFalse(creada.esCita());
        assertEquals("Bocagrande, Carrera 3 # 6-45, Apto 802", creada.getDireccionEntrega());

        // Consulta unificada por código
        ResponseEntity<?> consultadaRes = reservaController.consultar(creada.getCodigoReserva());
        assertEquals(HttpStatus.OK, consultadaRes.getStatusCode());
        Reserva consultada = (Reserva) consultadaRes.getBody();
        assertNotNull(consultada);
        assertEquals("domicilio", consultada.getTipoEntrega());
        assertNull(consultada.getDireccionEntrega());
    }

    @Test
    @DisplayName("3. Recogida en tienda (pickup) no exige dirección de entrega")
    void testRecogidaEnTiendaNoExigeDireccion() {
        Reserva order = new Reserva();
        order.setNombreCliente("Carlos Ruiz");
        order.setTelefono("3054449715");
        order.setEmail("carlos@test.com");
        order.setCiudad("Cartagena");
        order.setTipoEntrega("pickup");
        order.setDireccionEntrega(null);
        order.setItems(List.of(testKit.getNombre()));
        ItemReserva item = new ItemReserva(testKit.getId(), "kit", 1);
        item.setNombre(testKit.getNombre());
        item.setPrecioUnitario(95000.0);
        order.setItemsInventario(List.of(item));
        order.setSubtotal(95000.0);
        order.setAnticipo(95000.0);

        ResponseEntity<?> response = reservaController.crear(order);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        Reserva creada = (Reserva) response.getBody();
        assertNotNull(creada);
        assertTrue(creada.esRecogida());
        assertFalse(creada.esDomicilio());
    }

    @Test
    @DisplayName("4. Pedido puro de productos no aparece en la agenda de hoy ni próximas citas del dashboard")
    void testPedidoPuroNoApareceEnAgenda() {
        Reserva order = new Reserva();
        order.setCodigoReserva("ISV-ORD-TEST-123");
        order.setNombreCliente("Sofía Castro");
        order.setTelefono("3009998877");
        order.setEmail("sofia@test.com");
        order.setCiudad("Cartagena");
        order.setTipoEntrega("domicilio");
        order.setDireccionEntrega("Manga, Calle Real # 21-10");
        order.setItems(List.of(testProducto.getNombre()));
        ItemReserva item = new ItemReserva(testProducto.getId(), "producto", 1);
        item.setNombre(testProducto.getNombre());
        item.setPrecioUnitario(38000.0);
        order.setItemsInventario(List.of(item));
        order.setSubtotal(38000.0);
        order.setAnticipo(38000.0);

        reservaRepository.save(order);

        // Dashboard metrics
        ResponseEntity<Map<String, Object>> dashRes = dashboardController.obtenerResumen();
        assertEquals(HttpStatus.OK, dashRes.getStatusCode());
        Map<String, Object> dash = dashRes.getBody();
        assertNotNull(dash);
        assertEquals(0, ((Number) dash.get("citasHoy")).intValue());
        assertEquals(0, ((List<?>) dash.get("agendaHoy")).size());
        assertEquals(0, ((List<?>) dash.get("proximasCitas")).size());

        // Admin Orders list must include it
        List<Reserva> pedidosAdmin = reservaController.listar(null, false, "pedidos");
        assertEquals(1, pedidosAdmin.size());
        Reserva ped = pedidosAdmin.get(0);
        assertEquals("Sofía Castro", ped.getNombreCliente());
        assertEquals("domicilio", ped.getTipoEntrega());
        assertEquals("Manga, Calle Real # 21-10", ped.getDireccionEntrega());
    }
}
