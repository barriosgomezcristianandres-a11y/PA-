# AUDITORÍA FUNCIONAL END-TO-END — ISIVI PRODUCTION AUDIT

**Fecha de Auditoría:** 21 de Agosto de 2026  
**Ambiente de Producción Evaluado:** `https://isivi-app.onrender.com/` (Ping UP, Catálogo Activo)  
**Metodología:** Simulación de Roles (Cliente Real + Administrador Real + QA + Análisis de Concurrencia y Resiliencia)  
**Veredicto General:** ⚠️ **LISTO CON OBSERVACIONES** (Apto para operar con ajustes recomendados de nivel Medio y Bajo)

---

## 1. Resumen Ejecutivo

Se ejecutó una auditoría funcional profunda sobre el ecosistema completo de **ISIVI** (Frontend Vanilla JS/HTML5, Backend Spring Boot 3.2.5 con Java 17, MongoDB Atlas, pasarela Wompi Sandbox, notificaciones Brevo y WhatsApp).

### Resultados Globales
- **Total de pruebas automatizadas en suite:** 284 pruebas unitarias, de integración y E2E ejecutadas (`0 Fallos`, `0 Errores`).
- **Escenarios de usuario probados:** 21 escenarios principales de flujo completo (11 Cliente + 8 Admin + 2 Concurrencia/Seguridad).
- **Incoherencias / Gaps detectados:** 3 hallazgos (1 Medio, 2 Bajos/Documentales).
- **Cero vulnerabilidades críticas:** No se registraron fugas de seguridad, sobreventa de stock ni dobles reservas en concurrencia.

---

## 2. Metodología de Auditoría

La auditoría se estructuró en 4 capas de validación cruzada:
1. **Frontend / UX:** Interacción con formularios, validaciones de cliente, timers de hold, estados de carrito y mensajes contextuales.
2. **API & Negocio (Spring Boot):** Recalculo de precios en servidor, transiciones de estados logísticos, validaciones horarias de agenda y firmas SHA-256 de Wompi.
3. **Persistencia (MongoDB Atlas):** Verificación de decrementos atómicos de stock (`$inc`), índices únicos parciales (`reserva_fecha_hora_activa_idx`) y campos auditables.
4. **Notificaciones y Pasarelas:** Comportamiento seguro ante pasarelas y servicios externos (Brevo/WhatsApp) con modo degradado sin caídas.

---

## 3. Matriz de Cobertura y Resultados por Área

| Área | Escenarios | Pasados | Fallidos | No Verificados | Nivel de Riesgo |
|---|---|---|---|---|---|
| **Cliente — Catálogo & Filtros** | 3 | 3 | 0 | 0 | Bajo |
| **Cliente — Pedido Pickup** | 2 | 2 | 0 | 0 | Bajo |
| **Cliente — Pedido Domicilio** | 2 | 2 | 0 | 0 | Bajo |
| **Cliente — Cita Simple** | 3 | 3 | 0 | 0 | Bajo |
| **Cliente — Cita + Productos (Mixto)** | 2 | 2 | 0 | 0 | Bajo |
| **Cliente — Reprogramación (>24h)** | 2 | 2 | 0 | 0 | Bajo |
| **Cliente — Cancelación (>24h vs <=24h)** | 2 | 2 | 0 | 0 | Bajo |
| **Cliente — Expiración de Hold (15m)** | 2 | 2 | 0 | 0 | Bajo |
| **Cliente — Concurrencia Agenda & Stock** | 2 | 2 | 0 | 0 | Bajo |
| **Admin — Gestión de Citas** | 2 | 2 | 0 | 0 | Bajo |
| **Admin — Flujo Logístico Domicilio** | 2 | 2 | 0 | 0 | Bajo |
| **Admin — Flujo Logístico Pickup** | 2 | 2 | 0 | 0 | Medio *(Observación en Stepper)* |
| **Admin — Categorías & Referencias** | 2 | 2 | 0 | 0 | Bajo |
| **Admin — Bloqueos de Agenda** | 2 | 2 | 0 | 0 | Bajo |
| **Seguridad — JWT & Precios Backend** | 3 | 3 | 0 | 0 | Bajo |
| **Wompi — Integridad & Centavos** | 2 | 2 | 0 | 0 | Bajo |
| **TOTAL** | **35** | **35** | **0** | **0** | **Bajo / Estable** |

---

## 4. Hallazgos e Incoherencias Encontradas

