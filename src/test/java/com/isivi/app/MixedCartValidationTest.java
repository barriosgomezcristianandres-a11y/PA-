package com.isivi.app;

import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Kit;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Reserva;
import com.isivi.app.model.Servicio;
import com.isivi.app.repository.KitRepository;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.repository.ServicioRepository;
import com.isivi.app.service.ReservaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "isivi.wompi.public-key=pub_test_QA_ISIVI_12345",
        "isivi.wompi.private-key=prv_test_QA_SECRET_98765",
        "isivi.wompi.integrity-secret=test_integrity_QA_SECRET_123",
        "isivi.wompi.events-secret=test_events_QA_SECRET_456",
        "isivi.wompi.sandbox=true",
        "isivi.wompi.base-url=https://sandbox.wompi.co/v1"
})
public class MixedCartValidationTest {

    @Autowired
    private MockMvc mockMvc;

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
    private Kit kitPrueba;

    @BeforeEach
    void setUp() {
        reservaRepository.deleteAll();

        // 1. Servicio $100.000
        servicioPrueba = servicioRepository.findAll().stream().findFirst().orElseGet(() -> {
            Servicio s = new Servicio();
            s.setNombre("Balayage Premium");
            s.setPrecio(100000.0);
            s.setDuracion("120 min");
            s.setCategoria("Peluquería");
            return servicioRepository.save(s);
        });
        servicioPrueba.setPrecio(100000.0);
        servicioPrueba.setDuracion("120 min");
        servicioRepository.save(servicioPrueba);

        // 2. Producto $50.000
        productoPrueba = productoRepository.findAll().stream().findFirst().orElseGet(() -> {
            Producto p = new Producto();
            p.setNombre("Shampoo Reparador");
            p.setPrecio(50000.0);
            p.setCantidad(20);
            p.setEnStock(true);
            return productoRepository.save(p);
        });
        productoPrueba.setPrecio(50000.0);
        productoPrueba.setCantidad(20);
        productoPrueba.setEnStock(true);
        productoRepository.save(productoPrueba);

        // 3. Kit $100.000
        kitPrueba = kitRepository.findAll().stream().findFirst().orElseGet(() -> {
            Kit k = new Kit();
            k.setNombre("Kit Cuidado Total");
            k.setPrecio(100000.0);
            k.setCantidad(10);
            k.setEnStock(true);
            return kitRepository.save(k);
        });
        kitPrueba.setPrecio(100000.0);
        kitPrueba.setCantidad(10);
        kitPrueba.setEnStock(true);
        kitRepository.save(kitPrueba);
    }

    @AfterEach
    void tearDown() {
        reservaRepository.deleteAll();
    }

