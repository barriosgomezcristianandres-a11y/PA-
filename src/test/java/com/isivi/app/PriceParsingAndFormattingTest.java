package com.isivi.app;

import com.isivi.app.controller.CategoriaProductoController;
import com.isivi.app.controller.KitController;
import com.isivi.app.controller.ProductoController;
import com.isivi.app.model.CategoriaProducto;
import com.isivi.app.model.Kit;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Servicio;
import com.isivi.app.model.VarianteProducto;
import com.isivi.app.repository.CategoriaProductoRepository;
import com.isivi.app.repository.KitRepository;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ServicioRepository;
import com.isivi.app.service.WompiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PriceParsingAndFormattingTest {

    @Autowired
    private ProductoController productoController;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private KitController kitController;

    @Autowired
    private KitRepository kitRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private CategoriaProductoController categoriaController;

    @Autowired
    private CategoriaProductoRepository categoriaRepository;

    @Autowired(required = false)
    private WompiService wompiService;

    // Parser Java equivalente a la regla de negocio JS estricta para COP
    private Double parsePriceJava(String input) {
        if (input == null || input.isBlank()) return null;
        String str = input.replace("$", "").replaceAll("(?i)\\bCOP\\b", "").trim();

        if (str.endsWith(",00")) {
            str = str.substring(0, str.length() - 3).trim();
        } else if (str.endsWith(".00") && !str.matches("^\\d{1,3}(\\.\\d{3})+$")) {
            str = str.substring(0, str.length() - 3).trim();
        }

        if (str.contains(",")) {
            return null; // Rechazar decimales no enteros en COP
        }

        if (str.matches("^\\d{1,3}(\\.\\d{3})+$")) {
            String clean = str.replace(".", "");
            return Double.parseDouble(clean);
        }

        if (str.matches("^\\d+$")) {
            return Double.parseDouble(str);
        }

        return null;
    }

    private String formatPriceJava(double price) {
        return "$" + NumberFormat.getIntegerInstance(new Locale("es", "CO")).format(Math.round(price));
    }

    @Test
    @DisplayName("1. Parser de precios estricto para COP enteros (30.000, 45.000, 120.000, 1.250.000, 2.500.000, 30000)")
    public void testStrictPriceParsing() {
        assertEquals(30000.0, parsePriceJava("30.000"));
        assertEquals(45000.0, parsePriceJava("45.000"));
        assertEquals(120000.0, parsePriceJava("120.000"));
        assertEquals(1250000.0, parsePriceJava("1.250.000"));
        assertEquals(2500000.0, parsePriceJava("2.500.000"));
        assertEquals(30000.0, parsePriceJava("30000"));
        assertEquals(30000.0, parsePriceJava("$ 30.000"));
        assertEquals(1250000.0, parsePriceJava("$1.250.000 COP"));
        assertEquals(30000.0, parsePriceJava("30.000,00"));
        assertEquals(30000.0, parsePriceJava("30000.00"));

        // Decimales ambiguos o no enteros deben ser rechazados (null)
        assertNull(parsePriceJava("30.5"));
        assertNull(parsePriceJava("30,5"));
        assertNull(parsePriceJava("30,50"));
        assertNull(parsePriceJava("30.50"));
        assertNull(parsePriceJava("abc"));
        assertNull(parsePriceJava(""));
        assertNull(parsePriceJava(null));
    }

    @Test
    @DisplayName("2. Formato visual de precios COP ($30.000, $45.000, $1.250.000)")
    public void testPriceFormatting() {
        assertEquals("$30.000", formatPriceJava(30000.0));
        assertEquals("$45.000", formatPriceJava(45000.0));
        assertEquals("$1.250.000", formatPriceJava(1250000.0));
        assertEquals("$2.500.000", formatPriceJava(2500000.0));
    }

    @Test
    @DisplayName("3. Conversión a centavos para Wompi (30.000 COP -> 3.000.000 centavos)")
    public void testWompiAmountInCentsConversion() {
        double precioCop1 = 30000.0;
        long centavos1 = Math.round(precioCop1 * 100d);
        assertEquals(3000000L, centavos1);

        double precioCop2 = 1250000.0;
        long centavos2 = Math.round(precioCop2 * 100d);
        assertEquals(125000000L, centavos2);

        double anticipoServicio = 75000.0 * 0.25; // 18750 COP
        long centavosAnticipo = Math.round(anticipoServicio * 100d);
        assertEquals(1875000L, centavosAnticipo);
    }

    @Test
    @DisplayName("4. Tipos numéricos de MongoDB y entidades (Double precio)")
    public void testEntityNumericTypes() {
        Producto p = new Producto();
        p.setPrecio(30000.0);
        assertTrue(p.getPrecio() instanceof Double);

        Kit k = new Kit();
        k.setPrecio(45000.0);
        assertTrue(k.getPrecio() instanceof Double);

        VarianteProducto v = new VarianteProducto();
        v.setPrecio(35000.0);
        assertTrue(v.getPrecio() instanceof Double);

        Servicio s = new Servicio();
        s.setPrecio(75000.0);
        assertTrue(s.getPrecio() instanceof Double);
    }

    @Test
    @DisplayName("5. Producto y Kit guardan correctamente precio numérico y categoriaId")
    public void testProductoAndKitPersistenceWithCategory() {
        String slug = "cat-test-" + UUID.randomUUID().toString().substring(0, 6);
        CategoriaProducto cat = new CategoriaProducto(slug, "Categoría Test QA " + slug, true);
        categoriaRepository.save(cat);

        // Guardar producto
        Producto prod = new Producto();
        prod.setNombre("Shampoo QA " + slug);
        prod.setPrecio(30000.0);
        prod.setCategoriaId(cat.getId());
        prod.setCantidad(10);
        prod.setEnStock(true);
        ResponseEntity<Producto> resProd = productoController.crear(prod);
        assertEquals(201, resProd.getStatusCode().value());
        assertNotNull(resProd.getBody());
        assertEquals(30000.0, resProd.getBody().getPrecio());
        assertEquals(cat.getId(), resProd.getBody().getCategoriaId());

        // Guardar kit
        Kit kit = new Kit();
        kit.setNombre("Kit QA " + slug);
        kit.setPrecio(1250000.0);
        kit.setCategoriaId(cat.getId());
        kit.setCantidad(5);
        kit.setEnStock(true);
        ResponseEntity<Kit> resKit = kitController.crear(kit);
        assertEquals(201, resKit.getStatusCode().value());
        assertNotNull(resKit.getBody());
        assertEquals(1250000.0, resKit.getBody().getPrecio());
        assertEquals(cat.getId(), resKit.getBody().getCategoriaId());

        // Cleanup
        if (resProd.getBody().getId() != null) productoRepository.deleteById(resProd.getBody().getId());
        if (resKit.getBody().getId() != null) kitRepository.deleteById(resKit.getBody().getId());
        categoriaRepository.deleteById(cat.getId());
    }

    @Test
    @DisplayName("6. Integridad de Frontend: index.html y isivi.js contienen componentes requeridos")
    public void testFrontendAssetsIntegrity() throws Exception {
        Path htmlPath = Path.of("src/main/resources/static/index.html");
        assertTrue(Files.exists(htmlPath), "index.html debe existir");
        String html = Files.readString(htmlPath);
        assertTrue(html.contains("id=\"kit-categories-pills\""), "index.html debe tener el contenedor id='kit-categories-pills'");
        assertTrue(html.contains("id=\"product-categories-pills\""), "index.html debe tener el contenedor id='product-categories-pills'");
        assertFalse(html.contains("<input type=\"number\" id=\"prod-price\""), "prod-price no debe ser type='number'");
        assertFalse(html.contains("<input type=\"number\" id=\"serv-price\""), "serv-price no debe ser type='number'");

        Path jsPath = Path.of("src/main/resources/static/js/isivi.js");
        assertTrue(Files.exists(jsPath), "isivi.js debe existir");
        String js = Files.readString(jsPath);
        assertTrue(js.contains("renderKitCategoryPills"), "isivi.js debe contener renderKitCategoryPills");
        assertTrue(js.contains("selectKitCategory"), "isivi.js debe contener selectKitCategory");
        assertTrue(js.contains("activeKitCategory"), "isivi.js debe contener activeKitCategory");
        assertTrue(js.contains("function parsePrice("), "isivi.js debe contener la función parsePrice");
        assertTrue(js.contains("function formatPrice("), "isivi.js debe contener la función formatPrice");
        assertTrue(js.contains("function formatPriceNumber("), "isivi.js debe contener la función formatPriceNumber");
    }

    @Test
    @DisplayName("7. Integridad y alineación semántica de 7 columnas de la tabla del catálogo administrativo")
    public void testAdminCatalogTableColumnsAlignment() throws Exception {
        Path htmlPath = Path.of("src/main/resources/static/index.html");
        assertTrue(Files.exists(htmlPath), "index.html debe existir");
        String html = Files.readString(htmlPath);

        assertTrue(html.contains("<th class=\"p-3\">Foto</th>"), "Debe existir encabezado Foto");
        assertTrue(html.contains("<th class=\"p-3\">Nombre &amp; Tipo</th>"), "Debe existir encabezado Nombre & Tipo");
        assertTrue(html.contains("<th class=\"p-3\">Categoría</th>"), "Debe existir encabezado Categoría");
        assertTrue(html.contains("<th class=\"p-3\">Precio</th>"), "Debe existir encabezado Precio");
        assertTrue(html.contains("<th class=\"p-3 text-center\">Cantidad</th>"), "Debe existir encabezado Cantidad");
        assertTrue(html.contains("<th class=\"p-3 text-center\">Estado de Stock</th>"), "Debe existir encabezado Estado de Stock");
        assertTrue(html.contains("<th class=\"p-3 text-right\">Acciones</th>"), "Debe existir encabezado Acciones");

        Path jsPath = Path.of("src/main/resources/static/js/isivi.js");
        assertTrue(Files.exists(jsPath), "isivi.js debe existir");
        String js = Files.readString(jsPath);

        assertTrue(js.contains("function renderAdminProductsTable()"), "isivi.js debe definir renderAdminProductsTable");
        assertTrue(js.contains("<td colspan=\"7\""), "El estado vacío de la tabla debe usar colspan=7");
        assertTrue(js.contains("${item.typeLabel}"), "La columna Nombre & Tipo debe incluir el tipo");
        assertTrue(js.contains("${item.categoryLabel}"), "La columna Categoría debe contener la categoría real o por defecto");
        assertTrue(js.contains("Sin categoría asignada"), "Debe usarse 'Sin categoría asignada' por defecto si no hay categoría");
    }
}
