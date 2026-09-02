# AUDITORÍA FINAL DE INCONSISTENCIAS Y ESTADOS — ISIVI

**Fecha:** 21 de Agosto de 2026  
**Objetivo:** Registro exhaustivo de validación de consistencia lógica, máquinas de estado y sincronización de datos.

---

## 1. Verificación de Inconsistencias Clave

### 1. ¿Cliente dice "Pagado" y Admin dice "Pendiente"?
- **Estado:** 🟢 **CERO INCONSISTENCIAS.**
- **Mecanismo:** El webhook de Wompi (`POST /api/pagos/wompi/webhook`) actualiza atómicamente el documento en MongoDB cambiando `estado = "Confirmado"` (o `"Pago Confirmado"`) y `estadoPago = "APROBADO"`. Tanto la consulta pública como el panel administrativo leen directamente el mismo documento de MongoDB en tiempo real.

### 2. ¿Cliente dice "Pedido entregado" y Admin dice "En camino"?
- **Estado:** 🟢 **CERO INCONSISTENCIAS.**
- **Mecanismo:** Los cambios de estado logísticos son controlados exclusivamente por los endpoints administrativos (`PATCH /api/reservas/{id}/pedido/*`). Cuando el admin pulsa *Entregar*, el documento pasa a `Entregado` (Domicilio) o `Recogido` (Pickup) y el cliente visualiza de inmediato la fase completada al refrescar o consultar.

### 3. ¿Pedido Pickup mostrando información de Domicilio o viceversa?
- **Estado:** 🟢 **CERO INCONSISTENCIAS.**
- **Mecanismo:** 
  - `isDeliv = true` $\rightarrow$ Muestra modalidad *"🏠 Envío a Domicilio"*, campo de dirección registrada y stepper de 5 pasos (*Pagado $\rightarrow$ Prep. $\rightarrow$ Listo Envío $\rightarrow$ En camino $\rightarrow$ Entregado*).
  - `isDeliv = false` $\rightarrow$ Muestra *"📍 Recoger en Salón ISIVI"*, instrucciones del local y stepper de 4 pasos (*Pagado $\rightarrow$ Prep. $\rightarrow$ Listo Recoger $\rightarrow$ Recogido*).

### 4. ¿Pedido Puro activando lógica de Agenda o Bloqueo de Horario?
- **Estado:** 🟢 **CERO INCONSISTENCIAS.**
- **Mecanismo:**
  - `setPendingHold` en `isivi.js` valida `if (isPureOrder || !hasService) return;`, impidiendo que pedidos de solo productos creen retenciones de agenda o inicien temporizadores de citas.
  - En el backend, `Reserva.esPedidoPuro()` establece `fechaCita = null` y `horaCita = null`, por lo que nunca aparecen en `findByFechaCita`.

### 5. ¿Reserva cancelada manteniendo horario bloqueado en la Agenda?
- **Estado:** 🟢 **CERO INCONSISTENCIAS.**
- **Mecanismo:**
  - En `ReservaController.bloqueaAgenda(r)`: si el estado es `Cancelada`, `Denegada` o `Expirada`, la función retorna `false`.
  - El horario se libera inmediatamente en `/api/agenda/disponibilidad`.

### 6. ¿Doble cobro o doble reserva ante concurrencia extrema?
- **Estado:** 🟢 **CERO INCONSISTENCIAS.**
- **Mecanismo:**
  - MongoDB Atlas aplica el índice único compuesto parcial `reserva_fecha_hora_activa_idx` sobre `(fechaCita, horaCita)` para reservas activas (`archivada != true`, `estado != 'Cancelada'`, `'Denegada'`, `'Expirada'`).
  - Si dos peticiones compiten por el mismo slot, una obtiene `201 Created` y la otra es rechazada con `409 Conflict`.

---

## 2. Matriz de Estados y Separación Conceptual

### Relación `estadoPago` vs `estadoPedido` / `estado`:
| Tipo de Transacción | `estadoPago` | `estado` | `estadoPedido` | Efecto en Agenda | Efecto en Inventario |
|---|---|---|---|---|---|
| **Cita Agendada** | `APROBADO` (25% anticipo) | `Confirmado` | `null` | Bloquea slot horario | N/A |
| **Cita + Productos** | `APROBADO` (25% total) | `Confirmado` | `null` | Bloquea slot horario | Stock decrementado |
| **Pedido Domicilio** | `APROBADO` (100%) | `Pago Confirmado` $\rightarrow$ `En preparación` $\rightarrow$ `Listo para envío` $\rightarrow$ `En camino` $\rightarrow$ `Entregado` | `PENDIENTE_PREPARACION` $\rightarrow$ `EN_PREPARACION` $\rightarrow$ `LISTO_ENVIO` $\rightarrow$ `EN_CAMINO` $\rightarrow$ `ENTREGADO` | No ocupa agenda | Stock decrementado |
| **Pedido Pickup** | `APROBADO` (100%) | `Pago Confirmado` $\rightarrow$ `En preparación` $\rightarrow$ `Listo para recoger` $\rightarrow$ `Recogido` | `PENDIENTE_PREPARACION` $\rightarrow$ `EN_PREPARACION` $\rightarrow$ `LISTO_RECOGER` $\rightarrow$ `RECOGIDO` | No ocupa agenda | Stock decrementado |
| **Cancelada (>24h)** | `REEMBOLSADO` / N/A | `Cancelada` | `CANCELADO` | Libera slot horario | Stock reincorporado |
| **Expirada (15m)** | `PENDIENTE` | `Expirada` | `EXPIRADO` | Libera slot horario | Stock reincorporado |

---

## 3. Estado de Hallazgos Previos

1. **Categorías dinámicas en Kits:** ✅ Resuelto y probado (sin fallback destructivo a "Todos" ante 0 resultados).
2. **Parser estricto de precios COP:** ✅ Resuelto y probado (formato con separador de miles, sin ambigüedades).
3. **Separación de retenciones Cita vs Pedido Puro:** ✅ Resuelto y probado.
4. **Stepper público adaptativo:** ✅ Resuelto y probado para Domicilio (5 pasos) y Pickup (4 pasos).
5. **Sincronización de rutas documentadas:** ✅ Resuelto en `ISIVI_API.md` e `ISIVI_FLOWS.md`.
