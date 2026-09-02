package com.isivi.app;

import com.isivi.app.controller.CategoriaProductoController;
import com.isivi.app.controller.ProductoController;
import com.isivi.app.model.CategoriaProducto;
import com.isivi.app.model.ItemReserva;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Reserva;
import com.isivi.app.model.VarianteProducto;
import com.isivi.app.repository.CategoriaProductoRepository;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProductCategoryAndVariantTest {

    @Autowired
    private CategoriaProductoController categoriaController;

    @Autowired
    private CategoriaProductoRepository categoriaRepository;

    @Autowired
    private ProductoController productoController;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private com.isivi.app.controller.KitController kitController;

    @Autowired
    private com.isivi.app.repository.KitRepository kitRepository;

    private static final AtomicInteger counter = new AtomicInteger((int) (System.currentTimeMillis() % 50000));

    @BeforeEach
    public void setup() {}

    @Test
    @DisplayName("1. CRUD Categoría de Producto: Crear, Listar, Editar, Activar/Desactivar")
    public void testCrudCategoriaProducto() {
        String uniqueName = "Champús Especiales " + UUID.randomUUID().toString().substring(0, 8);
        CategoriaProducto cat = new CategoriaProducto(null, uniqueName, true, "PRODUCTO");

        // Crear
        ResponseEntity<?> respCreate = categoriaController.crear(cat);
        assertEquals(HttpStatus.CREATED, respCreate.getStatusCode());
        CategoriaProducto creada = (CategoriaProducto) respCreate.getBody();
        assertNotNull(creada.getId());
        assertEquals(uniqueName, creada.getNombre());
        assertTrue(creada.getActivo());

        // Duplicado debe fallar con 409
        ResponseEntity<?> respDup = categoriaController.crear(new CategoriaProducto(null, uniqueName, true, "PRODUCTO"));
        assertEquals(HttpStatus.CONFLICT, respDup.getStatusCode());

        // Editar
        String modName = uniqueName + " Editado";
        creada.setNombre(modName);
        ResponseEntity<?> respEdit = categoriaController.actualizar(creada.getId(), creada);
        assertEquals(HttpStatus.OK, respEdit.getStatusCode());
        CategoriaProducto editada = (CategoriaProducto) respEdit.getBody();
        assertEquals(modName, editada.getNombre());

        // Toggle Activo
        ResponseEntity<?> respToggle = categoriaController.alternarActivo(creada.getId());
        assertEquals(HttpStatus.OK, respToggle.getStatusCode());
        CategoriaProducto toggled = (CategoriaProducto) respToggle.getBody();
        assertFalse(toggled.getActivo());
    }

    @Test
    @DisplayName("2. Eliminación segura de Categoría: Bloquea con 409 si tiene productos asignados")
    public void testEliminarCategoriaConProductosRetorna409() {
        String catName = "Tratamientos Capilares " + UUID.randomUUID().toString().substring(0, 8);
        CategoriaProducto cat = new CategoriaProducto(null, catName, true, "PRODUCTO");
        CategoriaProducto creada = (CategoriaProducto) categoriaController.crear(cat).getBody();

        // Crear producto asignado a la categoría
        Producto p = new Producto("Tratamiento Intensivo " + UUID.randomUUID().toString().substring(0, 8), 45000.0, "Desc", "img.jpg", true, 5);
        p.setCategoriaId(creada.getId());
        Producto prodGuardado = (Producto) productoController.crear(p).getBody();

        // Intento de eliminación debe retornar 409 Conflict
        ResponseEntity<?> respDelete = categoriaController.eliminar(creada.getId());
        assertEquals(HttpStatus.CONFLICT, respDelete.getStatusCode());

        // Reasignar/eliminar producto y reintentar eliminación
        productoController.eliminar(prodGuardado.getId());
        ResponseEntity<?> respDeleteOk = categoriaController.eliminar(creada.getId());
        assertEquals(HttpStatus.NO_CONTENT, respDeleteOk.getStatusCode());
    }

    @Test
    @DisplayName("3. Producto de Precio Único: backend valida precio y existencias")
    public void testProductoPrecioUnico() {
        String prodName = "Shampoo Anticaspa " + UUID.randomUUID().toString().substring(0, 8);
        Producto p = new Producto(prodName, 32000.0, "Desc", "img.jpg", true, 10);
        p.setTipoPrecio("UNICO");
        Producto guardado = (Producto) productoController.crear(p).getBody();

        assertEquals("UNICO", guardado.getTipoPrecio());
        assertEquals(32000.0, guardado.getPrecio());

        // Crear pedido con precio manipulado por frontend
        Reserva req = new Reserva();
        req.setCodigoReserva("ISV-P1-" + UUID.randomUUID().toString().substring(0, 8));
        req.setNombreCliente("Cliente Compra");
        req.setTelefono("3001112233");

        ItemReserva item = new ItemReserva();
        item.setId(guardado.getId());
        item.setTipo("producto");
        item.setCantidad(2);
        item.setPrecioUnitario(100.0); // Precio manipulado intentando pagar 100 COP

        req.setItemsInventario(List.of(item));
        Reserva procesada = reservaService.prepararNuevaReserva(req);

        // El backend debe usar el precio real (32.000 * 2 = 64.000)
        assertEquals(64000.0, procesada.getSubtotal());
        assertEquals(32000.0, procesada.getItemsInventario().get(0).getPrecioUnitario());

        // Stock debe haber disminuido en 2
        Producto actual = productoRepository.findById(guardado.getId()).orElseThrow();
        assertEquals(8, actual.getCantidad());
    }

    @Test
    @DisplayName("4. Producto con Variantes/Modelos: backend valida precio independiente y variante seleccionada")
    public void testProductoConVariantes() {
        String prodName = "Aceite de Argán " + UUID.randomUUID().toString().substring(0, 8);
        Producto p = new Producto();
        p.setNombre(prodName);
        p.setTipoPrecio("VARIANTES");
        p.setImagenUrl("argan.jpg");
        p.setDescripcion("Puro");

        VarianteProducto v1 = new VarianteProducto("var-60ml", "60 ml", 25000.0, 5, true, true);
        VarianteProducto v2 = new VarianteProducto("var-120ml", "120 ml", 42000.0, 4, true, true);
        VarianteProducto v3 = new VarianteProducto("var-250ml", "250 ml (Agotado)", 65000.0, 0, false, true);

        p.setVariantes(List.of(v1, v2, v3));
        Producto guardado = (Producto) productoController.crear(p).getBody();

        assertEquals("VARIANTES", guardado.getTipoPrecio());
        assertEquals(25000.0, guardado.getPrecio()); // Mínimo precio
        assertEquals(9, guardado.getCantidad()); // Total stock (5 + 4)

        // Compra de variante 120ml
        Reserva req = new Reserva();
        req.setCodigoReserva("ISV-PV-" + UUID.randomUUID().toString().substring(0, 8));
        req.setNombreCliente("Cliente Variante");
        req.setTelefono("3004445566");

        ItemReserva item = new ItemReserva();
        item.setId(guardado.getId());
        item.setTipo("producto");
        item.setVarianteId("var-120ml");
        item.setCantidad(2);
        item.setPrecioUnitario(500.0); // Manipulado

        req.setItemsInventario(List.of(item));
        Reserva procesada = reservaService.prepararNuevaReserva(req);

        // Backend debe aplicar precio de 42.000 * 2 = 84.000
        assertEquals(84000.0, procesada.getSubtotal());
        assertEquals(42000.0, procesada.getItemsInventario().get(0).getPrecioUnitario());
        assertEquals("var-120ml", procesada.getItemsInventario().get(0).getVarianteId());
        assertEquals("120 ml", procesada.getItemsInventario().get(0).getVarianteNombre());
        assertTrue(procesada.getItemsInventario().get(0).getNombre().contains("120 ml"));

        // Verificar stock de la variante
        Producto despues = productoRepository.findById(guardado.getId()).orElseThrow();
        VarianteProducto v2Actual = despues.getVariantes().stream().filter(v -> "var-120ml".equals(v.getId())).findFirst().orElseThrow();
        assertEquals(2, v2Actual.getCantidad()); // 4 - 2 = 2
    }

    @Test
    @DisplayName("5. Validación estricta: Error al intentar comprar variante inexistente, inactiva o sin stock")
    public void testValidacionVariantesFallidas() {
        Producto p = new Producto();
        p.setNombre("Tinte Profesional " + UUID.randomUUID().toString().substring(0, 8));
        p.setTipoPrecio("VARIANTES");
        VarianteProducto v1 = new VarianteProducto("var-negro", "Negro Natural", 20000.0, 3, true, true);
        VarianteProducto v2 = new VarianteProducto("var-rojo", "Rojo Pasión (Inactivo)", 22000.0, 3, true, false);
        VarianteProducto v3 = new VarianteProducto("var-rubio", "Rubio Cenizo (Agotado)", 24000.0, 0, false, true);
        p.setVariantes(List.of(v1, v2, v3));
        Producto guardado = (Producto) productoController.crear(p).getBody();

        // 1. Sin variante
        Reserva reqSinVar = new Reserva();
        reqSinVar.setItemsInventario(List.of(new ItemReserva(guardado.getId(), "producto", 1)));
        assertThrows(IllegalArgumentException.class, () -> reservaService.prepararNuevaReserva(reqSinVar));

        // 2. Variante inexistente
        ItemReserva itemInexistente = new ItemReserva(guardado.getId(), "producto", 1);
        itemInexistente.setVarianteId("var-fantasma");
        Reserva reqInexistente = new Reserva();
        reqInexistente.setItemsInventario(List.of(itemInexistente));
        assertThrows(IllegalArgumentException.class, () -> reservaService.prepararNuevaReserva(reqInexistente));

        // 3. Variante inactiva
        ItemReserva itemInactiva = new ItemReserva(guardado.getId(), "producto", 1);
        itemInactiva.setVarianteId("var-rojo");
        Reserva reqInactiva = new Reserva();
        reqInactiva.setItemsInventario(List.of(itemInactiva));
        assertThrows(IllegalArgumentException.class, () -> reservaService.prepararNuevaReserva(reqInactiva));

        // 4. Variante sin stock
        ItemReserva itemSinStock = new ItemReserva(guardado.getId(), "producto", 1);
        itemSinStock.setVarianteId("var-rubio");
        Reserva reqSinStock = new Reserva();
        reqSinStock.setItemsInventario(List.of(itemSinStock));
        assertThrows(IllegalStateException.class, () -> reservaService.prepararNuevaReserva(reqSinStock));
    }

    @Test
    @DisplayName("6. Carrito con múltiples variantes del mismo producto como líneas independientes")
    public void testMultiplesVariantesMismoProductoEnPedido() {
        Producto p = new Producto();
        p.setNombre("Serum Capilar " + UUID.randomUUID().toString().substring(0, 8));
        p.setTipoPrecio("VARIANTES");
        VarianteProducto vA = new VarianteProducto("var-a", "Modelo A", 15000.0, 10, true, true);
        VarianteProducto vB = new VarianteProducto("var-b", "Modelo B", 20000.0, 10, true, true);
        p.setVariantes(List.of(vA, vB));
        Producto guardado = (Producto) productoController.crear(p).getBody();

        ItemReserva itA = new ItemReserva(guardado.getId(), "producto", 2);
        itA.setVarianteId("var-a");

        ItemReserva itB = new ItemReserva(guardado.getId(), "producto", 1);
        itB.setVarianteId("var-b");

        Reserva req = new Reserva();
        req.setCodigoReserva("ISV-MULTIVAR-" + UUID.randomUUID().toString().substring(0, 8));
        req.setItemsInventario(List.of(itA, itB));
        Reserva procesada = reservaService.prepararNuevaReserva(req);

        // 2 líneas independientes
        assertEquals(2, procesada.getItemsInventario().size());
        assertEquals(50000.0, procesada.getSubtotal()); // (15.000 * 2) + (20.000 * 1) = 50.000
    }

    @Test
    @DisplayName("7. Kit con Categoría Personalizada: asignar, actualizar y validar protección de eliminación de categoría")
    public void testKitConCategoriaPersonalizada() {
        String catName = "Kits Anticaída " + UUID.randomUUID().toString().substring(0, 8);
        CategoriaProducto cat = new CategoriaProducto(null, catName, true, "KIT");
        CategoriaProducto creada = (CategoriaProducto) categoriaController.crear(cat).getBody();
        assertNotNull(creada);
        assertNotNull(creada.getId());

        com.isivi.app.model.Kit kit = new com.isivi.app.model.Kit("Kit Crecimiento Intensivo " + UUID.randomUUID().toString().substring(0, 8), 120000.0, "Kit completo", "kit.jpg", true, 10);
        kit.setCategoriaId(creada.getId());

        ResponseEntity<com.isivi.app.model.Kit> respKit = kitController.crear(kit);
        assertEquals(HttpStatus.CREATED, respKit.getStatusCode());
        com.isivi.app.model.Kit kitCreado = respKit.getBody();
        assertNotNull(kitCreado);
        assertEquals(creada.getId(), kitCreado.getCategoriaId());

        // Intentar eliminar la categoría asociada a este kit debe retornar 409 CONFLICT
        ResponseEntity<?> respDeleteFail = categoriaController.eliminar(creada.getId());
        assertEquals(HttpStatus.CONFLICT, respDeleteFail.getStatusCode());

        // Actualizar kit con otra categoría o sin categoría
        kitCreado.setCategoriaId(null);
        ResponseEntity<com.isivi.app.model.Kit> respKitUpd = kitController.actualizar(kitCreado.getId(), kitCreado);
        assertEquals(HttpStatus.OK, respKitUpd.getStatusCode());
        assertNull(respKitUpd.getBody().getCategoriaId());

        // Ahora eliminar la categoría debe retornar 204 NO_CONTENT
        ResponseEntity<?> respDeleteOk = categoriaController.eliminar(creada.getId());
        assertEquals(HttpStatus.NO_CONTENT, respDeleteOk.getStatusCode());

        // Limpieza
        kitController.eliminar(kitCreado.getId());
    }
}
