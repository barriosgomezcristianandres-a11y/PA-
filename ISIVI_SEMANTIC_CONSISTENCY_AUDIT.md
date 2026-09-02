# Reporte de Auditoría de Coherencia Semántica y Terminológica — ISIVI

Este documento resume los hallazgos de incoherencia semántica detectados en el sistema ISIVI, la terminología unificada adoptada y los cambios aplicados en el frontend, backend y documentación para cumplir con las reglas canónicas del dominio.

---

## 1. Matriz de Terminología Canónica Implementada

Se adoptó de manera obligatoria y consistente la siguiente matriz terminológica en la presentación del sistema:

| Concepto | Terminología Anterior (Incoherente) | Terminología Correcta (Canónica) |
| :--- | :--- | :--- |
| **Servicio** | `Servicios / Productos` (en citas puras) | `Servicio` (o `Servicios` si son múltiples) |
| **Lugar de cita** | `Punto de Entrega` / `Entrega` (en citas) | `Lugar de la cita` |
| **Entrega de Productos** | `Entrega` (en operaciones mixtas) | `Entrega de productos` |
| **Entrega en Pedidos** | `Lugar de la cita` (en pedidos puros) | `Entrega` |
| **Recogida en local** | `Recogida en salón` / `Recoge en el local` | `Recogida en el local` / `Recoger en el local` |
| **Envío a domicilio** | `Domicilio` | `Envío a domicilio` |
| **Anticipo** | `Anticipo pagado (25% cita + productos)` | `Anticipo (25%)` |
| **Saldo** | `Saldo pendiente en peluquería / salón` | `Saldo en salón` |
| **Pago Total (Pedido)** | `Total pagado` / `Anticipo` (en pedidos) | `Pago total` |
| **Turno en Pedido** | `Sin fecha de cita · Sin turno` (en pedidos) | `Solo despacho de productos` |

---

## 2. Hallazgos y Correcciones por Pantalla

### A. Pantalla de Éxito de Checkout (Cliente)
- **Hallazgo**: Mostraba la etiqueta estática `"Servicios y Productos"` incluso si la transacción era únicamente una cita de servicios o una compra de un kit físico.
- **Hallazgo**: Mostraba `"Saldo pendiente en peluquería:"` mezclando la palabra peluquería en lugar de salón.
- **Corrección**: Se añadió el id `success-items-label` y en `isivi.js` se dinamizó la etiqueta:
  - Cita pura -> `Servicios`
  - Pedido puro -> `Productos y Kits`
  - Mixto -> `Servicios y Productos`
- **Corrección**: Se cambió la etiqueta de saldo a `"Saldo en salón:"`.

### B. Buscador de Reservas y Pedidos (Autoservicio Cliente)
- **Hallazgo**: Si se consultaba un pedido de productos, la sección de `"Fecha y Hora"` mostraba de forma hardcodeada `"Recoge en el local"` (incluso si era un envío a domicilio).
- **Hallazgo**: Mostraba la sección de `"Anticipo"` para compras de productos físicos, lo cual es incorrecto porque el pago de productos es del 100% por adelantado.
- **Corrección**: Se dividieron y dinamizaron los bloques:
  - Si es pedido puro: se oculta la sección de `"Fecha y Hora"`, se muestra una sección específica de `"Entrega"` indicando `Envío a domicilio` o `Recoger en el local` (con la dirección real), y se ocultan los desgloses de anticipo y saldo en salón (mostrando solo el pago total).
  - Si es cita/mixto: se despliega la fecha/hora de la cita, se muestra el anticipo pagado y el `Saldo en salón` pendiente.

### C. Modal de Detalle de Cita (Administración)
- **Hallazgo**: Para pedidos de productos puros, se mostraba la leyenda `"Sin fecha de cita · Sin turno"`, lo que daba la impresión de datos faltantes o corruptos.
- **Hallazgo**: Se mostraba `"Saldo pendiente en salón"` en lugar del término unificado.
- **Corrección**: Se implementaron selectores condicionales en `renderAdminBookingDetailModal()`:
  - Si es pedido puro, muestra la leyenda `"Solo despacho de productos"`, cambia la etiqueta financiera a `"Pago total:"` y oculta/muestra la sección de entrega con el título `"Entrega"`.
  - Si es mixto, muestra la sección de entrega con el título `"Entrega de productos"`.

### D. Tabla de Pedidos y Detalle de Pedido (Administración)
- **Hallazgo**: Se utilizaban inconsistencias como `"Domicilio"` vs `"Envío a domicilio"` and `"Recoge en salón"` vs `"Recoger en el local"`.
- **Corrección**: Se unificaron las strings de retorno en `renderAdminOrdersTable()` y `renderAdminOrderDetailModal()` a `"Envío a domicilio"` y `"Recoger en el local"` respectivamente.

---

## 3. Pruebas y Aseguramiento de Calidad

### Prueba Automatizada: `SemanticConsistencyTest.java`
Se creó una prueba integral de coherencia semántica en [`SemanticConsistencyTest.java`](file:///c:/Users/barri/OneDrive/Escritorio/antes%20de%20wompi/terminado1/isivi-app/src/test/java/com/isivi/app/SemanticConsistencyTest.java) que realiza:
1. **Análisis estático de `index.html`**: Garantiza que no existan etiquetas duras obsoletas como `"Saldo pendiente en peluquería"` o `"Punto de Entrega"`.
2. **Análisis estático de `isivi.js`**: Valida la inexistencia de `"Recogida en salón"`.
3. **Validación de dinamización**: Asegura que el mapeo de `success-items-label`, `lookup-res-items-label` y `adm-detail-items-label` se ejecute condicionalmente de acuerdo al tipo de reserva (Cita pura, Pedido puro y Mixto).
4. **Preservación de contratos de API**: Valida que no se alteraron los contratos de payload JSON del backend (`anticipo`, `saldo`, `tipoEntrega`, etc.).

---

## 4. Resultados de Verificación y Compilación

- **SemanticConsistencyTest**: **PASÓ (6/6 tests exitosos)**.
- **mvn test**: **BUILD SUCCESS** (337/337 tests exitosos en total).
- **mvn clean package -DskipTests**: **BUILD SUCCESS** (archivo compilado listo para producción).
- **Repositorio**: Cambios subidos a la rama `main` en GitHub.
