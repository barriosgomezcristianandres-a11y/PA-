# CATÁLOGO DE REGLAS DE NEGOCIO Y POLÍTICAS OPERATIVAS — ISIVI

Este documento recopila de manera estricta todas las reglas de negocio codificadas en el backend y frontend de ISIVI.

---

## 1. Reglas Financieras y de Precios
1. **Anticipo de Servicios (25%):**
   - Toda cita que contenga uno o más servicios exige un anticipo del 25% del valor total de los servicios para quedar confirmada (`subtotal * 0.25`).
   - El saldo restante (75%), denominado **Saldo en salón**, se paga directamente en el local el día de la cita (`subtotal - anticipo`).
2. **Pago Completo de Productos y Kits (100%):**
   - Toda compra de productos físicos o kits exige el 100% del pago por adelantado (`anticipo = subtotal`, `saldo = 0`).
3. **Revalidación Backend de Precios:**
   - El servidor nunca confía en el precio enviado en el payload JSON. `ReservaService.prepararNuevaReserva` consulta los precios vigentes de la base de datos (incluyendo ajustes por variantes) y recalcula subtotales, anticipos y saldos en el servidor.
4. **Monto en Centavos para Wompi:**
   - La pasarela Wompi opera en centavos COP. El backend convierte el valor a pagar mediante `Math.round(anticipo * 100)`.

---

## 2. Reglas de Retención y Expiración
1. **Tiempo de Retención (15 Minutos):**
   - Cuando una reserva entra en estado `Pendiente Pago`, el turno en la agenda y el stock de productos quedan reservados exclusivamente para el cliente durante **15 minutos** (`fechaExpiracionPago = Instant.now().plus(Duration.ofMinutes(15))`).
2. **Liberación por Expiración:**
   - Si transcurren los 15 minutos sin confirmación de pago:
     - El horario en la agenda se vuelve a habilitar inmediatamente en `/api/agenda/disponibilidad`.
     - El stock retenido se reincorpora al inventario (`liberarInventario`).
     - El registro pasa al estado `Expirada`.
3. **Retoma de Pago:**
   - Si un cliente intenta pagar una reserva expirada, `/api/reservas/retomar-pago` verifica nuevamente si el turno sigue libre y si hay stock disponible. Si se cumplen las condiciones, se genera una nueva retención de 15 minutos con una nueva referencia de pago Wompi.

---

## 3. Reglas de Cancelación y Políticas de 24 Horas
1. **Cancelación Automática con >24 Horas de Antelación:**
   - Si la diferencia entre la fecha y hora de la cita y el momento actual es **estrictamente mayor a 24 horas** (`cancelacionHorasMinimas = 24`), el cliente puede cancelar directamente desde el portal de autoservicio.
   - Efectos: Estado pasa a `Cancelada`, el turno se libera inmediatamente en la agenda, cualquier producto reservado se devuelve al stock y se despacha un email de confirmación de cancelación.
2. **Bloqueo de Cancelación con <=24 Horas:**
   - Si faltan **24 horas o menos** para la cita, el cliente **NO** puede cancelar automáticamente.
   - El sistema cambia el estado a `Solicitud Cancelación`, preserva el turno ocupado en la agenda y alerta al administrador en el panel de control.
   - El administrador puede:
     - **Aprobar Cancelación:** Pasa a `Cancelada` y libera el horario.
     - **Rechazar Cancelación:** Restablece la cita a `Confirmado` con un motivo de rechazo visible para el cliente.

---

## 4. Reglas de Reprogramación
1. **Antelación Requerida:** Solo se pueden reprogramar citas con más de 24 horas de antelación.
2. **Disponibilidad Estricta:** La nueva fecha y hora solicitada debe estar 100% disponible en la agenda. No se permiten solapamientos.

---

## 5. Reglas de Envíos y Logística
1. **Envío a domicilio:**
   - Si el cliente selecciona `tipoEntrega = "domicilio"`, el campo `direccionEntrega` es **estrictamente obligatorio** tanto en el formulario web como en la validación backend (`HTTP 400 DIRECCION_REQUERIDA`).
   - El email de confirmación incluye la dirección completa de entrega.
2. **Recogida en el local (Pickup):**
   - Si `tipoEntrega = "pickup"`, la dirección no es requerida.
   - Cuando el personal marca el pedido como `Listo para recoger`, se envía un email transaccional notificando al cliente que su paquete está disponible en la sede.
   - Si el pedido es a domicilio, la transición `Listo para recoger` **no** envía email de recogida en el local.

---

## 6. Reglas de Separación Operativa
1. **Pedidos Puros:**
   - Compras que contienen exclusivamente productos físicos o kits **nunca** bloquean la agenda de los profesionales, **nunca** aparecen en la lista de citas del día ni en el widget de próximas citas.
   - Viven exclusivamente en la sección **Pedidos** del panel de control.
2. **Compras Mixtas:**
   - Únicamente la parte del servicio reserva espacio en la agenda de turnos.
