# CATÁLOGO DE LA API REST — ISIVI

## Especificación General
- **Formato:** JSON (Content-Type: `application/json;charset=UTF-8`)
- **Autenticación Administrativa:** Header `Authorization: Bearer <JWT_TOKEN>`
- **Zona Horaria de Fechas:** `America/Bogota` (ISO-8601 `YYYY-MM-DD` para fechas)

---

## Tabla de Endpoints

| Método | Endpoint | Acceso | Request Body / Query Params | Response Code y Body | Controller y Service | Descripción |
|---|---|---|---|---|---|---|
| `POST` | `/api/auth/login` | Público | `{ "username": "...", "password": "..." }` | `200 OK` `{ "token": "...", "username": "...", "rol": "ROLE_ADMIN" }` | `AuthController` -> `JwtService` | Inicia sesión y emite token JWT |
| `GET` | `/api/pagos/wompi/config` | Público | Ninguno | `200 OK` `{ "wompiPublicKey": "...", "wompiAmbiente": "sandbox", "info": {...} }` | `PagoController` | Provee credenciales públicas de Wompi al frontend |
| `GET` | `/api/agenda/disponibilidad` | Público | `?fecha=YYYY-MM-DD` | `200 OK` `["08:00 AM", "09:30 AM", ...]` | `AgendaController` / `ReservaController` | Retorna los horarios disponibles calculados en tiempo real |
| `POST` | `/api/reservas` | Público | `Reserva` (datos cliente, items, fecha/hora o entrega) | `201 Created` `Reserva` | `ReservaController` -> `ReservaService`, `WASvc` | Crea una reserva o pedido, valida stock y bloquea agenda |
| `POST` | `/api/pagos/wompi/preparar/{id}` | Público | `{ "codigoReserva": "...", "telefono": "..." }` (opcional) | `200 OK` `{ "publicKey": "...", "amountInCents": 5000000, "reference": "...", "signature": {...} }` | `PagoController` -> `WompiService`, `ReservaSvc` | Genera la firma de integridad SHA-256 de Wompi y checkout |
| `GET` | `/api/pagos/wompi/checkout/{id}` | Público | Ninguno | `200 OK` `{ "publicKey": "...", "amountInCents": ..., "reference": "...", "signature": {...} }` | `PagoController` -> `WompiService` | Prepara checkout data de Wompi por ID |
| `POST` | `/api/pagos/wompi/retomar/{id}` | Público | `{ "codigoReserva": "...", "telefono": "..." }` | `200 OK` `{ "publicKey": "...", "amountInCents": ..., "reference": "...", "signature": {...} }` | `PagoController` -> `WompiService`, `ReservaSvc` | Reactiva un pago expirado re-validando stock y agenda |
| `GET` | `/api/reservas/consultar` | Público | `?query=ISV-XXXX` o `?telefono=...` | `200 OK` `Reserva` | `ReservaController` -> `ReservaService` | Consulta unificada de citas y pedidos para clientes |
| `POST` | `/api/reservas/consultar` | Público | `{ "codigoReserva": "...", "telefono": "..." }` | `200 OK` `Reserva` | `ReservaController` -> `ReservaService` | Consulta unificada vía POST con credenciales |
| `POST` | `/api/reservas/{id}/liberar-retencion` | Público | `{ "codigoReserva": "...", "telefono": "..." }` | `200 OK` `{ "mensaje": "..." }` | `ReservaController` -> `ReservaService` | Libera retención provisional de agenda de cliente |
| `PATCH`| `/api/reservas/{id}/reprogramar` | Público | `GestionReservaRequest` (código, teléfono, fechaCita, horaCita) | `200 OK` `Reserva` | `ReservaController` -> `ReservaService` | Reprograma cita validando antelación de 24h |
| `PATCH`| `/api/reservas/{id}/cancelar` | Público | `GestionReservaRequest` (código, teléfono, motivoCancelacion) | `200 OK` `Reserva` | `ReservaController` -> `ReservaService`, `EmailSvc` | Cancela directamente (>24h) o rechaza auto-cancelación (<=24h) |
| `POST` | `/api/pagos/wompi/webhook` | Público | Payload JSON Wompi + Header `x-event-checksum` | `200 OK` `{ "status": "OK" }` | `PagoController` -> `WompiService`, `ReservaSvc` | Procesa eventos asíncronos de pago de Wompi |
| `GET` | `/api/reservas` | **Admin** | `?fecha=YYYY-MM-DD&historial=true/false&tipo=citas/pedidos/todos` | `200 OK` `List<Reserva>` | `ReservaController` -> `ReservaService` | Listado y filtros de reservas y pedidos en admin |
| `GET` | `/api/reservas/{id}` | **Admin** | Ninguno | `200 OK` `Reserva` | `ReservaController` -> `ReservaService` | Detalle completo de una reserva o pedido |
| `PATCH`| `/api/reservas/{id}/aprobar` | **Admin** | Ninguno | `200 OK` `Reserva` | `ReservaController` -> `ReservaService` | Aprueba pago por transferencia bancaria |
| `PATCH`| `/api/reservas/{id}/denegar` | **Admin** | `{ "motivo": "..." }` | `200 OK` `Reserva` | `ReservaController` -> `ReservaService` | Deniega comprobante y libera stock/agenda |
| `PATCH`| `/api/reservas/{id}/admin-reprogramar` | **Admin** | `{ "fechaCita": "...", "horaCita": "..." }` | `200 OK` `Reserva` | `ReservaController` -> `ReservaService` | Reprogramación administrativa de cita |
| `PATCH`| `/api/reservas/{id}/aprobar-cancelacion` | **Admin** | Ninguno | `200 OK` `Reserva` | `ReservaController` -> `ReservaService` | Aprueba solicitud de cancelación tardía |
| `PATCH`| `/api/reservas/{id}/rechazar-cancelacion` | **Admin** | `{ "motivo": "..." }` | `200 OK` `Reserva` | `ReservaController` -> `ReservaService` | Rechaza solicitud de cancelación tardía |
| `PATCH`| `/api/reservas/{id}/admin-cancelar` | **Admin** | `{ "motivo": "..." }` | `200 OK` `Reserva` | `ReservaController` -> `ReservaService` | Cancela cita o pedido desde administración |
| `PATCH`| `/api/reservas/{id}/marcar-cancelacion-vista` | **Admin** | Ninguno | `200 OK` `Reserva` | `ReservaController` -> `ReservaService` | Marca notificación de cancelación como vista |
| `PATCH`| `/api/reservas/{id}/pedido/preparar` | **Admin** | Ninguno | `200 OK` `Reserva` | `ReservaController` -> `ReservaService` | Pasa pedido a estado "En preparación" |
| `PATCH`| `/api/reservas/{id}/pedido/listo-envio` | **Admin** | Ninguno | `200 OK` `Reserva` | `ReservaController` -> `ReservaService`, `WASvc` | Marca pedido domicilio como "Listo para envío" |
| `PATCH`| `/api/reservas/{id}/pedido/en-camino` | **Admin** | Ninguno | `200 OK` `Reserva` | `ReservaController` -> `ReservaService`, `WASvc` | Marca pedido domicilio como "En camino" |
| `PATCH`| `/api/reservas/{id}/pedido/listo-recoger` | **Admin** | Ninguno | `200 OK` `Reserva` | `ReservaController` -> `ReservaService`, `EmailSvc` | Notifica al cliente pedido listo para pickup |
| `PATCH`| `/api/reservas/{id}/pedido/entregar` | **Admin** | Ninguno | `200 OK` `Reserva` | `ReservaController` -> `ReservaService` | Marca pedido como "Entregado" (domicilio) o "Recogido" (pickup) |
| `DELETE`| `/api/reservas/{id}` | **Admin** | Ninguno | `204 No Content` | `ReservaController` -> `ReservaService` | Elimina una reserva o pedido y libera inventario |
| `DELETE`| `/api/reservas` | **Admin** | Ninguno | `204 No Content` | `ReservaController` -> `ReservaService` | Elimina todas las reservas y pedidos |
| `GET` | `/api/dashboard/resumen` | **Admin** | Ninguno | `200 OK` `{ "citasHoy": 4, "citasHoyDesglose": {...}, "ventasHoy": ..., "ventasMes": ..., "proximasCitas": [...] }` | `DashboardController` -> `ReservaService` | Métricas y KPIs operativos y financieros |
| `GET` | `/api/dashboard/solicitudes` | **Admin** | Ninguno | `200 OK` `{ "total": ..., "comprobantesPendientes": [...], "cancelacionesPendientes": [...] }` | `DashboardController` -> `ReservaService` | Solicitudes que requieren acción administrativa |
| `GET` | `/api/productos` | Público / Admin | Ninguno | `200 OK` `List<Producto>` | `ProductoController` | Listado de productos de catálogo |
| `POST` | `/api/productos` | **Admin** | `Producto` | `201 Created` `Producto` | `ProductoController` | Crea un producto físico con variantes |
| `PUT` | `/api/productos/{id}` | **Admin** | `Producto` | `200 OK` `Producto` | `ProductoController` | Actualiza un producto y sus variantes |
| `DELETE`| `/api/productos/{id}` | **Admin** | Ninguno | `204 No Content` | `ProductoController` | Elimina un producto |
| `GET` | `/api/categorias-producto` | Público / Admin | `?soloActivas=true/false` | `200 OK` `List<CategoriaProducto>` | `CategoriaProductoController` | Listado de categorías de producto |
| `POST` | `/api/categorias-producto` | **Admin** | `CategoriaProducto` | `201 Created` `CategoriaProducto` | `CategoriaProductoController` | Crea categoría de producto |
| `PUT` | `/api/categorias-producto/{id}` | **Admin** | `CategoriaProducto` | `200 OK` `CategoriaProducto` | `CategoriaProductoController` | Actualiza categoría de producto |
| `DELETE`| `/api/categorias-producto/{id}` | **Admin** | Ninguno | `204 No Content` / `409 Conflict` | `CategoriaProductoController` | Elimina categoría de producto (bloquea si tiene productos) |
| `GET` | `/api/servicios` | Público / Admin | `?categoria=...` | `200 OK` `List<Servicio>` | `ServicioController` | Listado de servicios capilares |
| `POST` | `/api/servicios` | **Admin** | `Servicio` | `201 Created` `Servicio` | `ServicioController` | Crea un servicio capilar |
| `PUT` | `/api/servicios/{id}` | **Admin** | `Servicio` | `200 OK` `Servicio` | `ServicioController` | Actualiza un servicio |
| `DELETE`| `/api/servicios/{id}` | **Admin** | Ninguno | `204 No Content` | `ServicioController` | Elimina un servicio |
| `GET` | `/api/categorias-servicio` | Público / Admin | Ninguno | `200 OK` `List<CategoriaServicio>` | `CategoriaServicioController` | Listado de categorías de servicio |
| `POST` | `/api/categorias-servicio` | **Admin** | `CategoriaServicio` | `201 Created` `CategoriaServicio` | `CategoriaServicioController` | Crea categoría de servicio |
| `DELETE`| `/api/categorias-servicio/{id}` | **Admin** | Ninguno | `204 No Content` | `CategoriaServicioController` | Elimina categoría de servicio |
| `GET` | `/api/kits` | Público / Admin | Ninguno | `200 OK` `List<Kit>` | `KitController` | Listado de kits capilares |
| `POST` | `/api/kits` | **Admin** | `Kit` | `201 Created` `Kit` | `KitController` | Crea un kit promocional |
| `PUT` | `/api/kits/{id}` | **Admin** | `Kit` | `200 OK` `Kit` | `KitController` | Actualiza un kit |
| `DELETE`| `/api/kits/{id}` | **Admin** | Ninguno | `204 No Content` | `KitController` | Elimina un kit |
| `GET` | `/api/banners` | Público / Admin | Ninguno | `200 OK` `List<Banner>` | `BannerController` | Listado de banners del carrusel |
| `POST` | `/api/banners` | **Admin** | `Banner` | `201 Created` `Banner` | `BannerController` | Agrega un nuevo banner |
| `DELETE`| `/api/banners/{id}` | **Admin** | Ninguno | `204 No Content` | `BannerController` | Elimina un banner |
| `GET` | `/api/agenda/configuracion` | **Admin** | Ninguno | `200 OK` `ConfiguracionAgenda` | `AgendaController` | Consulta horario base del salón |
| `PUT` | `/api/agenda/configuracion` | **Admin** | `ConfiguracionAgenda` | `200 OK` `ConfiguracionAgenda` | `AgendaController` | Modifica horario base del salón |
| `POST` | `/api/agenda/bloqueos` | **Admin** | `BloqueoHorario` | `200 OK` `BloqueoHorario` | `AgendaController` | Bloquea un horario puntual |
| `DELETE`| `/api/agenda/bloqueos/{id}` | **Admin** | Ninguno | `204 No Content` | `AgendaController` | Elimina un bloqueo puntual |
| `POST` | `/api/agenda/bloqueos-recurrentes` | **Admin** | `BloqueoRecurrente` | `200 OK` `BloqueoRecurrente` | `AgendaController` | Bloquea un horario semanal recurrente |
| `DELETE`| `/api/agenda/bloqueos-recurrentes/{id}` | **Admin** | Ninguno | `204 No Content` | `AgendaController` | Elimina un bloqueo recurrente |
| `POST` | `/api/agenda/excepciones` | **Admin** | `ExcepcionAgenda` | `200 OK` `ExcepcionAgenda` | `AgendaController` | Agrega día festivo / cierre especial |
| `DELETE`| `/api/agenda/excepciones/{id}` | **Admin** | Ninguno | `204 No Content` | `AgendaController` | Elimina excepción / festivo |
