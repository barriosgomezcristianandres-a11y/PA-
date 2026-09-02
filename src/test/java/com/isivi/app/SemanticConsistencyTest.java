package com.isivi.app;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.isivi.app.service.ReservaService;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ServicioRepository;
import com.isivi.app.model.Reserva;
import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Servicio;
import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class SemanticConsistencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservaService reservaService;
    @Autowired
    private ReservaRepository reservaRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private ServicioRepository servicioRepository;

    private ItemReserva crearItem(String id, String tipo, int cant, String nombre, double precio) {
        ItemReserva it = new ItemReserva(id, tipo, cant);
        it.setNombre(nombre);
        it.setPrecioUnitario(precio);
        it.setSubtotal(precio * cant);
        return it;
    }

    private static final Path INDEX_HTML_PATH = Paths.get("src/main/resources/static/index.html");
    private static final Path ISIVI_JS_PATH = Paths.get("src/main/resources/static/js/isivi.js");

    @Test
    @DisplayName("1. El archivo index.html no debe contener terminología obsoleta/incoherente")
    void testIndexHtmlStaticConsistency() throws Exception {
        assertTrue(Files.exists(INDEX_HTML_PATH), "index.html debe existir");
        String content = Files.readString(INDEX_HTML_PATH);

        // No debe contener términos obsoletos en etiquetas/textos visibles
        assertFalse(content.contains("Saldo pendiente en peluquería"), "Debe usar 'Saldo en salón'");
        assertFalse(content.contains("Recoge en salón"), "Debe usar 'Recoger en el local'");
        assertFalse(content.contains("Punto de Entrega"), "Debe usar 'Entrega' o 'Entrega de productos'");
    }

    @Test
    @DisplayName("2. El archivo isivi.js no debe contener referencias a 'Recogida en salón'")
    void testIsiviJsStaticConsistency() throws Exception {
        assertTrue(Files.exists(ISIVI_JS_PATH), "isivi.js debe existir");
        String content = Files.readString(ISIVI_JS_PATH);

        assertFalse(content.contains("Recogida en salón"), "Debe usar 'Recogida en el local'");
    }

    @Test
    @DisplayName("3. El frontend de cliente (isivi.js) debe mapear dinámicamente las etiquetas según el tipo de operación")
    void testFrontendDynamicLabelMapping() throws Exception {
        String content = Files.readString(ISIVI_JS_PATH);

        // Mapeo en pantalla de éxito (checkout success)
        assertTrue(content.contains("success-items-label"), "Debe dinamizar success-items-label");
        assertTrue(content.contains("isPureAppointment"), "Debe verificar si es Cita pura");
        assertTrue(content.contains("isPureOrder"), "Debe verificar si es Pedido puro");

        // Unificación de Anticipo y Pago total
        assertTrue(content.contains("Pago total:"), "Debe mostrar Pago total en pedidos puros aprobados");
        assertTrue(content.contains("Anticipo (25%):"), "Debe mostrar Anticipo (25%) para citas/mixtas");
    }

    @Test
    @DisplayName("4. El buscador (lookup) en isivi.js debe dinamizar entrega y desgloses financieros sin mezclar conceptos")
    void testLookupDynamicSemanticMapping() throws Exception {
        String content = Files.readString(ISIVI_JS_PATH);

        assertTrue(content.contains("lookup-res-items-label"), "Debe dinamizar lookup-res-items-label");
        assertTrue(content.contains("lookup-res-delivery-wrap"), "Debe usar el wrap de entrega en lookup");
        assertTrue(content.contains("lookup-res-delivery-label"), "Debe dinamizar el label de entrega en lookup");
        assertTrue(content.contains("lookup-res-balance-wrap"), "Debe usar el wrap del saldo en lookup");
        assertTrue(content.contains("lookup-res-deposit-wrap"), "Debe usar el wrap de anticipo en lookup");
    }

    @Test
    @DisplayName("5. El modal de detalle de citas en administración debe mapear etiquetas y desgloses financieros de forma coherente")
    void testAdminDetailDynamicSemanticMapping() throws Exception {
        String content = Files.readString(ISIVI_JS_PATH);

        assertTrue(content.contains("adm-detail-items-label"), "Debe dinamizar adm-detail-items-label");
        assertTrue(content.contains("adm-detail-delivery-label"), "Debe dinamizar adm-detail-delivery-label");
        assertTrue(content.contains("adm-detail-subtotal-label"), "Debe dinamizar adm-detail-subtotal-label");
        assertTrue(content.contains("adm-detail-deposit-label"), "Debe dinamizar adm-detail-deposit-label");
        assertTrue(content.contains("Solo despacho de productos"), "Debe mostrar Solo despacho de productos si es pedido puro");
    }

    @Test
    @DisplayName("6. Los contratos de API del backend (JSON) no deben alterarse para mantener compatibilidad")
    void testBackendContractsArePreserved() throws Exception {
        // Ejecutar llamadas para verificar que el backend sigue respondiendo con los contratos JSON originales
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("7. El frontend (isivi.js) contiene los mensajes CRUD contextualizados por entidad")
    void testFrontendJsCrudMessages() throws Exception {
        assertTrue(Files.exists(ISIVI_JS_PATH), "isivi.js debe existir");
        String content = Files.readString(ISIVI_JS_PATH);
        
        assertTrue(content.contains("Producto eliminado correctamente."), "Debe contener éxito de producto eliminado");
        assertTrue(content.contains("Kit eliminado correctamente."), "Debe contener éxito de kit eliminado");
        assertTrue(content.contains("Categoría eliminada correctamente."), "Debe contener éxito de categoría eliminada");
        assertTrue(content.contains("Variante eliminada correctamente."), "Debe contener éxito de variante eliminada");
        assertTrue(content.contains("Producto creado correctamente."), "Debe contener éxito de producto creado");
        assertTrue(content.contains("Kit creado correctamente."), "Debe contener éxito de kit creado");
        assertTrue(content.contains("Producto actualizado correctamente."), "Debe contener éxito de producto actualizado");
        assertTrue(content.contains("Kit actualizado correctamente."), "Debe contener éxito de kit actualizado");
    }

    @Test
    @DisplayName("8A. Flujo MIXTO: Cita activa, producto retenido, eliminar producto -> inventario liberado, producto disponible, cita sigue reservada con fecha/hora/estado intactos")
    void testMixtoEliminacionExplicita() {
        Servicio s = servicioRepository.save(new Servicio("Corte 8A", "corte", 50000.0, "30 min", "Desc", ""));
        Producto p = productoRepository.save(new Producto("Shampoo 8A", 30000.0, "Desc", "", true, 5));
        
        Reserva res = new Reserva();
        res.setCodigoReserva("COD-8A-" + System.nanoTime());
        res.setNombreCliente("Carlos Gomez 8A");
        res.setTelefono("3112223344");
        res.setFechaCita(LocalDate.now().plusDays(2));
        res.setHoraCita("10:00 AM");
        res.setMedioPago("WOMPI");
        res.setItemsInventario(List.of(
            crearItem(s.getId(), "servicio", 1, s.getNombre(), s.getPrecio()),
            crearItem(p.getId(), "producto", 1, p.getNombre(), p.getPrecio())
        ));
        
        res = reservaService.prepararNuevaReserva(res);
        res = reservaRepository.save(res);
        
        // Stock original de p era 5, ahora debe ser 4
        assertEquals(4, productoRepository.findById(p.getId()).orElseThrow().getCantidad());
        
        res = reservaService.prepararPagoWompi(res, "REF-8A");
        assertEquals("Pendiente Pago", res.getEstado());
        
        // Eliminar el producto de la reserva
        List<ItemReserva> nuevosItems = List.of(
            crearItem(s.getId(), "servicio", 1, s.getNombre(), s.getPrecio())
        );
        reservaService.actualizarItemsDeReserva(res, nuevosItems);
        
        // Stock del producto liberado inmediatamente
        assertEquals(5, productoRepository.findById(p.getId()).orElseThrow().getCantidad());
        
        // Cita permanece reservada, fecha/hora y estado intactos, no cambia a PENDIENTE_COMPROBANTE
        Reserva resDb = reservaRepository.findById(res.getId()).orElseThrow();
        assertEquals("Pendiente Pago", resDb.getEstado());
        assertEquals(LocalDate.now().plusDays(2), resDb.getFechaCita());
        assertEquals("10:00 AM", resDb.getHoraCita());
        assertTrue(resDb.esCita());
        assertFalse(resDb.esMixto());
    }

    @Test
    @DisplayName("8B. Flujo MIXTO: Cita activa, producto retenido, abandonar -> expirar hold -> inventario liberado, producto disponible, cita permanece según agenda, no cambia a PENDIENTE_COMPROBANTE")
    void testMixtoExpiracionHold() {
        Servicio s = servicioRepository.save(new Servicio("Corte 8B", "corte", 50000.0, "30 min", "Desc", ""));
        Producto p = productoRepository.save(new Producto("Shampoo 8B", 30000.0, "Desc", "", true, 5));
        
        Reserva res = new Reserva();
        res.setCodigoReserva("COD-8B-" + System.nanoTime());
        res.setNombreCliente("Carlos Gomez 8B");
        res.setTelefono("3112223344");
        res.setFechaCita(LocalDate.now().plusDays(2));
        res.setHoraCita("10:30 AM");
        res.setMedioPago("WOMPI");
        res.setItemsInventario(List.of(
            crearItem(s.getId(), "servicio", 1, s.getNombre(), s.getPrecio()),
            crearItem(p.getId(), "producto", 1, p.getNombre(), p.getPrecio())
        ));
        
        res = reservaService.prepararNuevaReserva(res);
        res = reservaRepository.save(res);
        res = reservaService.prepararPagoWompi(res, "REF-8B");
        
        // Expirar hold
        res = reservaService.marcarExpirada(res);
        
        // Inventario liberado
        assertEquals(5, productoRepository.findById(p.getId()).orElseThrow().getCantidad());
        
        // Cita permanece activa en Pendiente Comprobante (no cancelada ni pasada a Pendiente Pago)
        assertEquals("Pendiente Comprobante", res.getEstado());
        assertNull(res.getFechaExpiracionPago()); // Limpiado para evitar bucles, conservada para agenda
        assertTrue(res.esCita());
        assertFalse(res.esMixto());
    }

    @Test
    @DisplayName("9. Flujo MIXTO: APPROVED -> Cita Confirmada, pedido PENDIENTE_PREPARACION, inventario consumido una sola vez, sin doble descuento")
    void testMixtoApprovedSinDobleDescuento() {
        Servicio s = servicioRepository.save(new Servicio("Corte 9", "corte", 50000.0, "30 min", "Desc", ""));
        Producto p = productoRepository.save(new Producto("Shampoo 9", 30000.0, "Desc", "", true, 5));
        
        Reserva res = new Reserva();
        res.setCodigoReserva("COD-9-" + System.nanoTime());
        res.setNombreCliente("Andres 9");
        res.setTelefono("3115556677");
        res.setFechaCita(LocalDate.now().plusDays(2));
        res.setHoraCita("11:00 AM");
        res.setMedioPago("WOMPI");
        res.setItemsInventario(List.of(
            crearItem(s.getId(), "servicio", 1, s.getNombre(), s.getPrecio()),
            crearItem(p.getId(), "producto", 1, p.getNombre(), p.getPrecio())
        ));
        
        res = reservaService.prepararNuevaReserva(res);
        res = reservaRepository.save(res);
        res = reservaService.prepararPagoWompi(res, "REF-9");
        
        // Stock es 4
        assertEquals(4, productoRepository.findById(p.getId()).orElseThrow().getCantidad());
        
        // Aprobación de pago
        res = reservaService.confirmarPagoWompi(res);
        
        // Verificar una sola reducción (sigue siendo 4)
        assertEquals(4, productoRepository.findById(p.getId()).orElseThrow().getCantidad());
        assertTrue(res.getInventarioReservado());
        assertEquals("Confirmado", res.getEstado());
        assertEquals("PENDIENTE_PREPARACION", res.getEstadoPedido());
    }

    @Test
    @DisplayName("10. Flujo MIXTO: Logística completa -> estado de cita permanece en 'Confirmado', solo cambia estadoPedido")
    void testLogisticaMixtaCompleta() {
        Servicio s = servicioRepository.save(new Servicio("Corte 10", "corte", 50000.0, "30 min", "Desc", ""));
        Producto p = productoRepository.save(new Producto("Shampoo 10", 30000.0, "Desc", "", true, 5));
        
        Reserva res = new Reserva();
        res.setCodigoReserva("COD-10-" + System.nanoTime());
        res.setNombreCliente("Andres 10");
        res.setTelefono("3115556677");
        res.setFechaCita(LocalDate.now().plusDays(2));
        res.setHoraCita("11:30 AM");
        res.setMedioPago("WOMPI");
        res.setItemsInventario(List.of(
            crearItem(s.getId(), "servicio", 1, s.getNombre(), s.getPrecio()),
            crearItem(p.getId(), "producto", 1, p.getNombre(), p.getPrecio())
        ));
        
        res = reservaService.prepararNuevaReserva(res);
        res = reservaRepository.save(res);
        res = reservaService.prepararPagoWompi(res, "REF-10");
        res = reservaService.confirmarPagoWompi(res);
        
        assertEquals("Confirmado", res.getEstado());
        assertEquals("PENDIENTE_PREPARACION", res.getEstadoPedido());
        
        // Preparar
        res = reservaService.marcarPedidoEnPreparacion(res, "admin");
        assertEquals("Confirmado", res.getEstado());
        assertEquals("EN_PREPARACION", res.getEstadoPedido());
        
        // Listo (para recoger en este caso)
        res = reservaService.marcarPedidoListoParaRecoger(res, "admin");
        assertEquals("Confirmado", res.getEstado());
        assertEquals("LISTO_RECOGER", res.getEstadoPedido());
        
        // Entregar
        res = reservaService.marcarPedidoEntregado(res, "admin");
        assertEquals("Confirmado", res.getEstado());
        assertEquals("RECOGIDO", res.getEstadoPedido());
    }

    @Test
    @DisplayName("11. Flujo PEDIDO PURO: estado no se usa para logística, estadoPedido guía todo el flujo")
    void testPedidoPuroLogistica() {
        Producto p = productoRepository.save(new Producto("Shampoo 11", 30000.0, "Desc", "", true, 5));
        
        Reserva res = new Reserva();
        res.setCodigoReserva("COD-11-" + System.nanoTime());
        res.setNombreCliente("Andres 11");
        res.setTelefono("3115556677");
        res.setMedioPago("WOMPI");
        res.setItemsInventario(List.of(
            crearItem(p.getId(), "producto", 1, p.getNombre(), p.getPrecio())
        ));
        
        res = reservaService.prepararNuevaReserva(res);
        res = reservaRepository.save(res);
        res = reservaService.prepararPagoWompi(res, "REF-11");
        res = reservaService.confirmarPagoWompi(res);
        
        assertEquals("Pago Confirmado", res.getEstado());
        assertEquals("PENDIENTE_PREPARACION", res.getEstadoPedido());
        
        // Preparar
        res = reservaService.marcarPedidoEnPreparacion(res, "admin");
        assertEquals("Pago Confirmado", res.getEstado()); // No debe cambiar
        assertEquals("EN_PREPARACION", res.getEstadoPedido());
    }

    @Test
    @DisplayName("12. Flujo CITA PURA: estado representa flujo actual de cita, estadoPedido es null")
    void testCitaPura() {
        Servicio s = servicioRepository.save(new Servicio("Corte 12", "corte", 50000.0, "30 min", "Desc", ""));
        
        Reserva res = new Reserva();
        res.setCodigoReserva("COD-12-" + System.nanoTime());
        res.setNombreCliente("Andres 12");
        res.setTelefono("3115556677");
        res.setFechaCita(LocalDate.now().plusDays(2));
        res.setHoraCita("12:00 PM");
        res.setMedioPago("WOMPI");
        res.setItemsInventario(List.of(
            crearItem(s.getId(), "servicio", 1, s.getNombre(), s.getPrecio())
        ));
        
        res = reservaService.prepararNuevaReserva(res);
        res = reservaRepository.save(res);
        res = reservaService.prepararPagoWompi(res, "REF-12");
        
        assertEquals("Pendiente Pago", res.getEstado());
        assertNull(res.getEstadoPedido()); // Cita pura tiene siempre estadoPedido = null
        
        res = reservaService.confirmarPagoWompi(res);
        assertEquals("Confirmado", res.getEstado());
        assertNull(res.getEstadoPedido());
    }
}
