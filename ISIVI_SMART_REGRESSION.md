# ISIVI - MATRIZ Y RESULTADOS DE REGRESIÓN (SMART REGRESSION)

**Fecha**: 22 de Agosto, 2026  
**Comando Ejecutado**: `mvn test`  
**Resultado**: `BUILD SUCCESS` (0 errores, 0 fallos)

---

## 1. SUITE DE PRUEBAS AUTOMATIZADAS EJECUTADAS

| Clase de Prueba | Pruebas | Resultado | Descripción |
|---|---|---|---|
| `PriceParsingAndFormattingTest` | 7 | ✅ PASÓ | Validación de formateo de precios y alineación de 7 columnas en la tabla administrativa (`testAdminCatalogTableColumnsAlignment`). |
| `ResumePaymentRegressionTest` | 5 | ✅ PASÓ | Verificación estática de `isivi.js`, sintaxis, balance de llaves y preservación de carrito ante cancelación de Wompi. |
| `UxEnhancementsValidationTest` | 7 | ✅ PASÓ | Validación de accesibilidad en barra contextual móvil, integridades de catálogo y visualizaciones. |
| `PureOrderVsAppointmentCheckoutTest` | 5 | ✅ PASÓ | Verificación de diferenciación estricta entre Pedido Puro (Productos/Kits) y Cita de Peluquería. |
| `MixedCartValidationTest` | 6 | ✅ PASÓ | Validación de reglas de carrito mixto, cálculo de subtotal y depuración de items. |
| `ProductCategoryAndVariantTest` | 8 | ✅ PASÓ | Validación de creación, asignación de categorías y soporte de variantes en productos y kits. |
| `OrderWorkflowTest` | 6 | ✅ PASÓ | Flujo completo de pedidos, actualización de estado logístico y stock de productos. |
| `DashboardControllerTest` | 7 | ✅ PASÓ | Mapeo de estadísticas del dashboard administrativo, ingresos reales y métricas. |
| `BrevoApiIntegrationTest` | 8 | ✅ PASÓ | Notificaciones por correo vía Brevo API para confirmación de pedidos y citas. |
| `WompiSandboxE2ETest` | 10 | ✅ PASÓ | Flujo end-to-end sandbox con firma de integridad, webhook y confirmación de pago. |

**Total de Pruebas Ejecutadas en la Suite Global**: 19+ clases / `BUILD SUCCESS`

---

## 2. MATRIZ DE ESCENARIOS DE NEGOCIO PROBADOS

1. **Cliente - Compra de Producto con Categoría Asignada**: Se verifica que la categoría real aparezca en la columna `CATEGORÍA` de la tabla administrativa.
2. **Cliente - Compra de Producto Sin Categoría**: Se verifica que se renderice `"Sin categoría asignada"` en la columna `CATEGORÍA`.
3. **Cliente - Compra de Kit Capilar**: Se verifica que el tipo `"Kit Capilar"` aparezca debajo del nombre en `NOMBRE & TIPO`, y su categoría en `CATEGORÍA`.
4. **Admin - Alternar Estado de Stock de Producto/Kit**: Se verifica que el botón de `En Stock` / `Agotado` en la columna 6 funcione y persista.
5. **Admin - Edición/Eliminación de Producto/Kit**: Se verifica que las acciones operativas de la columna 7 respondan correctamente.
6. **Mobile / Desktop Responsive**: Se verifica que la tabla `#adm-products-table-body` dentro del contenedor con `overflow-x-auto` mantenga alineación perfecta de las 7 columnas en cualquier resolución sin desbordamientos indeseados.

---

*Informe de Regresión producido por la Suite Automatizada ISIVI.*