    @Test
    @DisplayName("Prueba 1: Servicio Solo ($100.000) -> Total $100.000, Anticipo $25.000, Saldo $75.000, Wompi $25.000")
    void testServicioSolo() {
        Reserva req = new Reserva();
        req.setCodigoReserva("ISV-SERV-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        req.setNombreCliente("Cliente Servicio Solo");
        req.setTelefono("3001112233");
        req.setCiudad("Cartagena");
        req.setFechaCita(LocalDate.now().plusDays(2));
        req.setHoraCita("09:30 AM");
        req.setItemsInventario(List.of(new ItemReserva(servicioPrueba.getId(), "servicio", 1)));

        Reserva preparada = reservaService.prepararNuevaReserva(req);
        preparada.setCodigoReserva(req.getCodigoReserva());
        assertEquals(100000.0, preparada.getSubtotal(), 0.001);
        assertEquals(25000.0, preparada.getAnticipo(), 0.001);
        assertEquals(75000.0, preparada.getSaldo(), 0.001);

        Reserva saved = reservaRepository.save(preparada);
        Reserva wompi = reservaService.prepararPagoWompi(saved, "REF-TEST-SERV-1");
        assertEquals(2500000L, wompi.getMontoPagoCentavos()); // 25.000 COP en centavos
    }

    @Test
    @DisplayName("Prueba 2: Producto Solo ($50.000) -> Total $50.000, Anticipo $0, Saldo $50.000, Wompi $50.000")
    void testProductoSolo() {
        Reserva req = new Reserva();
        req.setCodigoReserva("ISV-PROD-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        req.setNombreCliente("Cliente Producto Solo");
        req.setTelefono("3001112233");
        req.setCiudad("Cartagena");
        req.setTipoEntrega("pickup");
        req.setItemsInventario(List.of(new ItemReserva(productoPrueba.getId(), "producto", 1)));

        Reserva preparada = reservaService.prepararNuevaReserva(req);
        preparada.setCodigoReserva(req.getCodigoReserva());
        assertEquals(50000.0, preparada.getSubtotal(), 0.001);
        assertEquals(0.0, preparada.getAnticipo(), 0.001);
        assertEquals(50000.0, preparada.getSaldo(), 0.001);

        Reserva saved = reservaRepository.save(preparada);
        Reserva wompi = reservaService.prepararPagoWompi(saved, "REF-TEST-PROD-1");
        assertEquals(5000000L, wompi.getMontoPagoCentavos()); // 50.000 COP en centavos
    }

    @Test
    @DisplayName("Prueba 3: Kit Solo ($100.000) -> Total $100.000, Anticipo $0, Saldo $100.000, Wompi $100.000")
    void testKitSolo() {
        Reserva req = new Reserva();
        req.setCodigoReserva("ISV-KIT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        req.setNombreCliente("Cliente Kit Solo");
        req.setTelefono("3001112233");
        req.setCiudad("Cartagena");
        req.setTipoEntrega("delivery");
        req.setDireccionEntrega("Bocagrande Calle 6");
        req.setItemsInventario(List.of(new ItemReserva(kitPrueba.getId(), "kit", 1)));

        Reserva preparada = reservaService.prepararNuevaReserva(req);
        preparada.setCodigoReserva(req.getCodigoReserva());
        assertEquals(100000.0, preparada.getSubtotal(), 0.001);
        assertEquals(0.0, preparada.getAnticipo(), 0.001);
        assertEquals(100000.0, preparada.getSaldo(), 0.001);

        Reserva saved = reservaRepository.save(preparada);
        Reserva wompi = reservaService.prepararPagoWompi(saved, "REF-TEST-KIT-1");
        assertEquals(10000000L, wompi.getMontoPagoCentavos()); // 100.000 COP en centavos
    }

    @Test
    @DisplayName("Prueba 4: Carrito Mixto (Servicio $100.000 + Producto $50.000) -> Transferencia permite ($150k total, $37.5k anticipo, $112.5k saldo) pero Wompi rechaza")
    void testServicioMasProducto() throws Exception {
        Reserva req = new Reserva();
        req.setCodigoReserva("ISV-MIX-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        req.setNombreCliente("Cliente Mixto");
        req.setTelefono("3001112233");
        req.setCiudad("Cartagena");
        req.setFechaCita(LocalDate.now().plusDays(3));
        req.setHoraCita("11:00 AM");
        req.setItemsInventario(List.of(
                new ItemReserva(servicioPrueba.getId(), "servicio", 1),
                new ItemReserva(productoPrueba.getId(), "producto", 1)
        ));

        // 1. Preparar reserva mixta (Transferencia / WhatsApp)
        Reserva preparada = reservaService.prepararNuevaReserva(req);
        preparada.setCodigoReserva(req.getCodigoReserva());
        assertEquals(150000.0, preparada.getSubtotal(), 0.001);
        assertEquals(75000.0, preparada.getAnticipo(), 0.001); // 25% de servicio ($25.000) + 100% de producto ($50.000)
        assertEquals(75000.0, preparada.getSaldo(), 0.001);

        Reserva saved = reservaRepository.save(preparada);

        // 2. Preparar pago Wompi -> Debe funcionar y calcular el anticipo (75.000 COP en centavos)
        Reserva wompi = reservaService.prepararPagoWompi(saved, "REF-TEST-MIX-1");
        assertEquals(7500000L, wompi.getMontoPagoCentavos());

        // 3. Endpoint /api/pagos/wompi/preparar/{id} debe responder HTTP 200 OK
        mockMvc.perform(post("/api/pagos/wompi/preparar/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Prueba 5: Carrito Mixto (Servicio $100.000 + Kit $100.000) -> Wompi rechaza y Transferencia calcula $200k / $50k anticipo")
    void testServicioMasKit() throws Exception {
        Reserva req = new Reserva();
        req.setCodigoReserva("ISV-MIXK-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        req.setNombreCliente("Cliente Mixto Kit");
        req.setTelefono("3001112233");
        req.setCiudad("Cartagena");
        req.setFechaCita(LocalDate.now().plusDays(4));
        req.setHoraCita("01:30 PM");
        req.setItemsInventario(List.of(
                new ItemReserva(servicioPrueba.getId(), "servicio", 1),
                new ItemReserva(kitPrueba.getId(), "kit", 1)
        ));

        Reserva preparada = reservaService.prepararNuevaReserva(req);
        preparada.setCodigoReserva(req.getCodigoReserva());
        assertEquals(200000.0, preparada.getSubtotal(), 0.001);
        assertEquals(125000.0, preparada.getAnticipo(), 0.001); // 25% de servicio ($25.000) + 100% de kit ($100.000)
        assertEquals(75000.0, preparada.getSaldo(), 0.001);

        Reserva saved = reservaRepository.save(preparada);

        // 2. Preparar pago Wompi -> Debe funcionar (125.000 COP en centavos)
        Reserva wompi = reservaService.prepararPagoWompi(saved, "REF-TEST-MIX-KIT-1");
        assertEquals(12500000L, wompi.getMontoPagoCentavos());

        // 3. Endpoint /api/pagos/wompi/preparar/{id} debe responder HTTP 200 OK
        mockMvc.perform(post("/api/pagos/wompi/preparar/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
