# ISIVI Payment Retry Audit

Este documento detalla el análisis de la lógica de reintento de pagos, la validación de stock y el control de concurrencia en pedidos puros de ISIVI.

## 1. Comportamiento ante el Reintento de Pago

Cuando un pago Wompi falla y el pedido es un **Pedido Puro**, el cliente puede retomar la transacción a través de la interfaz web (botón "Reintentar"):
1. **Llamada a Retomar:** Llama al endpoint `POST /api/pagos/wompi/retomar/{reservaId}`.
2. **Validación de Stock:** En `retomarPagoWompi(reserva)`, si el inventario fue liberado previamente (es decir, `inventarioReservado` es `false`), intenta re-reservar el stock atómicamente llamando a `reservarInventario(reserva)`.
3. **Escenario de Stock Agotado:** Si el stock ya no está disponible (porque otro cliente lo compró durante la ventana de pago fallido), la re-reserva falla y arroja una excepción con el mensaje `"El producto ya no está disponible."`.
4. **Resiliencia de Datos:** Durante todo este flujo, los datos del pedido (tales como dirección, teléfono, correo, etc.) se preservan intactos sin alterarse.

---

## 2. Prevención de Doble Descuento y Doble Liberación

* **Doble Descuento:** Tanto `prepararPagoWompi` como `retomarPagoWompi` realizan la reserva de inventario condicionalmente:
  ```java
  if (!Boolean.TRUE.equals(reserva.getInventarioReservado()) && tieneProducto) {
      reservarInventario(reserva);
  }
  ```
  Esto previene que se decremente el stock dos veces si el inventario ya estaba reservado.
* **Doble Liberación:** `liberarInventario()` cuenta con la misma guardia sobre el flag `inventarioReservado` (poniéndolo a `false` tras la primera liberación), impidiendo devoluciones redundantes de stock.

---

## 3. Códigos HTTP y Mensajería Unificada

* **Códigos HTTP:** 
  * Los conflictos de inventario directo durante checkout (creación de reserva) se retornan con **409 Conflict** (`ReservaController.java`).
  * Los fallos al preparar o retomar el pago sobre reservas ya creadas se manejan con **400 Bad Request** (`PagoController.java`).
* **Mensajes Unificados:**
  * Cantidad solicitada > stock: `"Solo quedan X unidades disponibles."`
  * Agotado / Concurrency: `"El producto acaba de agotarse. Actualizamos tu carrito."`
  * Reintento cuando stock ya fue consumido: `"El producto ya no está disponible."`
  * Pago rechazado: `"El pago no fue aprobado. Puedes intentarlo nuevamente."`
