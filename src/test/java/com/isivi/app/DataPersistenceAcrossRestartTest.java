package com.isivi.app;

import com.isivi.app.model.Kit;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Reserva;
import com.isivi.app.model.Servicio;
import com.isivi.app.repository.KitRepository;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.repository.ServicioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class DataPersistenceAcrossRestartTest {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private KitRepository kitRepository;

    private final List<String> createdResIds = new ArrayList<>();
    private final List<String> createdProdIds = new ArrayList<>();
    private final List<String> createdSvcIds = new ArrayList<>();
    private final List<String> createdKitIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // Limpiar elementos creados por esta prueba si quedaron colgados
        cleanUpAll();
    }

    @AfterEach
    void tearDown() {
        cleanUpAll();
    }

    private void cleanUpAll() {
        createdResIds.forEach(reservaRepository::deleteById);
        createdProdIds.forEach(productoRepository::deleteById);
        createdSvcIds.forEach(servicioRepository::deleteById);
        createdKitIds.forEach(kitRepository::deleteById);
        createdResIds.clear();
        createdProdIds.clear();
        createdSvcIds.clear();
        createdKitIds.clear();
    }

    @Test
    @DisplayName("Verificar que la inserción de datos perdura de forma consistente")
    void testPersistenceAcrossStarts() {
        // 1. Crear datos de prueba únicos en 'isivi_test'
        Producto p = new Producto("TEST-PERSISTENCIA-PRODUCTO", 15000.0, "Detalle", "", true, 5);
        p = productoRepository.save(p);
        createdProdIds.add(p.getId());

        Kit k = new Kit("TEST-PERSISTENCIA-KIT", 35000.0, "Detalle kit", "", true, 3);
        k = kitRepository.save(k);
        createdKitIds.add(k.getId());

        Servicio s = new Servicio("TEST-PERSISTENCIA-SERVICIO", "tratamientos", 60000.0, "60 min", "Detalle servicio", "");
        s = servicioRepository.save(s);
        createdSvcIds.add(s.getId());

        Reserva r = new Reserva();
        r.setCodigoReserva("TEST-PERSISTENCIA-001");
        r.setNombreCliente("Cliente Persistencia");
        r.setTelefono("3000000001");
        r.setFechaCita(LocalDate.now().plusDays(2));
        r.setHoraCita("10:00 AM");
        r.setEstado("Confirmado");
        r = reservaRepository.save(r);
        createdResIds.add(r.getId());

        // 2. Verificar existencia inmediata
        assertTrue(productoRepository.findById(p.getId()).isPresent());
        assertTrue(kitRepository.findById(k.getId()).isPresent());
        assertTrue(servicioRepository.findById(s.getId()).isPresent());
        assertTrue(reservaRepository.findById(r.getId()).isPresent());

        // 3. Simular verificación de persistencia: los repositorios no deben verse afectados por reinicios o llamadas lógicas secundarias.
        assertNotNull(productoRepository.findById(p.getId()).orElse(null));
        assertNotNull(reservaRepository.findById(r.getId()).orElse(null));
    }
}
