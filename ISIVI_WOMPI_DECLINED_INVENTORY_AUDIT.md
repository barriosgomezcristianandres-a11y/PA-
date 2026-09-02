# ISIVI Wompi Declined Inventory Flow Audit

Este documento detalla el análisis del flujo de inventario cuando una transacción de pago Wompi es rechazada o denegada en un **Pedido Puro** de productos en ISIVI.

## 1. Ciclo de Descuento de Inventario

El inventario se descuenta en la siguiente etapa del flujo:
1. **Creación del Pedido:** Cuando el cliente inicia el checkout, el endpoint `POST /api/reservas` (`ReservaController.java`) llama a `prepararNuevaReserva(solicitud)` en [ReservaService.java](file:///c:/Users/barri/OneDrive/Escritorio/antes%20de%20wompi/terminado1/isivi-app/src/main/java/com/isivi/app/service/ReservaService.java).
2. **Reserva Temporal:** Dentro de `prepararNuevaReserva()`, se ejecuta `reservarInventario(solicitud)` que llama a `descontarAtomico(item)` para cada producto/kit.
3. **Bloqueo:** Esto reduce el stock atómicamente en MongoDB y marca `reserva.setInventarioReservado(true)`.
   * *Razón:* Bloquear las unidades para evitar sobreventas (overselling) durante el tiempo de ventana que el cliente tiene para pagar con Wompi.

---

## 2. Recepción del Estado Webhook

El webhook en [PagoController.java](file:///c:/Users/barri/OneDrive/Escritorio/antes%20de%20wompi/terminado1/isivi-app/src/main/java/com/isivi/app/controller/PagoController.java) (`POST /api/pagos/wompi/webhook`) procesa el estado de la siguiente manera:
1. **Detección de Estados:** Identifica los estados no aprobados (`DECLINED`, `VOIDED`, `ERROR`).
2. **Tipo de Pedido:** Ejecuta `reserva.esPedidoPuro()` para comprobar si la orden contiene exclusivamente productos/kits.
3. **Actualización de Estados:**
   * Para pedidos puros: El estado se ajusta a `PENDIENTE_PAGO` (para permitir que el cliente intente pagar de nuevo). El estado de pago se guarda como `RECHAZADO` o `ERROR`.
   * Para citas y mixtos: El estado de la reserva se ajusta a `Denegada`.
4. **Liberación del Stock:** Llama a `reservaService.liberarInventario(reserva)`, la cual invoca `sumarStock(item)` y restituye las unidades en la base de datos atómicamente.

---

## 3. Idempotencia y Prevención de Doble Liberación

* **Guardia de Estado:** `liberarInventario()` verifica primero:
  ```java
  if (!Boolean.TRUE.equals(reserva.getInventarioReservado())) return;
  ```
* **Efecto:** La primera vez que el webhook procesa el rechazo, restaura el stock y establece `inventarioReservado` a `false`.
* **Idempotencia:** Si Wompi envía el webhook de rechazo por segunda vez o hay reintentos concurrentes, el flag es `false`, la función retorna inmediatamente y el stock no se incrementa de más.
