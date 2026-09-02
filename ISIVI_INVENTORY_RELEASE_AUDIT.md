# ISIVI Inventory Release Audit

Este documento detalla el análisis de la liberación y devolución de inventario, así como la actualización del catálogo del cliente.

## 1. Funcionamiento de `liberarInventario()`

El método `liberarInventario(Reserva)` en [ReservaService.java](file:///c:/Users/barri/OneDrive/Escritorio/antes%20de%20wompi/terminado1/isivi-app/src/main/java/com/isivi/app/service/ReservaService.java) es el encargado de retornar las unidades al stock:
1. **Verificación de Guardia:** Comprueba que `inventarioReservado` sea `true` antes de proceder.
2. **Iteración de Ítems:** Filtra los artículos que no sean "servicio" (es decir, productos, variantes y kits) y llama a `sumarStock(item)`.
3. **Guardia de Flag:** Al final, establece `reserva.setInventarioReservado(false)`.

---

## 2. Identificación Precisa de Artículos en `sumarStock()`

El stock no se incrementa usando nombres o cadenas de texto ambiguas. Se utiliza el identificador único canónico (`id` de MongoDB) y la variante correspondiente:
* **Variantes de Producto:** Si el item posee un `varianteId`, busca el producto por su `id` y la variante correspondiente en la colección de variantes usando query selectivo en MongoDB. Incrementa la cantidad de la variante, activa `enStock = true` de la variante, e incrementa la cantidad general y `enStock` del producto padre.
* **Productos Únicos:** Incrementa la cantidad general del producto y establece `enStock = true`.
* **Kits:** Incrementa la cantidad del kit y establece `enStock = true`.

---

## 3. Actualización Inmediata del Catálogo

Cuando ocurre una liberación en el backend, el stock queda disponible en la base de datos de manera inmediata. Para asegurar que el frontend no muestre información desactualizada ("AGOTADO"):
1. **Invalidación de Caché:** El frontend utiliza `apiCache` (`isivi.js`) con un tiempo de expiración ultra-corto de 3 segundos.
2. **Refresco Inmediato:** Ante errores de inventario (`409 Conflict`), la función `refreshCatalogStock()` invalida la caché explícitamente llamando a:
   ```javascript
   apiCache.delete('/productos');
   apiCache.delete('/kits');
   ```
   E inicia una consulta fresca para repintar la UI con las cantidades correctas.