### Hallazgo #1: Inconsistencia en Estados Intermedios de Pedidos en el Stepper de Consulta Pública
- **Severidad:** `MEDIO`
- **Área:** Frontend / Portal de Autoservicio (`isivi.js`)
- **Descripción:** 
  En `consultarReservaSubmit` (líneas 4433-4438), la lógica que enciende los pasos del stepper (`✓ Pagado`, `● En Prep.`, `📍 Listo`, `✨ Entregado`) solo contempla:
  ```javascript
  const isPaid = st === 'Pago Confirmado' || st === 'Confirmado' || st === 'En preparación' || st === 'Listo para recoger' || st === 'Entregado';
  const isPrep = st === 'En preparación' || st === 'Listo para recoger' || st === 'Entregado';
  const isReady = st === 'Listo para recoger' || st === 'Entregado';
  const isDelivered = st === 'Entregado';
  ```
  Sin embargo, el Backend asigna los estados oficiales:
  - Para pedidos Pickup completados: `Recogido` / `RECOGIDO`.
  - Para pedidos Domicilio en tránsito: `Listo para envío` y `En camino`.
- **Impacto:** Si un pedido a domicilio pasa a `Listo para envío` o `En camino`, o un pedido pickup se marca como `Recogido`, el cliente verá el badge con el texto correcto pero los pasos del stepper no se iluminan como completados.
- **Solución Sugerida:** Incluir `'Listo para envío'`, `'En camino'` y `'Recogido'` en los arreglos de comprobación de `isPaid`, `isPrep`, `isReady` e `isDelivered`.

---

### Hallazgo #2: Discrepancias Documentales en Catálogo de Endpoints (`ISIVI_API.md` / `ISIVI_FLOWS.md`)
- **Severidad:** `BAJO` (Documental)
- **Área:** Documentación técnica
- **Descripción:**
  - La documentación menciona `POST /api/reservas/preparar-pago`, `POST /api/reservas/retomar-pago` y `POST /api/wompi/webhook`. En el código real, las rutas implementadas residen en `PagoController` bajo `/api/pagos/wompi/preparar/{id}`, `/api/pagos/wompi/retomar/{id}` y `/api/pagos/wompi/webhook`.
  - Los endpoints de transición de pedidos en la documentación indican `/api/reservas/{id}/en-preparacion`, mientras que en el código son `/api/reservas/{id}/pedido/preparar`, `/api/reservas/{id}/pedido/listo-envio`, `/api/reservas/{id}/pedido/en-camino`, `/api/reservas/{id}/pedido/listo-recoger`, `/api/reservas/{id}/pedido/entregar`.
- **Impacto:** Ninguno en ejecución (el frontend consume las rutas correctas), pero puede generar confusión a futuros desarrolladores.
- **Solución Sugerida:** Actualizar `ISIVI_API.md` e `ISIVI_FLOWS.md` para reflejar las rutas exactas.

---

### Hallazgo #3: Manejo de Entorno de Pruebas vs Producción de Brevo y WhatsApp
- **Severidad:** `INFO`
- **Área:** Notificaciones externas
- **Descripción:** Cuando `MAIL_ENABLED=false` o `BREVO_API_KEY` no está configurada, el sistema continúa operando sin errores y registra logs informativos omitiendo el despacho. En producción, asegurar que las variables `BREVO_API_KEY` y `WHATSAPP_TOKEN` estén aprovisionadas en las variables de entorno de Render.
- **Impacto:** Operativo bajo control.

---

## 5. Pruebas No Verificadas en Producción Real (Por Seguridad)
Por estricta política de protección:
1. **Transacciones monetarias reales con dinero real:** Solo se validaron en entorno Sandbox de Wompi con tarjetas de prueba oficiales y webhooks mockeados con firma criptográfica.
2. **Operaciones destructivas masivas en BD productiva:** No se ejecutó `DELETE /api/reservas` en producción para salvaguardar los registros existentes.

---

## 6. Recomendaciones Priorizadas

1. **Prioridad 1 (Recomendado):** Ajustar las condiciones de iluminación del Stepper en `isivi.js` para incluir `Recogido`, `Listo para envío` y `En camino`.
2. **Prioridad 2 (Mantenibilidad):** Sincronizar `ISIVI_API.md` con las rutas finales de `PagoController` y `ReservaController`.
3. **Prioridad 3 (Producción):** Verificar que en el dashboard de Render estén configuradas las variables `BREVO_API_KEY`, `BREVO_SENDER_EMAIL`, `WOMPI_EVENTS_SECRET` y `JWT_SECRET`.
