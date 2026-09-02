# ISIVI Delete & Archive Audit Report

Este documento recopila la auditoría de seguridad y las correcciones del flujo de eliminación física y archivado de registros en la administración de reservas y pedidos de ISIVI.

## 📄 Hallazgo 1: Eliminación física indiscriminada de pedidos confirmados y pagados

*   **ID:** AUDIT-DEL-01
*   **TIPO:** Seguridad y Consistencia de Datos / Lógica
*   **CRITICIDAD:** Alta
*   **ESCENARIO:** El administrador selecciona un pedido confirmado o pagado y presiona el botón de eliminar.
*   **ESPERADO:** La eliminación física de registros confirmados o pagados debe estar estrictamente bloqueada para evitar pérdida de datos operativos y financieros.
*   **REAL:** El endpoint `DELETE /api/reservas/{id}` eliminaba físicamente cualquier registro de la base de datos sin importar su estado (`Confirmado`, `Pago Confirmado`, etc.).
*   **CAUSA RAÍZ:** El endpoint de eliminación en [ReservaController.java](file:///c:/Users/barri/OneDrive/Escritorio/antes%20de%20wompi/terminado1/isivi-app/src/main/java/com/isivi/app/controller/ReservaController.java#L713) no realizaba ninguna validación del estado del pedido antes de invocar a `reservaRepository.delete()`.
*   **ARCHIVO:** [ReservaController.java](file:///c:/Users/barri/OneDrive/Escritorio/antes%20de%20wompi/terminado1/isivi-app/src/main/java/com/isivi/app/controller/ReservaController.java)
*   **FUNCIÓN:** `eliminar(String id)` y `eliminarTodas()`
*   **ENDPOINT:** `DELETE /api/reservas/{id}` y `DELETE /api/reservas`
*   **ENTIDAD:** `Reserva`
*   **CORRECCIÓN:** 
    1. Se implementó una verificación de estado en el backend para bloquear solicitudes en estados activos o pagados (`CONFIRMADO`, `PAGO_CONFIRMADO`, `EN_PREPARACION`, `LISTO_ENVIO`, `EN_CAMINO`, `ENTREGADO`, `LISTO_RECOGER`, `RECOGIDO`).
    2. Se implementó una lógica de archivado automático: si el registro a eliminar tiene trazabilidad financiera (`referenciaWompi`, `transaccionWompiId`, medio de pago `WOMPI` o `TRANSFERENCIA`), no se elimina físicamente; en su lugar, se marca con la bandera `archivada = true` y se establece la fecha actual de archivado.
    3. Si el registro no tiene huella financiera, se elimina físicamente del sistema tras liberar su inventario de forma segura.
*   **TEST:** `testEliminarReservaConfirmadaBloqueado`, `testEliminarReservaSinHuellaFinancieraFisico` y `testEliminarReservaConHuellaFinancieraArchivar` en [ResumePaymentRegressionTest.java](file:///c:/Users/barri/OneDrive/Escritorio/antes%20de%20wompi/terminado1/isivi-app/src/test/java/com/isivi/app/ResumePaymentRegressionTest.java).
*   **RESULTADO:** Exitoso. Las pruebas confirman que los estados operativos activos quedan bloqueados (retorna `400 Bad Request`), los registros con huella financiera se archivan de forma segura y solo los registros huérfanos se eliminan físicamente.

---

## 📄 Hallazgo 2: Botón de eliminación en interfaz visible para todos los registros

*   **ID:** AUDIT-DEL-02
*   **TIPO:** Interfaz de Usuario (UX/UI)
*   **CRITICIDAD:** Media
*   **ESCENARIO:** El administrador visualiza la tabla de reservas activas.
*   **ESPERADO:** Los registros confirmados o pagados no deben mostrar la opción de eliminación en la UI. Aquellos con huella financiera deben mostrar la etiqueta "Archivar" en lugar de "Eliminar".
*   **REAL:** Se mostraba un botón genérico de papelera roja ("Eliminar") para todas las filas de la tabla activa de reservas, lo cual permitía invocar la API para cualquier registro.
*   **CAUSA RAÍZ:** El template HTML de la fila en `renderAdminBookingsTable()` agregaba el botón `deleteBooking` incondicionalmente a todas las reservas.
*   **ARCHIVO:** [isivi.js](file:///c:/Users/barri/OneDrive/Escritorio/antes%20de%20wompi/terminado1/isivi-app/src/main/resources/static/js/isivi.js)
*   **FUNCIÓN:** `renderAdminBookingsTable()`
*   **ENDPOINT:** N/A (Frontend)
*   **ENTIDAD:** `Reserva`
*   **CORRECCIÓN:** Se añadieron los helpers `getDeleteBtnHtml(b)` y `getHistoryDeleteBtnHtml(b)`. Estos ocultan el control si el registro está en un estado operativo activo y muestran la etiqueta "[Archivar]" (con ícono de archivo de caja) si tiene trazabilidad financiera, o "[Eliminar]" (con papelera) si no la tiene. Asimismo, `deleteBooking()` valida las transiciones antes de invocar la API.
*   **TEST:** Ejecución interactiva del dashboard.
*   **RESULTADO:** Exitoso. La UI renderiza selectivamente los controles correctos según el estado y la trazabilidad financiera del registro.
