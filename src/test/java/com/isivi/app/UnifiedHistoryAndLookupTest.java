package com.isivi.app;

import com.isivi.app.controller.ReservaController;
import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.service.ReservaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UnifiedHistoryAndLookupTest {

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private ReservaRepository reservaRepository;

    @org.junit.jupiter.api.BeforeEach
    public void setup() {
        reservaController.setClock(java.time.Clock.system(java.time.ZoneId.of("America/Bogota")));
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() {
        reservaController.setClock(java.time.Clock.system(java.time.ZoneId.of("America/Bogota")));
    }

    @Test
    @DisplayName("1. Filtrado de reservas por tipo: citas, pedidos y todos")
    public void testFilteringByType() {
        String suffix = "-" + System.currentTimeMillis();

        // 1 Cita
        String uniqueSlot = "11:00 AM";
        Reserva cita = new Reserva();
        cita.setCodigoReserva("ISV-CITA" + suffix);
        cita.setNombreCliente("Cliente Cita");
        cita.setTelefono("301" + String.format("%07d", (int)(System.currentTimeMillis() % 10000000)));
        cita.setFechaCita(LocalDate.now(ZoneId.of("America/Bogota")).plusDays(300));
        cita.setHoraCita(uniqueSlot);
        cita.setEstado("Confirmado");
        cita.setItems(List.of("Balayage Completo"));
        ItemReserva itemServ = new ItemReserva("s1", "servicio", 1);
        itemServ.setNombre("Balayage Completo");
        itemServ.setPrecioUnitario(150000.0);
        cita.setItemsInventario(List.of(itemServ));
        reservaRepository.save(cita);

        // 1 Pedido puro
        Reserva pedido = new Reserva();
        pedido.setCodigoReserva("ISV-PED" + suffix);
        pedido.setNombreCliente("Cliente Pedido");
        pedido.setTelefono("302" + String.format("%07d", (int)(System.currentTimeMillis() % 10000000)));
        pedido.setEstado(ReservaService.PAGO_CONFIRMADO);
        pedido.setTipoEntrega("domicilio");
        pedido.setDireccionEntrega("Bocagrande Carrera 3 # 6-45");
        pedido.setItems(List.of("Mascarilla Hidratante"));
        ItemReserva itemProd = new ItemReserva("p1", "producto", 1);
        itemProd.setNombre("Mascarilla Hidratante");
        itemProd.setPrecioUnitario(55000.0);
        pedido.setItemsInventario(List.of(itemProd));
        reservaRepository.save(pedido);

        // Consultar tipo=citas
        List<Reserva> soloCitas = reservaController.listar(null, false, "citas");
        assertTrue(soloCitas.stream().anyMatch(r -> r.getId().equals(cita.getId())), "Debe incluir la cita");
        assertFalse(soloCitas.stream().anyMatch(r -> r.getId().equals(pedido.getId())), "NO debe incluir el pedido");

        // Consultar tipo=pedidos
        List<Reserva> soloPedidos = reservaController.listar(null, false, "pedidos");
        assertTrue(soloPedidos.stream().anyMatch(r -> r.getId().equals(pedido.getId())), "Debe incluir el pedido");
        assertFalse(soloPedidos.stream().anyMatch(r -> r.getId().equals(cita.getId())), "NO debe incluir la cita");

        // Consultar tipo=todos
        List<Reserva> todos = reservaController.listar(null, false, "todos");
        assertTrue(todos.stream().anyMatch(r -> r.getId().equals(cita.getId())));
        assertTrue(todos.stream().anyMatch(r -> r.getId().equals(pedido.getId())));
    }

    @Test
    @DisplayName("2. Consulta unificada (Mi reserva / Mi pedido) por código y por teléfono")
    public void testUnifiedLookupEndpoint() {
        String suffix = "-" + System.currentTimeMillis();
        String uniquePhone = "329" + String.format("%07d", (int)(System.currentTimeMillis() % 10000000));

        Reserva order = new Reserva();
        order.setCodigoReserva("ISV-LOOK" + suffix);
        order.setNombreCliente("Camila Consulta");
        order.setTelefono(uniquePhone);
        order.setEstado(ReservaService.LISTO_PARA_RECOGER);
        order.setSubtotal(80000.0);
        order.getItems().add("Kit Capilar Reparador");
        order = reservaRepository.save(order);

        // Consulta por código exacto
        ResponseEntity<?> respByCode = reservaController.consultar("ISV-LOOK" + suffix);
        assertEquals(200, respByCode.getStatusCode().value());
        Reserva foundByCode = (Reserva) respByCode.getBody();
        assertNotNull(foundByCode);
        assertEquals(order.getId(), foundByCode.getId());
        assertEquals("Camila Consulta", foundByCode.getNombreCliente());

        // Consulta por teléfono
        ResponseEntity<?> respByPhone = reservaController.consultar(uniquePhone);
        assertEquals(200, respByPhone.getStatusCode().value());
        Reserva foundByPhone = (Reserva) respByPhone.getBody();
        assertNotNull(foundByPhone);
        assertEquals(order.getId(), foundByPhone.getId());

        // Consulta con código inexistente
        ResponseEntity<?> respNotFound = reservaController.consultar("ISV-INEXISTENTE-999");
        assertEquals(404, respNotFound.getStatusCode().value());


    }
}
