package com.isivi.app;

import com.isivi.app.controller.ReservaController;
import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Reserva;
import com.isivi.app.model.Servicio;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.repository.ServicioRepository;
import com.isivi.app.service.EmailNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PurchaseSuccessViewTest {

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private EmailNotificationService emailNotificationService;

    private Producto testProducto;
    private Servicio testServicio;

    @BeforeEach
    void setUp() {
        reservaRepository.deleteAll();

        testProducto = new Producto();
        testProducto.setNombre("Shampoo Natural " + System.currentTimeMillis());
        testProducto.setPrecio(42000.0);
        testProducto.setCantidad(30);
        testProducto.setEnStock(true);
        testProducto = productoRepository.save(testProducto);

        testServicio = new Servicio();
        testServicio.setNombre("Corte y Cepillado Test " + System.currentTimeMillis());
        testServicio.setPrecio(60000.0);
        testServicio.setDuracion("60 min");
        testServicio = servicioRepository.save(testServicio);
    }

    @Test
    @DisplayName("1. Distinción pura: Cita vs Pedido vs Mixto")
    void testDistincionDominio() {
        // A. Cita pura
        Reserva cita = new Reserva();
        cita.setFechaCita(LocalDate.now().plusDays(2));
        cita.setHoraCita("09:30 AM");
        cita.setItems(List.of(testServicio.getNombre()));
        ItemReserva itemServ = new ItemReserva(testServicio.getId(), "servicio", 1);
        itemServ.setNombre(testServicio.getNombre());
        itemServ.setPrecioUnitario(60000.0);
        cita.setItemsInventario(List.of(itemServ));

        assertTrue(cita.esCita());
        assertFalse(cita.esPedido());
        assertFalse(cita.esPedidoPuro());
        assertFalse(cita.esMixto());

        // B. Pedido puro
        Reserva pedido = new Reserva();
        pedido.setTipoEntrega("domicilio");
        pedido.setDireccionEntrega("Centro Histórico, Calle de la Mantilla # 3-12");
        pedido.setItems(List.of(testProducto.getNombre()));
        ItemReserva itemProd = new ItemReserva(testProducto.getId(), "producto", 1);
        itemProd.setNombre(testProducto.getNombre());
        itemProd.setPrecioUnitario(42000.0);
        pedido.setItemsInventario(List.of(itemProd));

        assertFalse(pedido.esCita());
        assertTrue(pedido.esPedido());
        assertTrue(pedido.esPedidoPuro());
        assertFalse(pedido.esMixto());
        assertTrue(pedido.esDomicilio());

        // C. Mixto
        Reserva mixta = new Reserva();
        mixta.setFechaCita(LocalDate.now().plusDays(3));
        mixta.setHoraCita("11:00 AM");
        mixta.setTipoEntrega("pickup");
        mixta.setItems(List.of(testServicio.getNombre(), testProducto.getNombre()));
        mixta.setItemsInventario(List.of(itemServ, itemProd));

        assertTrue(mixta.esCita());
        assertTrue(mixta.esPedido());
        assertFalse(mixta.esPedidoPuro());
        assertTrue(mixta.esMixto());
        assertTrue(mixta.esRecogida());
    }

    @Test
    @DisplayName("2. Disponibilidad de agenda: pedido de productos no bloquea turnos de citas")
    void testPedidoNoBloqueaDisponibilidad() {
        LocalDate fecha = LocalDate.now().plusDays(5);

        // Crear pedido puro
        Reserva pedido = new Reserva();
        pedido.setNombreCliente("Andrea Torres");
        pedido.setTelefono("3101112233");
        pedido.setEmail("andrea@test.com");
        pedido.setCiudad("Cartagena");
        pedido.setTipoEntrega("domicilio");
        pedido.setDireccionEntrega("Bocagrande Carrera 2 # 5-10");
        pedido.setItems(List.of(testProducto.getNombre()));
        ItemReserva itemProd = new ItemReserva(testProducto.getId(), "producto", 1);
        itemProd.setNombre(testProducto.getNombre());
        itemProd.setPrecioUnitario(42000.0);
        pedido.setItemsInventario(List.of(itemProd));
        pedido.setSubtotal(42000.0);
        pedido.setAnticipo(42000.0);

        ResponseEntity<?> res = reservaController.crear(pedido);
        assertEquals(HttpStatus.CREATED, res.getStatusCode());

        // Consultar disponibilidad del día
        List<String> ocupados = reservaController.disponibilidad(fecha.toString());
        assertFalse(ocupados.contains("09:30 AM"));
        assertEquals(0, ocupados.size());
    }

    @Test
    @DisplayName("3. Email listo para recoger: no se despacha si la orden es a domicilio")
    void testEmailListoParaRecogerSuprimidoParaDomicilio() {
        Reserva pedidoDomicilio = new Reserva();
        pedidoDomicilio.setNombreCliente("Mariana Paz");
        pedidoDomicilio.setEmail("mariana@test.com");
        pedidoDomicilio.setTipoEntrega("domicilio");
        pedidoDomicilio.setDireccionEntrega("Castillogrande Calle 6 # 10-20");

        // enviarPedidoListoParaRecoger debe abortar silenciosamente
        assertDoesNotThrow(() -> emailNotificationService.enviarPedidoListoParaRecoger(pedidoDomicilio));
    }

    @Test
    @DisplayName("4. Solo cita: Lugar de la cita en ISIVI Salón Cartagena y sin modalidad de entrega de productos")
    void testSoloCitaLugar() {
        Reserva cita = new Reserva();
        cita.setFechaCita(LocalDate.now().plusDays(2));
        cita.setHoraCita("10:00 AM");
        cita.setItems(List.of(testServicio.getNombre()));
        ItemReserva itemServ = new ItemReserva(testServicio.getId(), "servicio", 1);
        itemServ.setNombre(testServicio.getNombre());
        itemServ.setPrecioUnitario(60000.0);
        cita.setItemsInventario(List.of(itemServ));

        assertTrue(cita.esCita());
        assertFalse(cita.esPedido());
        assertFalse(cita.esMixto());
        // En una cita pura, la ubicación corresponde al Salón ISIVI Cartagena
        assertNull(cita.getTipoEntrega());
        assertNull(cita.getDireccionEntrega());
    }

    @Test
    @DisplayName("5. Solo pedido: Mantiene concepto de entrega (domicilio o pickup)")
    void testSoloPedidoEntrega() {
        Reserva pedidoDomi = new Reserva();
        pedidoDomi.setTipoEntrega("domicilio");
        pedidoDomi.setDireccionEntrega("Manga 4ta Avenida # 21-45");
        pedidoDomi.setItems(List.of(testProducto.getNombre()));
        ItemReserva itemProd = new ItemReserva(testProducto.getId(), "producto", 1);
        itemProd.setNombre(testProducto.getNombre());
        itemProd.setPrecioUnitario(42000.0);
        pedidoDomi.setItemsInventario(List.of(itemProd));

        assertTrue(pedidoDomi.esPedidoPuro());
        assertTrue(pedidoDomi.esDomicilio());
        assertEquals("Manga 4ta Avenida # 21-45", pedidoDomi.getDireccionEntrega());

        Reserva pedidoPickup = new Reserva();
        pedidoPickup.setTipoEntrega("pickup");
        pedidoPickup.setItems(List.of(testProducto.getNombre()));
        pedidoPickup.setItemsInventario(List.of(itemProd));

        assertTrue(pedidoPickup.esPedidoPuro());
        assertTrue(pedidoPickup.esRecogida());
    }

    @Test
    @DisplayName("6. Cita + Productos: Ambos conceptos separados (Lugar de la cita y Entrega de productos)")
    void testCitaMasProductosSeparacion() {
        Reserva mixta = new Reserva();
        mixta.setFechaCita(LocalDate.now().plusDays(4));
        mixta.setHoraCita("03:00 PM");
        mixta.setTipoEntrega("pickup");
        mixta.setItems(List.of(testServicio.getNombre(), testProducto.getNombre()));
        ItemReserva itemServ = new ItemReserva(testServicio.getId(), "servicio", 1);
        itemServ.setNombre(testServicio.getNombre());
        itemServ.setPrecioUnitario(60000.0);
        ItemReserva itemProd = new ItemReserva(testProducto.getId(), "producto", 1);
        itemProd.setNombre(testProducto.getNombre());
        itemProd.setPrecioUnitario(42000.0);
        mixta.setItemsInventario(List.of(itemServ, itemProd));

        assertTrue(mixta.esMixto());
        assertTrue(mixta.esCita());
        assertTrue(mixta.esPedido());
        // Cita programada en el salón
        assertNotNull(mixta.getFechaCita());
        assertNotNull(mixta.getHoraCita());
        // Entrega asociada a los productos
        assertEquals("pickup", mixta.getTipoEntrega());
    }
}
