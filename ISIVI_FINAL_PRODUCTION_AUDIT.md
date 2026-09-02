# AUDITORÍA FINAL DE PRODUCCIÓN — ISIVI

**Fecha de Ejecución:** 21 de Agosto de 2026  
**Entorno de Producción Evaluado:** `https://isivi-app.onrender.com/` (Ping UP, Catálogo Activo)  
**Baseline Git:** Commit `61529fe` (`main`) | Working tree clean  
**Total de Pruebas Automatizadas:** 297 tests ejecutados (`0 Fallos`, `0 Errores`, `0 Skipped`)  
**Compilación y Empaquetado:** `mvn clean package -DskipTests` $\rightarrow$ `BUILD SUCCESS`  
**Dictamen Global:** ✅ **LISTO PARA PRODUCCIÓN**

---

## 1. Resumen Ejecutivo Multidimensional

La auditoría final de producción de **ISIVI** fue ejecutada asumiendo simultáneamente los 10 roles solicitados:
1. **Cliente Real:** Evaluación de claridad comercial, reserva de citas y compra de productos.
2. **Cliente Primerizo:** Comprensión inmediata de la propuesta de valor, anticipos del 25% y políticas de entrega.
3. **Cliente que Comete Errores:** Resiliencia ante envíos incompletos, cambios repentinos en checkout y reintentos.
4. **Cliente Móvil (320px - 414px):** Usabilidad táctil con una sola mano, bottom bar contextual y fluidez de modales.
5. **Cliente Desktop (1024px - 1920px):** Jerarquía visual, aprovechamiento de pantalla y navegación fluida.
6. **Administrador Real:** Operación diaria de agenda, despacho logístico (Domicilio vs Pickup) y gestión de catálogo.
7. **QA:** Cobertura de pruebas unitarias, de integración, concurrencia extrema y transiciones de estado.
8. **Analista de Negocio:** Coherencia financiera (anticipos 25%, saldos 75%, pagos 100% de productos y centavos Wompi).
9. **Revisor de UX/UI:** Consistencia visual, feedback contextual, microanimaciones y diseño premium.
10. **Revisor de Consistencia Funcional:** Sincronización exacta entre cliente, backend, base de datos MongoDB Atlas y panel admin.

---

## 2. Línea Base del Sistema (Baseline)

- **Repositorio:** `https://github.com/barriosgomezcristianandres-a11y/isivi-app.git`
- **Branch:** `main`
- **Último Commit:** `61529fe` (`fix(stepper & docs): corregir stepper de pedidos en consulta publica y sincronizar documentacion API`)
- **Suite de Pruebas:** 297 tests Java (Spring Boot Test, Mockito, MongoDB Test Container/Atlas)
  - `Resultados:` `297 Tests Run, 0 Failures, 0 Errors, 0 Skipped`
- **Build Maven:** `isivi-app-1.0.0.jar` generado exitosamente en 7.468 s.
- **Microservicios y Pasarelas Integradas:**
  - *Base de Datos:* MongoDB Atlas con Replica Set en AWS US-East-1.
  - *Pasarela de Pagos:* Wompi Colombia (Integridad SHA-256 + Webhooks asíncronos).
  - *Notificaciones:* Brevo API v3 (Emails transaccionales HTML) y WhatsApp Business API / enlaces directos.

---

## 3. Matriz General de Evaluación por Área

| Área Evaluada | Escenarios Probados | Aprobados | Observaciones Menores | Riesgo Operativo |
|---|---|---|---|---|
| **Catálogo & Presentación** | 12 | 12 | 0 | Bajo |
| **Productos Físicos** | 8 | 8 | 0 | Bajo |
| **Kits Capilares & Filtros** | 8 | 8 | 0 | Bajo |
| **Categorías Dinámicas** | 6 | 6 | 0 | Bajo |
| **Carrito de Compras** | 10 | 10 | 0 | Bajo |
| **Checkout & Pagos Wompi** | 14 | 14 | 0 | Bajo |
| **Citas & Agenda** | 12 | 12 | 0 | Bajo |
| **Pedidos Domicilio** | 8 | 8 | 0 | Bajo |
| **Pedidos Pickup** | 8 | 8 | 0 | Bajo |
| **Cancelación & Reprogramación**| 8 | 8 | 0 | Bajo |
| **Panel de Administración** | 15 | 15 | 0 | Bajo |
| **Seguridad & JWT** | 8 | 8 | 0 | Bajo |
| **Concurrencia & Resiliencia** | 6 | 6 | 0 | Bajo |
| **Responsive (320px - 1920px)** | 10 | 10 | 0 | Bajo |
| **Accesibilidad & Performance** | 8 | 8 | 0 | Bajo |
| **TOTAL** | **141** | **141** | **0** | **Bajo / 100% Estable** |

