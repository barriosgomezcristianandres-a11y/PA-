package com.isivi.app;

import com.isivi.app.controller.ReservaController;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.service.ReservaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class HistoryEndpointTest {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private ReservaService reservaService;

    private final List<String> createdIds = new ArrayList<>();

    @AfterEach
    public void cleanup() {
        for (String id : createdIds) {
            try {
                reservaRepository.findById(id).ifPresent(r -> {
                    reservaService.liberarInventario(r);
                    reservaRepository.delete(r);
                });
            } catch (Exception ignored) {}
        }
        createdIds.clear();
    }

    @Test
    @DisplayName("1. Reserva activa no aparece en historial y reserva archivada sí aparece")
    public void testActivaVsArchivadaEnHistorial() {
        // 1. Reserva Activa (fecha futura)
        Reserva activa = new Reserva();
        activa.setCodigoReserva("ISV-ACT-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        activa.setNombreCliente("Cliente Activo");
        activa.setTelefono("3001110001");
        activa.setFechaCita(LocalDate.now().plusDays(5));
        activa.setHoraCita("09:30 AM");
        activa.setSubtotal(80000.0);
        activa.setAnticipo(20000.0);
        activa.setSaldo(60000.0);
        activa.setEstado("Confirmado");
        activa.setArchivada(false);
        Reserva activaGuardada = reservaRepository.save(activa);
        final String activaId = activaGuardada.getId();
        createdIds.add(activaId);

        // 2. Reserva Archivada
        Reserva archivada = new Reserva();
        archivada.setCodigoReserva("ISV-ARC-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        archivada.setNombreCliente("Cliente Archivado");
        archivada.setTelefono("3001110002");
        archivada.setFechaCita(LocalDate.now().minusDays(10));
        archivada.setHoraCita("11:00 AM");
        archivada.setSubtotal(100000.0);
        archivada.setAnticipo(25000.0);
        archivada.setSaldo(75000.0);
        archivada.setEstado("Confirmado");
        archivada.setArchivada(true);
        archivada.setFechaArchivado(LocalDate.now().minusDays(9));
        Reserva archivadaGuardada = reservaRepository.save(archivada);
        final String archivadaId = archivadaGuardada.getId();
        createdIds.add(archivadaId);

        // Consultar /api/reservas (activas)
        List<Reserva> activasResp = reservaController.listar(null, false);
        assertTrue(activasResp.stream().anyMatch(r -> r.getId().equals(activaId)), "La reserva activa debe estar en la lista activa");
        assertFalse(activasResp.stream().anyMatch(r -> r.getId().equals(archivadaId)), "La reserva archivada NO debe estar en la lista activa");

        // Consultar /api/reservas?historial=true
        List<Reserva> historialResp = reservaController.listar(null, true);
        assertFalse(historialResp.stream().anyMatch(r -> r.getId().equals(activaId)), "La reserva activa NO debe estar en el historial");
        assertTrue(historialResp.stream().anyMatch(r -> r.getId().equals(archivadaId)), "La reserva archivada SÍ debe estar en el historial");
    }

    @Test
    @DisplayName("2. Reserva Cancelada + archivada aparece en el historial")
    public void testCanceladaArchivadaEnHistorial() {
        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-CANC-HIST-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        r.setNombreCliente("Cliente Cancelado Historial");
        r.setTelefono("3002220002");
        r.setFechaCita(LocalDate.now().minusDays(4));
        r.setHoraCita("03:00 PM");
        r.setEstado("Cancelada");
        r.setSubtotal(50000.0);
        r.setAnticipo(12500.0);
        r.setSaldo(37500.0);
        r.setArchivada(true);
        r.setFechaArchivado(LocalDate.now().minusDays(3));
        Reserva guardada = reservaRepository.save(r);
        final String guardadaId = guardada.getId();
        createdIds.add(guardadaId);

        List<Reserva> historial = reservaController.listar(null, true);
        assertTrue(historial.stream().anyMatch(h -> h.getId().equals(guardadaId)), "La reserva cancelada archivada debe figurar en el historial");
    }

    @Test
    @DisplayName("3. Reserva Denegada + archivada aparece en el historial")
    public void testDenegadaArchivadaEnHistorial() {
        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-DEN-HIST-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        r.setNombreCliente("Cliente Denegado Historial");
        r.setTelefono("3003330003");
        r.setFechaCita(LocalDate.now().minusDays(5));
        r.setHoraCita("04:30 PM");
        r.setEstado("Denegada");
        r.setSubtotal(70000.0);
        r.setAnticipo(17500.0);
        r.setSaldo(52500.0);
        r.setArchivada(true);
        r.setFechaArchivado(LocalDate.now().minusDays(4));
        Reserva guardada = reservaRepository.save(r);
        final String guardadaId = guardada.getId();
        createdIds.add(guardadaId);

        List<Reserva> historial = reservaController.listar(null, true);
        assertTrue(historial.stream().anyMatch(h -> h.getId().equals(guardadaId)), "La reserva denegada archivada debe figurar en el historial");
    }

    @Test
    @DisplayName("4. Pedido de productos sin fecha de cita + archivado aparece en el historial")
    public void testPedidoSinCitaArchivadoEnHistorial() {
        Reserva pedido = new Reserva();
        pedido.setCodigoReserva("ISV-PED-HIST-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        pedido.setNombreCliente("Comprador Productos Histórico");
        pedido.setTelefono("3004440004");
        pedido.setFechaCita(null);
        pedido.setHoraCita(null);
        pedido.setFechaRegistro(LocalDate.now().minusDays(2));
        pedido.setSubtotal(150000.0);
        pedido.setAnticipo(150000.0);
        pedido.setSaldo(0.0);
        pedido.setEstado("Confirmado");
        pedido.setEstadoPago("APROBADO");
        pedido.setArchivada(true);
        pedido.setFechaArchivado(LocalDate.now().minusDays(1));
        Reserva guardada = reservaRepository.save(pedido);
        final String guardadaId = guardada.getId();
        createdIds.add(guardadaId);

        List<Reserva> historial = reservaController.listar(null, true);
        assertTrue(historial.stream().anyMatch(h -> h.getId().equals(guardadaId)), "El pedido sin cita archivado debe figurar en el historial");
    }

    @Test
    @DisplayName("5. Caso especial QA-HISTORIAL-001: Creación, archivado y presencia en historial")
    public void testQAHistorial001() {
        String code = "QA-HISTORIAL-001";
        Reserva r = new Reserva();
        r.setCodigoReserva(code);
        r.setNombreCliente("QA Test Historial 001");
        r.setTelefono("3005550005");
        r.setFechaCita(LocalDate.now().minusDays(1));
        r.setHoraCita("01:30 PM");
        r.setSubtotal(120000.0);
        r.setAnticipo(30000.0);
        r.setSaldo(90000.0);
        r.setEstado("Confirmado");
        r.setEstadoPago("APROBADO");
        r.setMedioPago("WOMPI");
        r.setReferenciaWompi("REF-" + code);
        r.setArchivada(false); // Creada no archivada

        Reserva guardada = reservaRepository.save(r);
        final String guardadaId = guardada.getId();
        createdIds.add(guardadaId);

        // Ejecutar archivado automático
        reservaController.archivarReservasVencidas();

        // Verificar que en MongoDB pasó a archivada = true
        Reserva enMongo = reservaRepository.findById(guardadaId).orElse(null);
        assertNotNull(enMongo);
        assertEquals(Boolean.TRUE, enMongo.getArchivada());
        assertNotNull(enMongo.getFechaArchivado());

        // Consultar endpoint de historial
        List<Reserva> historial = reservaController.listar(null, true);
        assertTrue(historial.stream().anyMatch(h -> h.getCodigoReserva().equals(code)), "QA-HISTORIAL-001 debe aparecer en /api/reservas?historial=true");

        // Consultar por código y teléfono
        ResponseEntity<?> consulta = reservaController.consultar(code, "3005550005");
        assertEquals(200, consulta.getStatusCode().value());
        assertTrue(consulta.getBody() instanceof Reserva);
        assertEquals(code, ((Reserva) consulta.getBody()).getCodigoReserva());
    }
}
