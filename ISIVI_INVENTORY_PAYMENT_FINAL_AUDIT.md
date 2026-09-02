# ISIVI Inventory and Payment Final Audit Report

Este documento recopila la auditoría final de inventarios atómicos, variantes de productos, kits, sincronización de carrito y control de concurrencia en ISIVI.

## 📄 Hallazgo 1: Sincronización del Carrito ante cambio de Stock

*   **ID:** AUDIT-INV-01
*   **TIPO:** Experiencia de Usuario (UX) / Consistencia de Datos
*   **CRITICIDAD:** Alta
*   **ESCENARIO:** El cliente tiene 3 unidades en el carrito, pero el stock disminuye a 1 o se agota antes del checkout.
*   **ESPERADO:** 
    1. Si cantidad > stock real, ajustar la cantidad al stock real y alertar al usuario: `"El stock disponible cambió. Ajustamos tu cantidad a X unidades."`
    2. Si stock = 0, remover del carrito y alertar: `"El producto se agotó y fue retirado de tu carrito."`
    3. Aplicar a productos, variantes y kits por igual.
*   **REAL:** El carrito reducía la cantidad silenciosamente o fallaba en checkout sin explicar el motivo al usuario, lo que generaba fricción.
*   **CAUSA RAÍZ:** `syncCartState()` realizaba la sincronización interna de variables sin interactuar con el DOM ni emitir avisos (toasts) claros al cliente.
*   **ARCHIVO:** [isivi.js](file:///c:/Users/barri/OneDrive/Escritorio/antes%20de%20wompi/terminado1/isivi-app/src/main/resources/static/js/isivi.js)
*   **FUNCIÓN:** `syncCartState()`
*   **ENDPOINT:** N/A (Frontend)
*   **ENTIDAD:** Carrito cliente
*   **CORRECCIÓN:** Se modificó `syncCartState()` para recorrer todos los productos, variantes y kits en el carrito y compararlos contra el catálogo fresco. Si detecta reducción o agotamiento, realiza el ajuste en el estado y emite un toast informativo visible (`showToast`).
*   **TEST:** Simulación de cambios de inventario en pruebas frontend.
*   **RESULTADO:** Exitoso. La UI refleja claramente los cambios de stock y previene transacciones inconsistentes.

---

## 📄 Hallazgo 2: Control de concurrencia y sobreventas en checkout simultáneo

*   **ID:** AUDIT-INV-02
*   **TIPO:** Concurrencia / Integridad de Datos
*   **CRITICIDAD:** Crítica
*   **ESCENARIO:** Dos clientes intentan comprar el último producto disponible (stock = 1) de forma simultánea.
*   **ESPERADO:** Exactamente una solicitud debe resultar exitosa (201 Created) y la otra debe fallar de forma controlada (409 Conflict) devolviendo el mensaje `"El producto acaba de agotarse. Actualizamos tu carrito."`. El stock final debe quedar exactamente en 0.
*   **REAL:** En entornos altamente concurrentes, existía el riesgo de sobreventas (stock final < 0 o dos transacciones exitosas vendiendo la misma unidad).
*   **CAUSA RAÍZ:** Falta de operaciones atómicas en MongoDB para la reducción y chequeo de stock en una única consulta de actualización.
*   **ARCHIVO:** [ReservaService.java](file:///c:/Users/barri/OneDrive/Escritorio/antes%20de%20wompi/terminado1/isivi-app/src/main/java/com/isivi/app/service/ReservaService.java)
*   **FUNCIÓN:** `descontarAtomico(ItemReserva)`
*   **ENDPOINT:** `POST /api/reservas`
*   **ENTIDAD:** `Producto` / `Kit`
*   **CORRECCIÓN:** Se implementaron consultas atómicas en `descontarAtomico` utilizando `mongoTemplate.updateFirst` con filtros condicionales:
    `Query.query(Criteria.where("id").is(item.getId()).and("cantidad").gte(item.getCantidad()))`
    Esto asegura que MongoDB verifique la disponibilidad y reste la cantidad en una sola operación de CPU. Si el resultado es 0 modificaciones, el stock no estaba disponible y se aborta arrojando la excepción correspondiente.
*   **TEST:** `testConcurrenciaUltimoProducto` en [ResumePaymentRegressionTest.java](file:///c:/Users/barri/OneDrive/Escritorio/antes%20de%20wompi/terminado1/isivi-app/src/test/java/com/isivi/app/ResumePaymentRegressionTest.java).
*   **RESULTADO:** Exitoso. Las pruebas concurrentes con múltiples hilos confirman que solo un cliente logra concretar la compra, y el stock final permanece seguro en 0.
