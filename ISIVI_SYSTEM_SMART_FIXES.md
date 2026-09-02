# ISIVI - REGISTRO DE CORRECCIONES DEL SISTEMA (SYSTEM-WIDE SMART FIXES)

**Fecha**: 22 de Agosto, 2026

---

## 1. RESUMEN DE CAMBIOS APLICADOS

### CORRECCIÓN #1: Alineación Semántica de 7 Columnas en la Tabla del Catálogo Administrativo (`isivi.js`)

- **Archivo**: [`src/main/resources/static/js/isivi.js`](file:///c:/Users/barri/OneDrive/Escritorio/antes%20de%20wompi/terminado1/isivi-app/src/main/resources/static/js/isivi.js#L6541-L6650)
- **Causa Raíz**: Descalce de 6 `<td>` contra 7 `<th>` en la tabla de productos y kits.
- **Solución**: Creación de la celda `<td>` #3 para la categoría real (`categoryLabel`), manteniendo en la celda `<td>` #2 el nombre e insignia de tipo.
- **Prueba**: `PriceParsingAndFormattingTest.testAdminCatalogTableColumnsAlignment()`.

### CORRECCIÓN #2: Precisión Semántica de Anticipo (25%) en la Sección Héroe (`index.html`)

- **Archivo**: [`src/main/resources/static/index.html`](file:///c:/Users/barri/OneDrive/Escritorio/antes%20de%20wompi/terminado1/isivi-app/src/main/resources/static/index.html#L405)
- **Causa Raíz**: Redacción imprecisa que atribuía el 25% de anticipo a la compra de productos.
- **Solución**: Se corrigió el texto para delimitar que el 25% de anticipo aplica exclusivamente a los servicios de peluquería.
- **Prueba**: `UxEnhancementsValidationTest.testCatalogIntegrityAndNullSafety()`.

---

## 2. PRUEBAS AUTOMATIZADAS ASOCIADAS

Se ejecutó la suite completa de pruebas obteniendo `BUILD SUCCESS` (317 pruebas de integración pasadas).

---

*Documento oficial de correcciones ISIVI.*
