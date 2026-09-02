# ARQUITECTURA TÉCNICA Y MAPA DE COMPONENTES — ISIVI

## 1. Visión General de la Arquitectura
ISIVI implementa una arquitectura desacoplada en capas (Layered Architecture) basada en **Spring Boot 3.2.5** sobre **Java 17 LTS**, persistencia no relacional con **MongoDB Atlas** y un frontend **Single Page Application (SPA)** de alto rendimiento en Vanilla HTML5/CSS3/JavaScript ES6+.

```
┌─────────────────────────────────────────────────────────────┐
│                 FRONTEND SPA (index.html)                    │
│    (isivi.js, isivi.css, Wompi Checkout Widget JS)          │
└──────────────────────────────┬──────────────────────────────┘
                               │ HTTP / HTTPS (REST JSON)
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 SPRING SECURITY & FILTERS                   │
│   CorsConfigurationSource ──> JwtAuthenticationFilter       │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                     REST CONTROLLERS                        │
│ AuthController, ReservaController, AgendaController,        │
│ DashboardController, ProductoController, ServicioController,│
│ KitController, BannerController, WompiWebhookController     │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                     SERVICE LAYER                           │
│ ReservaService, WompiService, EmailNotificationService,     │
│ WhatsAppNotificationService, JwtService                     │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 SPRING DATA MONGODB REPOSITORIES            │
│ ReservaRepository, ProductoRepository, ServicioRepository,  │
│ KitRepository, ConfiguracionAgendaRepository, etc.          │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    MONGODB ATLAS DATABASE                   │
│             (Replica Set AWS us-east-1)                     │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Componentes por Capa

### 2.1. Capa de Controladores (Presentation Layer)
- **`AuthController`**: `POST /api/auth/login`. Autentica credenciales administrativas y emite tokens JWT.
- **`PublicCatalogController`**: `GET /api/public/catalogo`. Agrupa servicios, productos y kits en una sola respuesta liviana.
- **`PublicConfigController`**: `GET /api/public/config`. Provee llaves públicas de Wompi e información general del negocio.
- **`AgendaController`**: `GET /api/agenda/disponibilidad`, `GET/PUT /api/agenda/configuracion`, `POST/DELETE /api/agenda/bloqueos/**`, `/bloqueos-recurrentes/**`, `/excepciones/**`.
- **`ReservaController`**: CRUD, cálculo de totales, retención, cancelación, reprogramación, consulta unificada y ciclo de vida de pedidos y citas.
- **`DashboardController`**: `GET /api/dashboard/resumen`. Calcula métricas financieras, citas del día, desglose operativo y conversión.
- **`ProductoController`**: CRUD de productos y actualización de stock/variantes.
- **`ServicioController`**: CRUD de servicios capilares y asignación de especialistas.
- **`KitController`**: CRUD de kits capilares promocionales.
- **`CategoriaController` & `CategoriaServicioController`**: Gestión de taxonomías de productos y servicios.
- **`BannerController`**: CRUD de banners del carrusel principal.
- **`WompiWebhookController`**: `POST /api/wompi/webhook`. Receptor de eventos de Wompi con validación criptográfica de checksum.

### 2.2. Capa de Servicios (Business Logic Layer)
- **`ReservaService`**: Validador maestro de catálogo, retención de 15 minutos, descuento y liberación atómica de stock, reglas de 24h para cancelaciones y gestión de estados canónicos.
- **`WompiService`**: Constructor de firmas SHA-256 de integridad, conector con API REST de Wompi y auto-recuperación de transacciones.
- **`EmailNotificationService`**: Cliente HTTP de Brevo API v3 para despacho de plantillas HTML transaccionales.
- **`WhatsAppNotificationService`**: Generador de textos estructurados y enlaces `wa.me`.
- **`JwtService`**: Generación, validación y extracción de claims en tokens JWT HMAC-SHA256.

### 2.3. Capa de Acceso a Datos (Persistence Layer)
- **`ReservaRepository`**: Búsquedas por código, teléfono, fecha, estado, referencia Wompi y filtros compuestos de historial.
- **`ProductoRepository`**: Búsqueda por categoría, destacados y activos.
- **`ServicioRepository`**: Búsqueda por categoría y servicios activos.
- **`KitRepository`**: Búsqueda de kits activos.
- **`ConfiguracionAgendaRepository`**: Almacenamiento singleton de los horarios base.
- **`BloqueoHorarioRepository`, `BloqueoRecurrenteRepository`, `ExcepcionAgendaRepository`**: Consultas de bloqueo temporal para el motor de disponibilidad.
- **`AdministradorRepository`**: Búsqueda de usuarios administrativos por `username`.
- **`BannerRepository`**: Búsqueda de banners activos ordenados por `orden ASC`.

---

## 3. Índices de Base de Datos y Rendimiento
1. **Índice Único Compuesto Parcial en `reservas`:**
   - Campos: `{ "fechaCita": 1, "horaCita": 1 }`
   - Nombre: `reserva_fecha_hora_activa_idx`
   - Condición: `{ "archivada": { "$ne": true }, "estado": { "$in": ["Confirmado", "Pendiente Comprobante", "Pendiente Pago", "Pendiente Reprogramación", "En curso"] } }`
   - Propósito: Garantizar a nivel de motor que dos citas nunca puedan solaparse en el mismo turno exacto.
2. **Índice Único en `reservas.codigoReserva`:**
   - Búsquedas ultrarrápidas de clientes en la consulta unificada.
3. **Índice en `reservas.referenciaWompi`:**
   - Procesamiento instantáneo de webhooks entrantes de Wompi.
4. **Índice Único en `administradores.username`:**
   - Unicidad de usuarios administrativos.
