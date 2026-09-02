package com.isivi.app;

import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Reserva;
import com.isivi.app.model.Servicio;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.repository.ServicioRepository;
import com.isivi.app.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class MixedReservationExpirationLifecycleTest {

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

    @BeforeEach
    public void setup() {
        reservaRepository.deleteAll();
        servicioRepository.deleteAll();
        productoRepository.deleteAll();

        servicioPrueba = new Servicio("Balayage", "Peluquería", 100000.0, "120 min", "Desc", "");
        servicioPrueba = servicioRepository.save(servicioPrueba);

        productoPrueba = new Producto("Shampoo", 50000.0, "Desc", "", true, 10);
        productoPrueba = productoRepository.save(productoPrueba);
    }

    @Test
    public void testMixedReservationExpirationLifecycle() {
        // 1. Crear reserva mixta
        Reserva req = new Reserva();
        req.setNombreCliente("Cliente Mixto Expirado");
        req.setTelefono("3112223344");
        req.setFechaCita(LocalDate.now().plusDays(3));
        req.setHoraCita("09:30 AM");
        req.setItemsInventario(List.of(
                new ItemReserva(servicioPrueba.getId(), "servicio", 1),
                new ItemReserva(productoPrueba.getId(), "producto", 1)
        ));

        Reserva preparada = reservaService.prepararNuevaReserva(req);
        preparada.setCodigoReserva("ISV-MIX-EXP");
        Reserva guardada = reservaRepository.save(preparada);

        // Validar stock reservado
        Producto prodDb = productoRepository.findById(productoPrueba.getId()).orElseThrow();
        assertEquals(9, prodDb.getCantidad());

        // Simular expiración del Hold
        Reserva expirada = reservaService.marcarExpirada(guardada);

        // 2. Verificar liberación de stock
        Producto prodDbPostExp = productoRepository.findById(productoPrueba.getId()).orElseThrow();
        assertEquals(10, prodDbPostExp.getCantidad());

        // 3. Verificar estado final
        assertEquals("Pendiente Comprobante", expirada.getEstado());
        assertNull(expirada.getFechaExpiracionPago());

        // 4. Verificar que solo contiene el servicio
        assertEquals(1, expirada.getItemsInventario().size());
        assertEquals("servicio", expirada.getItemsInventario().get(0).getTipo());
    }
}
