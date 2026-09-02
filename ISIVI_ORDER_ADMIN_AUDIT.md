# ISIVI Order Admin Audit Report

Este documento recopila la auditoría detallada de los flujos de administración, confirmación de comprobantes, preparación y despacho logístico de pedidos en el panel administrativo de ISIVI.

## 📄 Hallazgo 1: Coherencia de estados en flujo de Transferencia manual

*   **ID:** AUDIT-ADM-01
*   **TIPO:** Flujo Operativo / Máquina de Estados
*   **CRITICIDAD:** Alta
*   **ESCENARIO:** El cliente realiza un pedido y selecciona método de pago transferencia. Envía el comprobante por WhatsApp.
*   **ESPERADO:** 
    1. El pedido debe quedar en estado `PENDIENTE_COMPROBANTE` y el pago en `PENDIENTE`.
    2. El administrador debe poder visualizar el comprobante y confirmar el pago.
    3. Al confirmar, el estado cambia a `PAGO_CONFIRMADO` y se habilita la acción de preparación.
    4. NO se debe permitir preparar, despachar o entregar el pedido si no se ha verificado el comprobante.
*   **REAL:** El backend permitía transicionar el pedido directamente a preparación sin que el pago estuviese verificado o aprobado, rompiendo la coherencia de negocio.
*   **CAUSA RAÍZ:** `validarTransicion` permitía cambiar de `PENDIENTE_COMPROBANTE` a cualquier estado operativo si se invocaban directamente los endpoints del controlador sin verificar el flag de confirmación de pago.
*   **ARCHIVO:** [ReservaService.java](file:///c:/Users/barri/OneDrive/Escritorio/antes%20de%20wompi/terminado1/isivi-app/src/main/java/com/isivi/app/service/ReservaService.java)
*   **FUNCIÓN:** `validarTransicion(Reserva, String)`
*   **ENDPOINT:** N/A (Servicios operativos internos)
*   **ENTIDAD:** `Reserva`
*   **CORRECCIÓN:** Se restringió `validarTransicion` y los endpoints operativos de preparación para exigir que el pago esté confirmado (`PAGO_CONFIRMADO` o estado de pago `APROBADO`) para cualquier pedido. De este modo, la opción de "Preparar" solo se habilita y procesa cuando el comprobante ha sido aprobado por el administrador.
*   **TEST:** `testFlujoCompletoClienteTransferenciaManual` en [BusinessE2ETest.java](file:///c:/Users/barri/OneDrive/Escritorio/antes%20de%20wompi/terminado1/isivi-app/src/test/java/com/isivi/app/BusinessE2ETest.java).
*   **RESULTADO:** Exitoso. Las transiciones logísticas indebidas quedan estrictamente bloqueadas hasta la confirmación de pago.

---

## 📄 Hallazgo 2: Exclusiones de método de entrega (Domicilio vs Recogida)

*   **ID:** AUDIT-ADM-02
*   **TIPO:** Reglas de Negocio / Flujo Logístico
*   **CRITICIDAD:** Media
*   **ESCENARIO:** Pedido para recoger en local (Pickup) vs Pedido a domicilio (Delivery).
*   **ESPERADO:**
    *   Pickup: `PENDIENTE_PREPARACION` -> `EN_PREPARACION` -> `LISTO_RECOGER` -> `RECOGIDO`. (Bloquear paso a "Listo para envío", "En camino" o "Entregado").
    *   Domicilio: `PENDIENTE_PREPARACION` -> `EN_PREPARACION` -> `LISTO_ENVIO` -> `EN_CAMINO` -> `ENTREGADO`. (Bloquear paso a "Listo para recoger" o "Recogido").
*   **REAL:** El sistema permitía transicionar cruzadamente (ej: un pedido para recoger en local pasaba a "En camino", o un pedido a domicilio pasaba a "Recogido").
*   **CAUSA RAÍZ:** No existían exclusiones de flujo por tipo de entrega en `validarTransicion()`.
*   **ARCHIVO:** [ReservaService.java](file:///c:/Users/barri/OneDrive/Escritorio/antes%20de%20wompi/terminado1/isivi-app/src/main/java/com/isivi/app/service/ReservaService.java)
*   **FUNCIÓN:** `validarTransicion(Reserva, String)`
*   **ENDPOINT:** N/A
*   **ENTIDAD:** `Reserva`
*   **CORRECCIÓN:** Se agregaron guardias específicas en `validarTransicion()`:
    ```java
    if (reserva.esPedido()) {
        if (esDomicilio && (LISTO_PARA_RECOGER.equalsIgnoreCase(destino) || RECOGIDO.equalsIgnoreCase(destino))) {
            throw new IllegalStateException("Los pedidos con entrega a domicilio no pueden pasar a estados de recogida en local.");
        }
        if (esRecogida && (LISTO_ENVIO.equalsIgnoreCase(destino) || EN_CAMINO.equalsIgnoreCase(destino) || ENTREGADO.equalsIgnoreCase(destino))) {
            throw new IllegalStateException("Los pedidos para recoger en el local no pueden pasar a estados de envío o entrega a domicilio.");
        }
    }
    ```
*   **TEST:** `testTransitionValidations` en [OrderWorkflowTest.java](file:///c:/Users/barri/OneDrive/Escritorio/antes%20de%20wompi/terminado1/isivi-app/src/test/java/com/isivi/app/OrderWorkflowTest.java).
*   **RESULTADO:** Exitoso. Las transiciones logísticas cruzadas están correctamente bloqueadas en base de datos.
