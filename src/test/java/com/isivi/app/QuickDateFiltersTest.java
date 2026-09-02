package com.isivi.app;

import com.isivi.app.controller.ReservaController;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.ReservaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class QuickDateFiltersTest {

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private ReservaRepository reservaRepository;

    private final ZoneId zonaBogota = ZoneId.of("America/Bogota");

    @BeforeEach
    void setUp() {
        reservaController.setClock(java.time.Clock.system(zonaBogota));
        reservaRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        reservaController.setClock(java.time.Clock.system(zonaBogota));
        reservaRepository.deleteAll();
    }

    @Test
    @DisplayName("1. Filtrado por fecha en histórico y agenda")
    void testFiltradoPorFecha() {
        LocalDate hoy = LocalDate.now(zonaBogota);
        LocalDate ayer = hoy.minusDays(1);

        Reserva r1 = new Reserva();
        r1.setNombreCliente("Cliente Hoy");
        r1.setTelefono("3110001122");
        r1.setCodigoReserva("ISV-1001");
        r1.setFechaCita(hoy);
        r1.setHoraCita("11:30 PM");
        r1.setEstado("Confirmado");
        r1.setArchivada(false);
        reservaRepository.save(r1);

        Reserva r2 = new Reserva();
        r2.setNombreCliente("Cliente Ayer");
        r2.setTelefono("3110003344");
        r2.setCodigoReserva("ISV-1002");
        r2.setFechaCita(ayer);
        r2.setHoraCita("11:00 AM");
        r2.setEstado("Confirmado");
        r2.setArchivada(false);
        reservaRepository.save(r2);

        List<Reserva> listaHoy = reservaController.listar(hoy.toString(), false);
        assertEquals(1, listaHoy.size());
        assertEquals("ISV-1001", listaHoy.get(0).getCodigoReserva());

        List<Reserva> listaAyer = reservaController.listar(ayer.toString(), true);
        assertEquals(1, listaAyer.size());
        assertEquals("ISV-1002", listaAyer.get(0).getCodigoReserva());
    }
}

