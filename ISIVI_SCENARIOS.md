# REGISTRO DETALLADO DE ESCENARIOS DE PRUEBA E2E — ISIVI

Este documento detalla la ejecución paso a paso de los 21 escenarios principales de prueba simulando usuarios y administradores reales.

---

## FASE 2: PERFIL CLIENTE

### Escenario A — Visita Inicial y Catálogo
- **Acción:** Acceso a la página principal y carga de catálogo.
- **Ruta / API:** `GET /api/productos`, `GET /api/kits`, `GET /api/servicios`, `GET /api/categorias-producto`.
- **Verificación:** Catálogo responde sin errores, productos con precio en miles (COP), categorías activas filtradas, sin estados indefinidos.
- **Resultado:** ✅ **PASÓ**

### Escenario B — Producto Simple (Pickup)
- **Acción:** Selección de producto, tipo de entrega "Recoger en Salón", checkout y pago.
- **Backend / MongoDB:** Se crea reserva con `tipoEntrega="pickup"`, `direccionEntrega=null`, `fechaCita=null`, `horaCita=null`.
- **Inventario:** Stock decrementado atómicamente con `$inc: -cantidad`.
- **Agenda:** No ocupa agenda (`findByFechaCita` no incluye este pedido).
- **Checkout UX:** Muestra *"Completa el pago para confirmar tu pedido y continuar con la preparación."*. No activa banner de cita ni timer de horario.
- **Resultado:** ✅ **PASÓ**

### Escenario C — Producto con Domicilio
- **Acción:** Selección de producto y envío a domicilio.
- **Validación:**
  1. Intento sin dirección $\rightarrow$ Rechazado con `HTTP 400 DIRECCION_REQUERIDA`.
  2. Con dirección $\rightarrow$ Creado exitosamente con `tipoEntrega="delivery"`, `direccionEntrega="Manga 3ra Ave # 21-45"`.
- **Checkout UX:** Muestra *"Completa el pago para confirmar tu pedido y continuar con el envío."*.
- **Resultado:** ✅ **PASÓ**

### Escenario D — Cita Simple (Servicio)
- **Acción:** Selección de servicio ("Alisado Orgánico"), fecha y hora en `/disponibilidad`.
- **Financiero:** Subtotal = 120.000 COP, Anticipo (25%) = 30.000 COP, Saldo restante = 90.000 COP.
- **Agenda:** Horario bloqueado en `/disponibilidad`.
- **Hold:** Retención de 15 minutos en `fechaExpiracionPago`.
- **Checkout UX:** Muestra *"Horario reservado temporalmente mientras completas tu pago."* y banner *"TU RESERVA SIGUE ACTIVA"*.
- **Resultado:** ✅ **PASÓ**

### Escenario E — Cita + Productos (Compra Mixta)
- **Acción:** Servicio de corte + Aceite de Argán.
- **Validación:** Se clasifica como Cita (`esCita()=true`, `esMixto()=true`), anticipo 25% del total combinado, stock de producto descontado inmediatamente, horario bloqueado.
- **Resultado:** ✅ **PASÓ**

### Escenario F — Cambio de Horario / Reprogramación
- **Acción:** Cliente reprograma con >24 horas de antelación.
- **Validación:** Se libera el horario antiguo y se bloquea el nuevo en la agenda. Horarios pasados o cerrados son rechazados.
- **Resultado:** ✅ **PASÓ**

### Escenario G — Cancelación de Cita
- **Acción:**
  - Caso 1 (>24 horas): Cancelación automática, estado `Cancelada`, horario liberado de inmediato en agenda, stock devuelto si aplica.
  - Caso 2 (<=24 horas): Auto-cancelación bloqueada con `CancelacionNoPermitidaException` para revisión administrativa.
- **Resultado:** ✅ **PASÓ**

### Escenario H — Expiración de Hold (15 Minutos)
- **Acción:** Reserva pendiente que supera los 15 minutos.
- **Validación:** `expirarReservasVencidas()` marca estado `Expirada`, devuelve stock al inventario y libera el horario en la agenda.
- **Resultado:** ✅ **PASÓ**

### Escenario I — Abandono y Reanudación de Checkout
- **Acción:** Cliente cierra el modal de Wompi e intenta pagar posteriormente.
- **Validación:** Endpoint de retoma re-valida si el horario y stock siguen libres, calculando nueva referencia Wompi y extendiendo el hold.
- **Resultado:** ✅ **PASÓ**

### Escenario J — Consulta Unificada (Autoservicio)
- **Acción:** Consulta de orden mediante código `ISV-...` y teléfono.
- **Validación:** 
  - Para Pedidos: Stepper de 4 fases, detalle de entrega (domicilio con dirección o local con sede).
  - Para Citas: Fecha, hora, profesional, anticipo, saldo y botones de cancelación / reprogramación / calendario.
- **Resultado:** ✅ **PASÓ**

### Escenario K — Concurrencia Extrema
- **Acción:** Múltiples hilos intentando reservar el mismo turno exacto.
- **Validación:** MongoDB aplica el índice único parcial `reserva_fecha_hora_activa_idx`. Exactamente 1 cliente obtiene `201 Created`, los demás reciben `409 Conflict`.
- **Resultado:** ✅ **PASÓ**

---

## FASE 3: PERFIL ADMINISTRADOR

### Escenario Admin A — Visualización de Citas
- **Validación:** Aparecen en módulo de citas y dashboard diario con desglose de anticipo vs saldo en caja.
- **Resultado:** ✅ **PASÓ**

### Escenario Admin B — Visualización de Pedidos
- **Validación:** Pedidos puros viven exclusivamente en la sección Pedidos y no ensucian el calendario de turnos.
- **Resultado:** ✅ **PASÓ**

### Escenario Admin C — Ciclo Logístico Domicilio
- **Flujo:** `Pago Confirmado` $\rightarrow$ `En preparación` $\rightarrow$ `Listo para envío` $\rightarrow$ `En camino` $\rightarrow$ `Entregado`.
- **Validación:** Transiciones inválidas (ej. intentar marcar pickup) son rechazadas por el backend.
- **Resultado:** ✅ **PASÓ**

### Escenario Admin D — Ciclo Logístico Pickup
- **Flujo:** `Pago Confirmado` $\rightarrow$ `En preparación` $\rightarrow$ `Listo para recoger` $\rightarrow$ `Recogido`.
- **Validación:** Intento de marcar `En camino` en un pickup es rechazado con `400 Bad Request`.
- **Resultado:** ✅ **PASÓ**

### Escenario Admin E — Gestión de Categorías
- **Validación:** Creación de categorías, asignación a productos/kits, toggle activo/inactivo. Intento de eliminar una categoría con productos asociados es bloqueado con `409 Conflict`.
- **Resultado:** ✅ **PASÓ**

### Escenario Admin F — Seguridad y Recálculo de Precios
- **Validación:** Si un atacante envía un subtotal manipulado en el payload (ej. 100 COP en vez de 150.000 COP), el servidor ignora el precio enviado, consulta la base de datos y recalcula el monto exacto.
- **Resultado:** ✅ **PASÓ**