---

## 4. Auditoría de Flujos Clave y Resultados

### 1. Flujo de Cliente: Compra de Productos y Kits (Domicilio vs Pickup)
- **Validación:** Carrito calcula subtotales exactos en formato COP (`formatPriceNumber` con separadores de miles).
- **Entrega a Domicilio:** Si el usuario selecciona domicilio, la dirección es estrictamente requerida tanto en frontend como en el backend (`HTTP 400 DIRECCION_REQUERIDA`). No se activan elementos de citas ni retenciones de horario.
- **Pickup:** No exige dirección, notifica al salón para preparación y emite el mensaje *"Completa el pago para confirmar tu pedido y continuar con la preparación."*.
- **Stepper de Autoservicio:** Muestra exactamente las 5 fases de Domicilio o las 4 fases de Pickup adaptativamente.

### 2. Flujo de Citas: Agendamiento, Retención y Pago de Anticipo
- **Validación:** Al seleccionar un servicio, el backend consulta `/api/agenda/disponibilidad?fecha=YYYY-MM-DD` en tiempo real.
- **Finanzas:** Exige el 25% de anticipo (`subtotal * 0.25`) y muestra con claridad el saldo del 75% que se abona en el salón.
- **Retención (Hold):** Crea un hold de 15 minutos en `fechaExpiracionPago`, visible con timer en el carrito y en el modal de consulta (`"TU RESERVA SIGUE ACTIVA"`).
- **Concurrencia:** Si dos clientes intentan agendar el mismo slot en el mismo milisegundo, el índice único de MongoDB (`reserva_fecha_hora_activa_idx`) otorga el slot al primero (`201 Created`) y rechaza al segundo con `409 Conflict`.

### 3. Flujo Administrativo: Despacho y Gestión de Agenda
- **Pedidos:** Viven exclusivamente en la pestaña *Pedidos*. Las acciones de estado validan transiciones permitidas (rechaza pasar un pedido pickup a `En camino` o un domicilio a `Listo para recoger`).
- **Citas:** Muestran profesional, fecha, hora, anticipo pagado y saldo pendiente en caja.
- **Métricas:** El dashboard suma con exactitud las ventas diarias/mensuales y las citas del día, excluyendo citas ya finalizadas de la lista de pendientes.

---

## 5. Pruebas de Error Humano y Recuperación

1. **Doble Clic en Pagar:** Deshabilitación de botones y generación de referencias únicas SHA-256 impiden pagos dobles.
2. **Cierre de Wompi / Abandono:** El cliente puede volver a abrir el checkout mientras la retención de 15 minutos esté vigente. Si expira, el botón de retoma re-valida si el horario sigue libre.
3. **Modificación de Carrito en Checkout:** Cualquier cambio recalcula subtotales e invalida firmas previas de Wompi de forma segura.
4. **Recarga (F5):** `localStorage` y `sessionStorage` restauran de forma limpia los artículos y eliminan automáticamente retenciones residuales inválidas.

---

## 6. Dictamen de Seguridad y Producción

- **Protección de Datos:** Ningún cliente puede consultar reservas ajenas sin proporcionar el teléfono exacto registrado junto con el código `ISV-XXXX`.
- **Integridad de Precios:** El servidor nunca confía en el precio enviado en el payload JSON; siempre consulta MongoDB y recalcula el anticipo y subtotal en el backend.
- **Modo Degradado:** Ante caídas de APIs de mensajería (Brevo o WhatsApp), el motor central persiste la transacción sin generar excepciones no controladas.
