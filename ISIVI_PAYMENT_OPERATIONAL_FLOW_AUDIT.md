# ISIVI Payment and Operational Flow Audit Report

Este documento recopila la auditoría detallada de la máquina de estados de pagos y flujos de pasarela de pago Wompi en ISIVI.

## 📄 Hallazgo 1: Máquina de estados ante pago rechazado (DECLINED / ERROR)

*   **ID:** AUDIT-PAY-01
*   **TIPO:** Máquina de Estados / Lógica de Negocio
*   **CRITICIDAD:** Alta
*   **ESCENARIO:** Un pedido puro de productos con stock = 1 recibe una respuesta `DECLINED` de Wompi.
*   **ESPERADO:** 
    1. El pedido puro debe volver al estado `PENDIENTE_PAGO`.
    2. El stock temporal debe ser liberado de inmediato en MongoDB.
    3. El catálogo y el carrito deben volver a sincronizarse con el stock real = 1.
    4. El pedido no debe interferir con la agenda (fecha/hora nulos).
*   **REAL:** El stock quedaba bloqueado en 0 de forma permanente si el pago fallaba o se cancelaba, y el usuario no podía intentar de nuevo la compra.
*   **CAUSA RAÍZ:** Aunque `liberarInventario()` existía, no se coordinaba adecuadamente con el webhook de Wompi ni se gestionaba el flag `inventarioReservado` de manera atómica, impidiendo que el stock retornara a la base de datos tras rechazos en pasarela.
*   **ARCHIVO:** [PagoController.java](file:///c:/Users/barri/OneDrive/Escritorio/antes%20de%20wompi/terminado1/isivi-app/src/main/java/com/isivi/app/controller/PagoController.java)
*   **FUNCIÓN:** `webhook(JsonNode evento, String checksum)`
*   **ENDPOINT:** `POST /api/pagos/wompi/webhook`
*   **ENTIDAD:** `Reserva`
*   **CORRECCIÓN:** Se estructuró el bloque `else` del webhook en `PagoController.java` para manejar los estados `DECLINED`, `VOIDED` y `ERROR`. Ante estos estados:
    1. Si es pedido puro, el estado cambia a `PENDIENTE_PAGO` y se invoca a `reservaService.liberarInventario(reserva)`.
    2. Si es cita o mixto, el estado cambia a `Denegada` y se invoca a `liberarInventario(reserva)`.
    3. Se persiste el cambio llamando a `reservas.save(reserva)`.
*   **TEST:** `testTraceDeFlujoCompletoConWompiDeclined` en [ResumePaymentRegressionTest.java](file:///c:/Users/barri/OneDrive/Escritorio/antes%20de%20wompi/terminado1/isivi-app/src/test/java/com/isivi/app/ResumePaymentRegressionTest.java).
*   **RESULTADO:** Exitoso. Tras un pago rechazado, el stock retorna a 1, `enStock` pasa a `true` y el catálogo se refresca correctamente.

---

## 📄 Hallazgo 2: Falta de Idempotencia del Webhook Wompi

*   **ID:** AUDIT-PAY-02
*   **TIPO:** Consistencia de Datos / Concurrencia
*   **CRITICIDAD:** Alta
*   **ESCENARIO:** Wompi envía el evento `DECLINED` o `VOIDED` de forma duplicada o redundante debido a reintentos de red.
*   **ESPERADO:** El stock inicial (1 unidad) debe ser liberado exactamente una vez. Las llamadas subsiguientes no deben alterar el stock (debe seguir en 1).
*   **REAL:** El stock se incrementaba acumulativamente en cada llamada, provocando stock duplicado (ej: stock = 2 en lugar de 1).
*   **CAUSA RAÍZ:** `liberarInventario()` no verificaba si la reserva ya había liberado previamente sus ítems antes de llamar a `sumarStock()`.
*   **ARCHIVO:** [ReservaService.java](file:///c:/Users/barri/OneDrive/Escritorio/antes%20de%20wompi/terminado1/isivi-app/src/main/java/com/isivi/app/service/ReservaService.java)
*   **FUNCIÓN:** `liberarInventario(Reserva)`
*   **ENDPOINT:** N/A (Servicio interno)
*   **ENTIDAD:** `Reserva`
*   **CORRECCIÓN:** Se añadió una guardia de estado en `liberarInventario()`:
    ```java
    if (!Boolean.TRUE.equals(reserva.getInventarioReservado())) return;
    // ... liberar ...
    reserva.setInventarioReservado(false);
    ```
    Esto garantiza que la liberación de stock ocurra una sola vez. Las llamadas repetidas retornan inmediatamente sin alterar el inventario.
*   **TEST:** Simulación de webhook DECLINED duplicado en `testTraceDeFlujoCompletoConWompiDeclined`.
*   **RESULTADO:** Exitoso. Las llamadas repetidas mantienen el stock en 1 sin generar duplicados indeseados.
