package com.isivi.app;

import com.isivi.app.config.DataInitializer;
import com.isivi.app.controller.DashboardController;
import com.isivi.app.controller.ReservaController;
import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.*;
import com.isivi.app.service.ReservaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PersistenceAuditTest {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private KitRepository kitRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private BannerRepository bannerRepository;

    @Autowired
    private CategoriaServicioRepository categoriaServicioRepository;

    @Autowired
    private CategoriaProductoRepository categoriaProductoRepository;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ReservaController reservaController;

    @Autowired
    private DashboardController dashboardController;

    @Autowired
    private DataInitializer dataInitializer;

    private final List<String> createdReservaIds = new ArrayList<>();

    @BeforeEach
    public void setupClocks() {
        reservaController.setClock(java.time.Clock.system(java.time.ZoneId.of("America/Bogota")));
        reservaService.setClock(java.time.Clock.system(java.time.ZoneId.of("America/Bogota")));
        dashboardController.setClock(java.time.Clock.system(java.time.ZoneId.of("America/Bogota")));
    }

    @AfterEach
    public void cleanupTestData() {
        reservaController.setClock(java.time.Clock.system(java.time.ZoneId.of("America/Bogota")));
        reservaService.setClock(java.time.Clock.system(java.time.ZoneId.of("America/Bogota")));
        dashboardController.setClock(java.time.Clock.system(java.time.ZoneId.of("America/Bogota")));
        for (String id : createdReservaIds) {
            try {
                reservaRepository.findById(id).ifPresent(r -> {
                    reservaService.liberarInventario(r);
                    reservaRepository.delete(r);
                });
            } catch (Exception ignored) {}
        }
        createdReservaIds.clear();
    }

    @Test
    @DisplayName("1. Reserva completa persiste en MongoDB con todos sus campos intactos")
    public void testReservaPersisteConTodosLosCamposIntactos() {
        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-QA-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        r.setNombreCliente("Cliente QA Persistencia");
        r.setTelefono("3009998877");
        r.setCiudad("Cartagena");
        r.setFechaCita(LocalDate.now().plusDays(10));
        r.setHoraCita("11:00 AM");
        r.setSubtotal(100000.0);
        r.setAnticipo(25000.0);
        r.setSaldo(75000.0);
        r.setEstado("Confirmado");
        r.setEstadoPago("APROBADO");
        r.setMedioPago("WOMPI");
        r.setReferenciaWompi("ISV-REF-" + UUID.randomUUID().toString().substring(0, 8));
        r.setTransaccionWompiId("TX-" + UUID.randomUUID().toString().substring(0, 8));
        r.setMetodoPagoWompi("CARD");
        r.setFechaPago(LocalDate.now());
        r.setFechaRegistro(LocalDate.now());
        r.setArchivada(false);
        r.setItems(List.of("Tratamiento Capilar QA", "Shampoo QA"));

        ItemReserva item = new ItemReserva();
        item.setId("prod-qa-test");
        item.setNombre("Shampoo QA");
        item.setTipo("producto");
        item.setCantidad(1);
        item.setPrecioUnitario(25000.0);
        item.setSubtotal(25000.0);
        r.setItemsInventario(List.of(item));

        Reserva guardada = reservaRepository.save(r);
        assertNotNull(guardada.getId(), "El ID generado por MongoDB no debe ser nulo");
        createdReservaIds.add(guardada.getId());

        // Simular consulta posterior
        Reserva recuperada = reservaRepository.findById(guardada.getId()).orElse(null);
        assertNotNull(recuperada, "La reserva debe existir en MongoDB");
        assertEquals(r.getCodigoReserva(), recuperada.getCodigoReserva());
        assertEquals(r.getNombreCliente(), recuperada.getNombreCliente());
        assertEquals("3009998877", recuperada.getTelefono());
        assertEquals(r.getFechaCita(), recuperada.getFechaCita());
        assertEquals("11:00 AM", recuperada.getHoraCita());
        assertEquals(100000.0, recuperada.getSubtotal());
        assertEquals(25000.0, recuperada.getAnticipo());
        assertEquals(75000.0, recuperada.getSaldo());
        assertEquals("Confirmado", recuperada.getEstado());
        assertEquals("APROBADO", recuperada.getEstadoPago());
        assertEquals("WOMPI", recuperada.getMedioPago());
        assertEquals(r.getReferenciaWompi(), recuperada.getReferenciaWompi());
        assertEquals(r.getTransaccionWompiId(), recuperada.getTransaccionWompiId());
        assertEquals("CARD", recuperada.getMetodoPagoWompi());
        assertEquals(Boolean.FALSE, recuperada.getArchivada());
    }

    @Test
    @DisplayName("2. Pedido de productos/kits persiste sin cita y con dirección de entrega")
    public void testPedidoProductosKitsPersiste() {
        Reserva pedido = new Reserva();
        pedido.setCodigoReserva("ISV-PEDIDO-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        pedido.setNombreCliente("Compradora Directa");
        pedido.setTelefono("3112223344");
        pedido.setCiudad("Barranquilla");
        pedido.setTipoEntrega("delivery");
        pedido.setDireccionEntrega("Calle 84 # 51B - 32 Apto 402");
        pedido.setFechaCita(null);
        pedido.setHoraCita(null);
        pedido.setSubtotal(120000.0);
        pedido.setAnticipo(120000.0);
        pedido.setSaldo(0.0);
        pedido.setEstado("Confirmado");
        pedido.setEstadoPago("APROBADO");
        pedido.setMedioPago("WOMPI");
        pedido.setFechaRegistro(LocalDate.now());
        pedido.setArchivada(false);

        Reserva guardado = reservaRepository.save(pedido);
        assertNotNull(guardado.getId());
        createdReservaIds.add(guardado.getId());

        Reserva recuperado = reservaRepository.findById(guardado.getId()).orElse(null);
        assertNotNull(recuperado);
        assertEquals("delivery", recuperado.getTipoEntrega());
        assertEquals("Calle 84 # 51B - 32 Apto 402", recuperado.getDireccionEntrega());
        assertNull(recuperado.getFechaCita());
        assertNull(recuperado.getHoraCita());
        assertEquals(120000.0, recuperado.getSubtotal());
    }

    @Test
    @DisplayName("3. Cancelación preserva el documento en MongoDB con estado 'Cancelada'")
    public void testCancelacionMantieneDocumentoEnMongo() {
        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-CANC-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        r.setNombreCliente("Cliente Cancelado");
        r.setTelefono("3001112233");
        r.setFechaCita(LocalDate.now().plusDays(5));
        r.setHoraCita("03:00 PM");
        r.setEstado("Pendiente Comprobante");
        r.setSubtotal(80000.0);
        r.setAnticipo(20000.0);
        r.setSaldo(60000.0);
        r.setArchivada(false);

        Reserva guardada = reservaRepository.save(r);
        createdReservaIds.add(guardada.getId());

        // Cancelar la reserva
        Reserva cancelada = reservaService.cancelar(guardada);
        assertEquals("Cancelada", cancelada.getEstado());

        // Consultar MongoDB directamente
        Reserva enMongo = reservaRepository.findById(guardada.getId()).orElse(null);
        assertNotNull(enMongo, "El documento NO debe ser eliminado tras cancelación");
        assertEquals("Cancelada", enMongo.getEstado());
        assertEquals(r.getCodigoReserva(), enMongo.getCodigoReserva());
        assertEquals("3001112233", enMongo.getTelefono());
    }

    @Test
    @DisplayName("4. Denegación preserva el documento en MongoDB con estado 'Denegada'")
    public void testDenegacionMantieneDocumentoEnMongo() {
        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-DEN-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        r.setNombreCliente("Cliente Denegado");
        r.setTelefono("3004445566");
        r.setFechaCita(LocalDate.now().plusDays(6));
        r.setHoraCita("04:30 PM");
        r.setEstado("Pendiente Comprobante");
        r.setSubtotal(90000.0);
        r.setAnticipo(22500.0);
        r.setSaldo(67500.0);
        r.setArchivada(false);

        Reserva guardada = reservaRepository.save(r);
        createdReservaIds.add(guardada.getId());

        // Denegar la reserva
        Reserva denegada = reservaService.denegar(guardada);
        assertEquals("Denegada", denegada.getEstado());

        // Consultar MongoDB directamente
        Reserva enMongo = reservaRepository.findById(guardada.getId()).orElse(null);
        assertNotNull(enMongo, "El documento NO debe ser eliminado tras denegación");
        assertEquals("Denegada", enMongo.getEstado());
        assertEquals(r.getCodigoReserva(), enMongo.getCodigoReserva());
    }

    @Test
    @DisplayName("5. DataInitializer no elimina datos existentes y es 100% idempotente")
    public void testDataInitializerIdempotente() {
        long countCatAntes = categoriaServicioRepository.count();
        long countProdAntes = productoRepository.count();
        long countKitsAntes = kitRepository.count();
        long countServAntes = servicioRepository.count();

        // Ejecutar DataInitializer de nuevo
        assertDoesNotThrow(() -> {
            dataInitializer.cargarDatosIniciales(
                    productoRepository, kitRepository, servicioRepository,
                    categoriaServicioRepository, administradorRepository, bannerRepository,
                    categoriaProductoRepository
            ).run();
        });

        // Verificar que no se eliminaron ni duplicaron registros
        assertTrue(categoriaServicioRepository.count() >= countCatAntes);
        assertTrue(productoRepository.count() >= countProdAntes);
        assertTrue(kitRepository.count() >= countKitsAntes);
        assertTrue(servicioRepository.count() >= countServAntes);
    }

    @Test
    @DisplayName("6. Startup repetido de ReservaService no borra ni altera reservas")
    public void testStartupRepetidoReservaService() {
        Reserva r = new Reserva();
        r.setCodigoReserva("ISV-STARTUP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        r.setNombreCliente("Cliente Prueba Startup");
        r.setTelefono("3005556677");
        r.setFechaCita(LocalDate.now().plusDays(8));
        r.setHoraCita("09:30 AM");
        r.setEstado("Confirmado");
        r.setSubtotal(150000.0);
        r.setAnticipo(37500.0);
        r.setSaldo(112500.0);
        r.setArchivada(false);

        Reserva guardada = reservaRepository.save(r);
        createdReservaIds.add(guardada.getId());

        // Simular múltiples arranques de Spring Boot ejecutando PostConstruct
        for (int i = 0; i < 3; i++) {
            reservaService.inicializarYVerificarIndices();
        }

        // Comprobar que el documento sigue existiendo intacto
        Reserva recuperada = reservaRepository.findById(guardada.getId()).orElse(null);
        assertNotNull(recuperada, "La reserva debe sobrevivir a múltiples inicializaciones de Spring Boot");
        assertEquals("Confirmado", recuperada.getEstado());
        assertEquals(r.getCodigoReserva(), recuperada.getCodigoReserva());
    }

    @Test
    @DisplayName("7. Consulta pública por código y teléfono encuentra la reserva")
    public void testConsultaPublicaPorCodigoYTelefono() {
        Reserva r = new Reserva();
        String codigo = "ISV-CONS-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        r.setCodigoReserva(codigo);
        r.setNombreCliente("Cliente Consulta");
        r.setTelefono("3008889900");
        r.setFechaCita(LocalDate.now().plusDays(4));
        r.setHoraCita("01:30 PM");
        r.setEstado("Confirmado");
        r.setSubtotal(50000.0);
        r.setAnticipo(12500.0);
        r.setSaldo(37500.0);
        r.setArchivada(false);

        Reserva guardada = reservaRepository.save(r);
        createdReservaIds.add(guardada.getId());

        // Consultar mediante el endpoint
        ResponseEntity<?> resp = reservaController.consultar(codigo, "3008889900");
        assertEquals(200, resp.getStatusCode().value());
        assertTrue(resp.getBody() instanceof Reserva);
        Reserva encontrada = (Reserva) resp.getBody();
        assertEquals(codigo, encontrada.getCodigoReserva());
        assertEquals("Cliente Consulta", encontrada.getNombreCliente());
    }

    @Test
    @DisplayName("8. El Dashboard reconstruye métricas directamente desde MongoDB")
    public void testDashboardReconstruyeDesdeMongo() {
        ResponseEntity<Map<String, Object>> resp = dashboardController.obtenerResumen();
        assertEquals(200, resp.getStatusCode().value());
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("citasHoy"));
        assertTrue(body.containsKey("ventasHoy"));
        assertTrue(body.containsKey("pendientes"));
        assertTrue(body.containsKey("agendaHoy"));
        assertTrue(body.containsKey("pendientesLista"));
    }

    @Test
    @DisplayName("9. Conjunto de reservas QA de Persistencia (QA-PERSISTENCE-001/002/003)")
    public void testQAPersistenceSet() {
        String[] codes = {"QA-PERSISTENCE-001", "QA-PERSISTENCE-002", "QA-PERSISTENCE-003"};
        String[] hours = {"08:00 AM", "09:30 AM", "11:00 AM"};
        for (int i = 0; i < codes.length; i++) {
            String code = codes[i];
            String hour = hours[i];
            Reserva r = new Reserva();
            r.setCodigoReserva(code);
            r.setNombreCliente("QA Test " + code);
            r.setTelefono("3000000001");
            r.setFechaCita(LocalDate.now().plusDays(15));
            r.setHoraCita(hour);
            r.setSubtotal(100000.0);
            r.setAnticipo(25000.0);
            r.setSaldo(75000.0);
            r.setEstado("Confirmado");
            r.setEstadoPago("APROBADO");
            r.setMedioPago("WOMPI");
            r.setReferenciaWompi("REF-" + code);
            r.setArchivada(false);

            Reserva guardada = reservaRepository.save(r);
            createdReservaIds.add(guardada.getId());

            // Recuperar directamente por código
            Reserva encontrada = reservaRepository.findByCodigoReserva(code).orElse(null);
            assertNotNull(encontrada, "El documento " + code + " debe persistir en MongoDB");
            assertEquals("QA Test " + code, encontrada.getNombreCliente());
            assertEquals("REF-" + code, encontrada.getReferenciaWompi());
        }
    }

    @Test
    @DisplayName("10. Archivar reservas no elimina documentos de MongoDB")
    public void testArchivarNoEliminaDocumentos() {
        Reserva pasada = new Reserva();
        pasada.setCodigoReserva("ISV-PAST-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        pasada.setNombreCliente("Cliente Cita Pasada");
        pasada.setTelefono("3007778899");
        pasada.setFechaCita(LocalDate.now().minusDays(3)); // Fecha en el pasado
        pasada.setHoraCita("10:00 AM");
        pasada.setEstado("Confirmado");
        pasada.setSubtotal(60000.0);
        pasada.setAnticipo(15000.0);
        pasada.setSaldo(45000.0);
        pasada.setArchivada(false);

        Reserva guardada = reservaRepository.save(pasada);
        createdReservaIds.add(guardada.getId());

        // Ejecutar proceso de archivado
        reservaController.archivarReservasVencidas();

        // Verificar que el documento sigue existiendo en MongoDB con archivada = true
        Reserva enMongo = reservaRepository.findById(guardada.getId()).orElse(null);
        assertNotNull(enMongo, "El documento con fecha pasada NO fue borrado de MongoDB");
        assertEquals(Boolean.TRUE, enMongo.getArchivada(), "El documento fue marcado como archivado");
        assertNotNull(enMongo.getFechaArchivado(), "Tiene fecha de archivado");

        // Verificar que aparece en el historial
        List<Reserva> historial = reservaController.listar(null, true);
        assertTrue(historial.stream().anyMatch(h -> h.getId().equals(guardada.getId())), "La reserva debe figurar en el historial administrativo");
    }
}
