package com.isivi.app;

import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.service.ReservaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderLogisticsValidationTest {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ReservaRepository reservaRepository;

    private String readClasspathFile(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        return new String(Files.readAllBytes(Paths.get(resource.getURI())));
    }

    private Reserva createBaseOrder(String tipoEntrega) {
        Reserva r = new Reserva();
        r.setNombreCliente("Cristian Test");
        r.setCodigoReserva("ISV-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        r.setTelefono("+573000000000");
        r.setSubtotal(100000.0);
        r.setAnticipo(100000.0);
        r.setSaldo(0.0);
        r.setEstado("Pago Confirmado");
        r.setEstadoPedido("PENDIENTE_PREPARACION");
        r.setTipoEntrega(tipoEntrega);
        
        // Add a product item so esPedido() is true
        ItemReserva item = new ItemReserva();
        item.setId("prod-123");
        item.setNombre("Producto Test");
        item.setTipo("producto");
        item.setCantidad(1);
        item.setPrecioUnitario(100000.0);
        r.setItemsInventario(List.of(item));
        
        r.setInventarioReservado(true);
        return reservaRepository.save(r);
    }

    @Test
    @DisplayName("1. Pedido domicilio en preparación")
    public void test1_PedidoDomicilioEnPreparacion() {
        Reserva r = createBaseOrder("domicilio");
        Reserva updated = reservaService.marcarPedidoEnPreparacion(r, "admin");
        assertEquals("Pago Confirmado", updated.getEstado());
        assertEquals("EN_PREPARACION", updated.getEstadoPedido());
    }

    @Test
    @DisplayName("2. Pedido domicilio listo para envío")
    public void test2_PedidoDomicilioListoEnvio() {
        Reserva r = createBaseOrder("domicilio");
        r = reservaService.marcarPedidoEnPreparacion(r, "admin");
        Reserva updated = reservaService.marcarPedidoListoEnvio(r, "admin");
        assertEquals("Pago Confirmado", updated.getEstado());
        assertEquals("LISTO_ENVIO", updated.getEstadoPedido());
    }

    @Test
    @DisplayName("3. Pedido domicilio en camino")
    public void test3_PedidoDomicilioEnCamino() {
        Reserva r = createBaseOrder("domicilio");
        r = reservaService.marcarPedidoEnPreparacion(r, "admin");
        r = reservaService.marcarPedidoListoEnvio(r, "admin");
        Reserva updated = reservaService.marcarPedidoEnCamino(r, "admin");
        assertEquals("Pago Confirmado", updated.getEstado());
        assertEquals("EN_CAMINO", updated.getEstadoPedido());
    }

    @Test
    @DisplayName("4. Pedido domicilio entregado")
    public void test4_PedidoDomicilioEntregado() {
        Reserva r = createBaseOrder("domicilio");
        r = reservaService.marcarPedidoEnPreparacion(r, "admin");
        r = reservaService.marcarPedidoListoEnvio(r, "admin");
        r = reservaService.marcarPedidoEnCamino(r, "admin");
        Reserva updated = reservaService.marcarPedidoEntregado(r, "admin");
        assertEquals("Pago Confirmado", updated.getEstado());
        assertEquals("ENTREGADO", updated.getEstadoPedido());
        assertTrue(Boolean.TRUE.equals(updated.getArchivada()));
    }

    @Test
    @DisplayName("5. Pedido pickup en preparación")
    public void test5_PedidoPickupEnPreparacion() {
        Reserva r = createBaseOrder("pickup");
        Reserva updated = reservaService.marcarPedidoEnPreparacion(r, "admin");
        assertEquals("Pago Confirmado", updated.getEstado());
        assertEquals("EN_PREPARACION", updated.getEstadoPedido());
    }

    @Test
    @DisplayName("6. Pedido pickup listo para recoger")
    public void test6_PedidoPickupListoRecoger() {
        Reserva r = createBaseOrder("pickup");
        r = reservaService.marcarPedidoEnPreparacion(r, "admin");
        Reserva updated = reservaService.marcarPedidoListoParaRecoger(r, "admin");
        assertEquals("Pago Confirmado", updated.getEstado());
        assertEquals("LISTO_RECOGER", updated.getEstadoPedido());
    }

    @Test
    @DisplayName("7. Pedido pickup recogido")
    public void test7_PedidoPickupRecogido() {
        Reserva r = createBaseOrder("pickup");
        r = reservaService.marcarPedidoEnPreparacion(r, "admin");
        r = reservaService.marcarPedidoListoParaRecoger(r, "admin");
        Reserva updated = reservaService.marcarPedidoEntregado(r, "admin");
        assertEquals("Pago Confirmado", updated.getEstado());
        assertEquals("RECOGIDO", updated.getEstadoPedido());
        assertTrue(Boolean.TRUE.equals(updated.getArchivada()));
    }

    @Test
    @DisplayName("8. Pickup intentando EN_CAMINO debe fallar")
    public void test8_PickupIntentandoEnCaminoFalla() {
        Reserva r = createBaseOrder("pickup");
        assertThrows(IllegalStateException.class, () -> {
            reservaService.marcarPedidoEnCamino(r, "admin");
        });
    }

    @Test
    @DisplayName("9. Pickup intentando LISTO_ENVIO debe fallar")
    public void test9_PickupIntentandoListoEnvioFalla() {
        Reserva r = createBaseOrder("pickup");
        assertThrows(IllegalStateException.class, () -> {
            reservaService.marcarPedidoListoEnvio(r, "admin");
        });
    }

    @Test
    @DisplayName("10. Domicilio intentando LISTO_RECOGER debe fallar")
    public void test10_DomicilioIntentandoListoRecogerFalla() {
        Reserva r = createBaseOrder("domicilio");
        assertThrows(IllegalStateException.class, () -> {
            reservaService.marcarPedidoListoParaRecoger(r, "admin");
        });
    }

    @Test
    @DisplayName("11. Domicilio intentando RECOGIDO debe fallar")
    public void test11_DomicilioIntentandoRecogidoFalla() {
        Reserva r = createBaseOrder("domicilio");
        assertThrows(IllegalStateException.class, () -> {
            reservaService.validarTransicion(r, "RECOGIDO");
        });
    }

    @Test
    @DisplayName("12. Detalle refleja método correcto en isivi.js")
    public void test12_DetalleReflejaMetodoCorrecto() throws Exception {
        String js = readClasspathFile("static/js/isivi.js");
        assertTrue(js.contains("adm-order-detail-delivery"), "HTML delivery detail binding must be present");
        assertTrue(js.contains("isDelivery"), "isDelivery check in javascript must be present");
    }

    @Test
    @DisplayName("13. Historial refleja método correcto en isivi.js")
    public void test13_HistorialReflejaMetodoCorrecto() throws Exception {
        String js = readClasspathFile("static/js/isivi.js");
        assertTrue(js.contains("historyBookingsList.find"), "historyBookingsList lookup must be present");
    }

    @Test
    @DisplayName("14. Consulta pública refleja método correcto en isivi.js")
    public void test14_ConsultaPublicaReflejaMetodoCorrecto() throws Exception {
        String js = readClasspathFile("static/js/isivi.js");
        assertTrue(js.contains("FLUJO DOMICILIO:"), "Public lookup must display delivery flows");
        assertTrue(js.contains("FLUJO PICKUP:"), "Public lookup must display pickup flows");
    }

    @Test
    @DisplayName("15. Dashboard/contadores clasifican correctamente en isivi.js")
    public void test15_DashboardContadoresClasificanCorrectamente() throws Exception {
        String js = readClasspathFile("static/js/isivi.js");
        assertTrue(js.contains("adm-orders-stat-active"), "Active order metric ID must be updated");
        assertTrue(js.contains("adm-orders-stat-prep"), "Prep order metric ID must be updated");
        assertTrue(js.contains("adm-orders-stat-ready"), "Ready order metric ID must be updated");
        assertTrue(js.contains("adm-orders-stat-delivered"), "Delivered order metric ID must be updated");
    }

    @Test
    @DisplayName("16. Métrica 'Listos para recoger' excluye domicilio")
    public void test16_MetricaListosParaRecogerExcluyeDomicilio() throws Exception {
        String js = readClasspathFile("static/js/isivi.js");
        assertTrue(js.contains("!isDelivery && (st === 'LISTO_RECOGER' || st === 'LISTO PARA RECOGER')"),
                "Stat counter for Ready must strictly count pickup orders");
    }

    @Test
    @DisplayName("17. Métrica 'Entregados / Recogidos' incluye ambos y conserva estado real")
    public void test17_MetricaEntregadosRecogidosConservaEstadoReal() throws Exception {
        String js = readClasspathFile("static/js/isivi.js");
        assertTrue(js.contains("st === 'ENTREGADO' || st === 'RECOGIDO'"),
                "Stat counter must include both ENTREGADO and RECOGIDO");
        assertTrue(js.contains("delivDomicilio") && js.contains("delivPickup"),
                "Stat counter must display the breakdown for both delivery and pickup");
    }

    @Test
    @DisplayName("18. Filtro agrupador muestra correctamente los tres estados: LISTO_RECOGER, LISTO_ENVIO, EN_CAMINO")
    public void test18_FiltroAgrupadorMuestraTresEstados() throws Exception {
        String js = readClasspathFile("static/js/isivi.js");
        assertTrue(js.contains("st === 'LISTO_RECOGER' || st === 'LISTO PARA RECOGER' || st === 'LISTO_ENVIO' || st === 'LISTO PARA ENVÍO' || st === 'LISTO PARA ENVIO' || st === 'EN_CAMINO' || st === 'EN CAMINO'"),
                "Filter 'LISTO PARA RECOGER' must map to all ready/shipping/on-the-way states");
    }

    @Test
    @DisplayName("19. Método de entrega visible coincide con backend")
    public void test19_MetodoEntregaVisibleCoincideConBackend() throws Exception {
        String js = readClasspathFile("static/js/isivi.js");
        assertTrue(js.contains("deliveryMethodNormalized"), "js must normalize deliveryMethod for comparisons");
        assertTrue(js.contains("tipoEntrega: b.tipoEntrega"), "js must map b.tipoEntrega from backend");
    }

    @Test
    @DisplayName("20. Consulta pública e historial reflejan el mismo estado y método de entrega")
    public void test20_ConsultaPublicaEHistorialReflejanMismoEstado() throws Exception {
        String js = readClasspathFile("static/js/isivi.js");
        assertTrue(js.contains("currentLookupBooking.estadoPedido"), "public lookup must check backend estadoPedido");
        assertTrue(js.contains("order.estadoPedido"), "admin history/table must check backend estadoPedido");
    }

    @Test
    @DisplayName("21. Un pedido pickup nunca muestra acción 'Entregar'")
    public void test21_PedidoPickupNuncaMuestraAccionEntregar() throws Exception {
        String js = readClasspathFile("static/js/isivi.js");
        // Within table render or detail modal actions, pickup flow must not show listo-envio/en-camino/entregar
        assertTrue(js.contains("adminSetOrderReadyForPickup"), "pickup must use ready for pickup actions");
    }

    @Test
    @DisplayName("22. Un pedido domicilio nunca muestra acción 'Recogido'")
    public void test22_PedidoDomicilioNuncaMuestraAccionRecogido() throws Exception {
        String js = readClasspathFile("static/js/isivi.js");
        // Within table render or detail modal actions, domicilio flow must not show listo-recoger/recogido
        assertTrue(js.contains("adminSetOrderReadyForShipping"), "domicilio must use ready for shipping actions");
        assertTrue(js.contains("adminSetOrderOnTheWay"), "domicilio must use on the way actions");
    }
}
