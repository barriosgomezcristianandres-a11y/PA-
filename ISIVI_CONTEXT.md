# DOCUMENTO DE CONTEXTO TÉCNICO EXHAUSTIVO — PROYECTO ISIVI
**Versión:** 1.0.0 (Producción / Release Estable)  
**URL de Despliegue Oficial:** `https://isivi-app.onrender.com/`  
**Propósito:** Transferencia integral de conocimiento técnico, arquitectónico y operativo para agentes de IA e ingenieros de software.  
**Restricción Operativa:** Documento de referencia técnica estricta basado exclusivamente en el código fuente, pruebas automatizadas y configuración del repositorio.

---

## ÍNDICE GENERAL
1. [Resumen Ejecutivo](#1-resumen-ejecutivo)
2. [Arquitectura del Sistema](#2-arquitectura-del-sistema)
3. [Estructura del Proyecto y Árbol de Archivos](#3-estructura-del-proyecto-y-árbol-de-archivos)
4. [Modelo de Datos y Esquemas MongoDB](#4-modelo-de-datos-y-esquemas-mongodb)
5. [Catálogo Completo de la API REST](#5-catálogo-completo-de-la-api-rest)
6. [Capa de Servicios y Lógica de Negocio](#6-capa-de-servicios-y-lógica-de-negocio)
7. [Ciclo de Vida de Reservas, Pedidos y Compras Mixtas](#7-ciclo-de-vida-de-reservas-pedidos-y-compras-mixtas)
8. [Gestión de Inventario Atómico y Concurrencia](#8-gestión-de-inventario-atómico-y-concurrencia)
9. [Motor de Agenda, Horarios y Bloqueos](#9-motor-de-agenda-horarios-y-bloqueos)
10. [Pasarela de Pagos Wompi (Checkout, Webhooks y Recuperación)](#10-pasarela-de-pagos-wompi-checkout-webhooks-y-recuperación)
11. [Seguridad, Autenticación y Autorización](#11-seguridad-autenticación-y-autorización)
12. [Frontend (Arquitectura SPA Vanilla, Estado y Vistas)](#12-frontend-arquitectura-spa-vanilla-estado-y-vistas)
13. [Panel Administrativo y Operaciones de Salón](#13-panel-administrativo-y-operaciones-de-salón)
14. [Sistema de Notificaciones (Brevo API y WhatsApp)](#14-sistema-de-notificaciones-brevo-api-y-whatsapp)
15. [Estrategia de Pruebas y Aseguramiento de Calidad](#15-estrategia-de-pruebas-y-aseguramiento-de-calidad)
16. [Variables de Entorno y Configuración](#16-variables-de-entorno-y-configuración)
17. [Despliegue e Infraestructura en Render](#17-despliegue-e-infraestructura-en-render)
18. [Catálogo Exhaustivo de Reglas de Negocio](#18-catálogo-exhaustivo-de-reglas-de-negocio)
19. [Grafo de Dependencias entre Componentes](#19-grafo-de-dependencias-entre-componentes)
20. [Flujos Operativos Paso a Paso](#20-flujos-operativos-paso-a-paso)
21. [Máquinas de Estados y Transiciones Canónicas](#21-máquinas-de-estados-y-transiciones-canónicas)
22. [Riesgos Conocidos y Casos Borde Críticos](#22-riesgos-conocidos-y-casos-borde-críticos)
23. [Auditoría de Discrepancias (Código vs Documentación Previa)](#23-auditoría-de-discrepancias-código-vs-documentación-previa)
24. [Lo que un Nuevo Desarrollador / Agente DEBE Saber antes de Tocar ISIVI](#24-lo-que-un-nuevo-desarrollador--agente-debe-saber-antes-de-tocar-isivi)

---

## 1. RESUMEN EJECUTIVO
ISIVI es una plataforma web integral de comercio electrónico, autoservicio de reservas y gestión operativa para un salón de belleza y estética capilar de alto nivel en Colombia.
La aplicación unifica en una sola solución:
- **Catálogo público** interactivo de servicios, productos físicos con variantes y kits promocionales.
- **Motor de reservas en tiempo real** con selección de profesionales, cálculo de duración acumulativa, bloqueo de solapamientos y cálculo de anticipos (25%).
- **E-commerce capilar** con carrito de compras, selección de método de entrega (*Recogida en Tienda / Pickup* vs *Envío a Domicilio* con dirección obligatoria) y reserva temporal atómica de stock.
- **Pasarela de pagos Wompi** (Bancolombia) con firma de integridad SHA-256, checkout Widget/Redirect, webhooks asíncronos y retoma de transacciones pendientes/expiradas.
- **Notificaciones omnicanal**: Correos transaccionales HTML mediante API REST v3 de Brevo y enlaces directos estructurados para WhatsApp.
- **Portal de autoservicio para clientes** (*"Consultar reserva o pedido"*), permitiendo consultar el estado, descargar eventos a Google Calendar / Apple iCal, solicitar cancelaciones o reprogramar turnos bajo políticas horarias estrictas (límite de 24h).
- **Panel de control administrativo privado (SPA)** protegido por Spring Security y JWT, con métricas en tiempo real, agenda visual, gestión de pedidos con trazabilidad de estados (*Pago Confirmado -> En preparación -> Listo para recoger -> Entregado*), control de stock/variantes, gestión de bloqueos de agenda y diagnósticos de auditoría.

---

## 2. ARQUITECTURA DEL SISTEMA

```mermaid
graph TD
    Client[Navegador Web / Cliente Móvil]
    
    subgraph "Frontend Layer (Static Assets)"
        HTML[index.html (Semantic SPA Layout)]
        CSS[isivi.css (Design System, Tokens, Glassmorphism)]
        JS[isivi.js (State Machine, DOM Controllers, API Client)]
    end
    
    subgraph "Security & Filter Layer"
        CORS[CorsConfigurationSource]
        JWTFilter[JwtAuthenticationFilter]
        SecConfig[SecurityConfig (Stateless Session)]
    end
    
    subgraph "REST Controllers Layer"
        AuthController[AuthController]
        PublicController[PublicCatalogController / PublicConfigController]
        ReservaCtrl[ReservaController]
        AgendaCtrl[AgendaController]
        ProdCtrl[ProductoController]
        ServCtrl[ServicioController]
        KitCtrl[KitController]
        CatCtrl[CategoriaController / CategoriaServicioController]
        BannerCtrl[BannerController]
        DashCtrl[DashboardController]
        WompiWebhook[WompiWebhookController]
    end
    
    subgraph "Service Layer (Business Logic)"
        ReservaSvc[ReservaService]
        WompiSvc[WompiService]
        EmailSvc[EmailNotificationService (Brevo API v3)]
        WASvc[WhatsAppNotificationService]
        JwtSvc[JwtService]
    end
    
    subgraph "Persistence Layer (Spring Data MongoDB)"
        ReservaRepo[(ReservaRepository)]
        ProdRepo[(ProductoRepository)]
        ServRepo[(ServicioRepository)]
        KitRepo[(KitRepository)]
        CatProdRepo[(CategoriaProductoRepository)]
        CatServRepo[(CategoriaServicioRepository)]
        AdminRepo[(AdministradorRepository)]
        BannerRepo[(BannerRepository)]
        AgendaConfigRepo[(ConfiguracionAgendaRepository)]
        BloqHorarioRepo[(BloqueoHorarioRepository)]
        BloqRecurrenteRepo[(BloqueoRecurrenteRepository)]
        ExcepcionRepo[(ExcepcionAgendaRepository)]
    end
    
    subgraph "External Integrations"
        WompiGateway[Wompi Gateway (Bancolombia)]
        BrevoAPI[Brevo API v3 (Transactional Emails)]
        Atlas[(MongoDB Atlas Cloud)]
    end

    Client --> HTML
    HTML --> JS
    HTML --> CSS
    JS -->|HTTP Fetch + Bearer JWT| CORS
    CORS --> JWTFilter
    JWTFilter --> SecConfig
    SecConfig --> AuthController & ReservaCtrl & AgendaCtrl & ProdCtrl & ServCtrl & KitCtrl & CatCtrl & BannerCtrl & DashCtrl & WompiWebhook & PublicController
    
    ReservaCtrl --> ReservaSvc
    ReservaCtrl --> WompiSvc
    ReservaCtrl --> EmailSvc
    ReservaCtrl --> WASvc
    
    WompiWebhook --> WompiSvc
    WompiSvc --> ReservaSvc
    WompiSvc --> EmailSvc
    WompiSvc --> WASvc
    
    DashCtrl --> ReservaRepo & ProdRepo & KitRepo
    ReservaSvc --> ReservaRepo & ProdRepo & ServRepo & KitRepo
    
    ReservaRepo & ProdRepo & ServRepo & KitRepo & CatProdRepo & CatServRepo & AdminRepo & BannerRepo & AgendaConfigRepo & BloqHorarioRepo & BloqRecurrenteRepo & ExcepcionRepo --> Atlas
    WompiSvc -->|HTTPS SHA-256| WompiGateway
    EmailSvc -->|HTTPS api-key| BrevoAPI
```

---

## 3. ESTRUCTURA DEL PROYECTO Y ÁRBOL DE ARCHIVOS

```
terminado1/isivi-app/
├── pom.xml                                   # Configuración Maven, dependencias Spring Boot 3.2.5, JWT, Mongo
├── Dockerfile                                # Definición Docker para despliegue productivo en Render
├── README.md                                 # Documentación previa y setup
├── .gitignore                                # Exclusiones de Git (target, ide, logs)
└── src/
    ├── main/
    │   ├── java/com/isivi/app/
    │   │   ├── IsiviApplication.java          # Punto de entrada @SpringBootApplication, @EnableScheduling
    │   │   ├── config/
    │   │   │   ├── DataInitializer.java      # Seed idempotente de admin, catálogo inicial y banners
    │   │   │   ├── JacksonConfig.java        # Configuración de deserialización JSON
    │   │   │   └── MongoConfig.java          # Conexión, converter custom y creación de índices
    │   │   ├── controller/
    │   │   │   ├── AdministradorController.java  # CRUD de credenciales administrativas
    │   │   │   ├── AgendaController.java         # Motor de disponibilidad pública, bloqueos y excepciones
    │   │   │   ├── AuthController.java           # Login administrativo y emisión de tokens JWT
    │   │   │   ├── BannerController.java         # Gestión del carrusel de banners publicitarios
    │   │   │   ├── CategoriaController.java      # Categorías de productos
    │   │   │   ├── CategoriaServicioController.java # Categorías de servicios
    │   │   │   ├── DashboardController.java      # Métricas, agenda diaria, próximas citas, conversión
    │   │   │   ├── KitController.java            # Kits capilares
    │   │   │   ├── ProductoController.java       # Productos y gestión de stock / variantes
    │   │   │   ├── PublicCatalogController.java  # Endpoints públicos de catálogo optimizados
    │   │   │   ├── PublicConfigController.java   # Configuración pública (Wompi Public Key, salón info)
    │   │   │   ├── ReservaController.java        # Reservas, pedidos, historial, consulta unificada, estados
    │   │   │   ├── ServicioController.java       # Servicios y asignación de profesionales
    │   │   │   └── WompiWebhookController.java   # Receptor de eventos webhook de Wompi con checksum
    │   │   ├── dto/
    │   │   │   ├── AuthRequest.java              # DTO de login { username, password }
    │   │   │   ├── AuthResponse.java             # DTO de login { token, username, rol, expiraEn }
    │   │   │   ├── CancelacionAdminRequest.java  # DTO para cancelación desde admin con motivo
    │   │   │   ├── DenegacionRequest.java        # DTO para denegar cita con motivo
    │   │   │   ├── GestionReservaRequest.java    # DTO para consulta/reprogramación cliente { codigo, tel }
    │   │   │   ├── RechazoCancelacionRequest.java# DTO para rechazar solicitud de cancelación
    │   │   │   ├── SolicitudCancelacionRequest.java # DTO del cliente solicitando cancelación
    │   │   │   └── WompiTransaccionResponse.java # Mapeo de respuesta API Wompi
    │   │   ├── model/
    │   │   │   ├── Administrador.java            # Documento de administradores (BCrypt passwords)
    │   │   │   ├── Banner.java                   # Documento de banners visuales
    │   │   │   ├── BloqueoHorario.java           # Documento de bloqueo de horario puntual
    │   │   │   ├── BloqueoRecurrente.java        # Documento de bloqueo semanal repetitivo
    │   │   │   ├── CategoriaProducto.java        # Documento categoría de producto
    │   │   │   ├── CategoriaServicio.java        # Documento categoría de servicio
    │   │   │   ├── ConfiguracionAgenda.java      # Documento con horarios base (08:00 AM - 07:00 PM)
    │   │   │   ├── ExcepcionAgenda.java          # Documento de días festivos o cierres especiales
    │   │   │   ├── ItemReserva.java              # Subdocumento / DTO de línea de pedido/servicio
    │   │   │   ├── Kit.java                      # Documento de kit capilar con items incluidos
    │   │   │   ├── Producto.java                 # Documento de producto con stock y variantes
    │   │   │   ├── Reserva.java                  # Documento principal de Reserva / Pedido / Mixto
    │   │   │   ├── Servicio.java                 # Documento de servicio con duración y profesionales
    │   │   │   └── VarianteProducto.java         # Subdocumento con SKU, nombre y stock por variante
    │   │   ├── repository/                       # Interfaces Spring Data MongoRepository
    │   │   ├── security/
    │   │   │   ├── JwtAuthenticationFilter.java  # Filtro OncePerRequestFilter para validar Authorization header
    │   │   │   ├── JwtService.java               # Firma, decodificación y expiración de tokens HMAC-SHA256
    │   │   │   └── SecurityConfig.java           # Cadena de filtros de seguridad, CORS, BCrypt bean
    │   │   ├── service/
    │   │   │   ├── EmailNotificationService.java # Integración HTTP con Brevo API v3 (templates HTML)
    │   │   │   ├── ReservaService.java           # Reglas centrales de negocio, stock, retención y transiciones
    │   │   │   ├── WhatsAppNotificationService.java # Generador de mensajes y URLs directas a WhatsApp
    │   │   │   └── WompiService.java             # Generación de firmas SHA-256, consultas API Wompi
    │   │   └── util/
    │   │       └── HorarioUtil.java              # Conversión 12h/24h, cálculo de duraciones y overlaps
    │   └── resources/
    │       ├── application.properties            # Propiedades base, templates Brevo, configuración Wompi
    │       ├── application-local.properties      # Configuración de desarrollo local
    │       └── static/
    │           ├── index.html                    # Layout SPA completo (Navbar, Catálogos, Cart, Modales, Admin)
    │           ├── css/isivi.css                 # Sistema de diseño, Glassmorphism, animaciones, Responsive
    │           ├── js/isivi.js                   # Controlador frontal, renderers, fetchers, storage, Wompi JS
    │           └── images/                       # Logotipos, hero banner y tarjetas
    └── test/java/com/isivi/app/
        ├── AgendaManagementTest.java             # Pruebas de configuración de agenda y bloqueos
        ├── AnalyticsConversionTest.java          # Pruebas de KPIs de conversión y analítica
        ├── AppointmentCompletionWorkflowTest.java# Pruebas de transiciones de citas completadas
        ├── BrevoApiAuthTest.java                 # Pruebas de autenticación y headers Brevo
        ├── BrevoApiIntegrationTest.java          # Pruebas de envío e idempotencia de correos Brevo
        ├── BusinessE2ETest.java                  # Pruebas End-to-End de ciclo de vida completo
        ├── CancellationPolicyTest.java           # Pruebas de reglas de cancelación (>24h y <=24h)
        ├── DashboardAgendaRenderTest.java        # Pruebas de render de agenda en dashboard
        ├── DashboardControllerTest.java          # Pruebas de resumen métrico del dashboard
        ├── DeliveryAddressTest.java              # Pruebas de validación de dirección en domicilios
        ├── GlobalResilienceAuditTest.java        # Pruebas de resiliencia ante caídas externas
        ├── HistoryDiagnosisTest.java             # Pruebas de diagnósticos de integridad histórica
        ├── HistoryEndpointTest.java              # Pruebas de endpoints de historial
        ├── HorarioValidationTest.java            # Pruebas de validación y parsing de horarios
        ├── MixedCartValidationTest.java          # Pruebas de carritos mixtos (servicio + producto)
        ├── OrderWorkflowTest.java                # Pruebas del flujo de estados de pedidos
        ├── PersistenceAuditTest.java             # Pruebas de persistencia y no eliminación de documentos
        ├── ProductCategoryAndVariantTest.java    # Pruebas de variantes y categorías de productos
        ├── PublicCatalogRegressionTest.java      # Pruebas de regresión en catálogo público
        ├── PurchaseSuccessViewTest.java          # Pruebas de vistas de éxito diferenciadas
        ├── QuickDateFiltersTest.java             # Pruebas de filtros rápidos por fecha
        ├── RecurringAgendaBlocksTest.java        # Pruebas de bloqueos recurrentes
        ├── ReservaConcurrencyTest.java           # Pruebas de concurrencia y prevención de dobles reservas
        ├── ReservaExpirationAndConcurrencyTest.java # Pruebas de expiración y liberación de inventario
        ├── ReservationEmailAndLookupTest.java    # Pruebas de consulta unificada y correos
        ├── ReservationHistoryTest.java           # Pruebas de visibilidad en historial vs agenda
        ├── ReservationHoldReleaseTest.java       # Pruebas de retención y liberación de stock
        ├── ReservationPaymentResumeTest.java     # Pruebas de retoma de pago Wompi
        ├── ResumePaymentRegressionTest.java      # Pruebas de regresión en retoma de pagos
        ├── SecurityAuthTest.java                 # Pruebas de autenticación JWT y roles
        ├── SeoAndPublicEndpointsTest.java        # Pruebas de SEO, robots.txt y sitemap
        ├── UnifiedHistoryAndLookupTest.java      # Pruebas de consulta unificada cliente y admin
        ├── UpcomingAppointmentsTest.java         # Pruebas del widget de próximas citas
        ├── UxEnhancementsValidationTest.java     # Pruebas de validación de mejoras UX
        ├── WhatsAppPendingReminderTest.java      # Pruebas de recordatorios WhatsApp
        ├── WompiPaymentAuditTest.java            # Pruebas de auditoría de pagos Wompi
        ├── WompiRecoveryTest.java                # Pruebas de recuperación de transacciones Wompi
        └── WompiSandboxE2ETest.java              # Pruebas E2E de simulación Wompi Sandbox
```

---

## 4. MODELO DE DATOS Y ESQUEMAS MONGODB

### 4.1. Entidad: `Reserva` (`reservas`)
Representa de forma unificada: una Cita de Servicios, un Pedido de Productos/Kits, o una Compra Mixta.

| Campo | Tipo | Significado | Default / Regla |
|---|---|---|---|
| `id` | `String` | Identificador único generado por MongoDB | Auto |
| `codigoReserva` | `String` | Código alfanumérico público único (ej. `ISV-A1B2C3D4`) | Único |
| `nombreCliente` | `String` | Nombre completo del cliente | Obligatorio |
| `telefono` | `String` | Teléfono normalizado (solo dígitos) | Obligatorio, Búsqueda |
| `email` | `String` | Correo electrónico para notificaciones Brevo | Validado con regex |
| `servicioId` | `String` | ID del servicio principal (si es cita) | Opcional |
| `nombreServicio` | `String` | Nombre del servicio principal | Opcional |
| `profesional` | `String` | Especialista asignado (ej. "Isabella Gómez") | Opcional |
| `fechaCita` | `LocalDate` | Fecha agendada para el servicio | Índice parcial |
| `horaCita` | `String` | Horario de inicio en formato `hh:mm a` | Índice parcial |
| `items` | `List<String>` | Lista legible de items (ej. `["Balayage x1"]`) | Auto |
| `itemsInventario` | `List<ItemReserva>` | Lista estructurada de items para control de stock | Normalizado |
| `subtotal` | `Double` | Valor total de la transacción en COP | Obligatorio |
| `anticipo` | `Double` | Anticipo pagado o requerido (25% en citas, 100% pedidos) | Obligatorio |
| `saldo` | `Double` | Saldo pendiente a pagar en el salón (`subtotal - anticipo`) | Auto |
| `estado` | `String` | Estado operativo del registro | Ver máquina de estados |
| `estadoPago` | `String` | Estado financiero (`PENDIENTE`, `APROBADO`, `RECHAZADO`) | `PENDIENTE` |
| `medioPago` | `String` | `WOMPI`, `TRANSFERENCIA`, `EFECTIVO` | `WOMPI` |
| `referenciaWompi` | `String` | Referencia única enviada a Wompi | Búsqueda, Única |
| `idTransaccionWompi`| `String` | ID de transacción asignado por Wompi | Actualizado vía Webhook |
| `montoPagoCentavos`| `Long` | Monto procesado en centavos COP (ej. `5000000` = $50.000) | Validado con Wompi |
| `fechaRegistro` | `LocalDate` | Fecha de creación del registro | `LocalDate.now()` |
| `fechaPago` | `LocalDate` | Fecha en que el pago fue aprobado | Set en aprobación |
| `fechaExpiracionPago`| `Instant` | Marca de tiempo UTC para expiración de retención | +15 min |
| `inventarioReservado`| `Boolean` | Flag de concurrencia: indica si el stock físico fue descontado | Atómico |
| `tipoEntrega` | `String` | `pickup` (recogida en tienda) o `domicilio` | Default `pickup` |
| `direccionEntrega`| `String` | Dirección física de entrega (obligatoria si `esDomicilio()`) | Obligatoria en domicilio |
| `archivada` | `Boolean` | Flag que indica si el registro pasó a histórico permanente | Default `false` |
| `fechaArchivado` | `LocalDate` | Fecha en que fue archivado automáticamente | Opcional |
| `motivoCancelacion`| `String` | Motivo registrado al cancelar | Opcional |
| `motivoRechazoCancelacion`| `String` | Razón por la cual el admin rechazó cancelación | Opcional |
| `estadoPrevioCancelacion`| `String` | Estado antes de entrar en `Solicitud Cancelación` | Opcional |
| `notificacionCancelacionVista`| `Boolean` | Flag para alertas en panel de admin | Default `false` |
| `fechaEnPreparacion`| `Instant` | Timestamp de pase a preparación (pedidos) | Opcional |
| `fechaListoParaRecoger`| `Instant` | Timestamp de notificación listo para pickup | Opcional |
| `fechaEntregado` | `Instant` | Timestamp de entrega final del producto | Opcional |
| `atendidoPor` | `String` | Usuario admin que despachó/atendió el pedido | Opcional |

#### Métodos de Dominio en `Reserva`:
- `esCita()`: Retorna `true` si tiene `fechaCita` y `horaCita` válida, o si contiene algún `ItemReserva` de tipo `"servicio"`.
- `esPedido()`: Retorna `true` si contiene algún `ItemReserva` de tipo `"producto"` o `"kit"`, o si tiene `tipoEntrega` no nulo.
- `esPedidoPuro()`: Retorna `true` si `!esCita() && esPedido()`.
- `esCompraMixta()`: Retorna `true` si `esCita() && esPedido()`.
- `esDomicilio()`: Retorna `true` si `tipoEntrega` es `"domicilio"`.
- `esRecogida()`: Retorna `true` si no es domicilio (`"pickup"`).
- `bloqueaAgenda()`: Retorna `true` únicamente si `esCita()` y el estado es activo (`Confirmado`, `Pendiente Comprobante`, `Pendiente Pago`, `Pendiente Reprogramación`, `En curso`). Pedidos puros retornan siempre `false`.

### 4.2. Entidad: `Producto` (`productos`)
| Campo | Tipo | Significado |
|---|---|---|
| `id` | `String` | ID único MongoDB |
| `nombre` | `String` | Nombre comercial del producto |
| `descripcion` | `String` | Descripción detallada y modo de uso |
| `precio` | `Double` | Precio base en COP |
| `stock` | `Integer` | Stock general (usado si no tiene variantes) |
| `categoria` | `String` | Nombre de la categoría asociada |
| `imagenUrl` | `String` | URL de la imagen del producto |
| `destacado` | `Boolean` | Mostrar en carrusel destacado público |
| `activo` | `Boolean` | Visibilidad pública en catálogo |
| `variantes` | `List<VarianteProducto>` | Lista de variantes (ej. tonos, tamaños, presentaciones) |

### 4.3. Subdocumento: `VarianteProducto`
| Campo | Tipo | Significado |
|---|---|---|
| `sku` | `String` | Identificador único de variante |
| `nombre` | `String` | Nombre de la variante (ej. "Tono Rubio Cenizo 60ml") |
| `stock` | `Integer` | Cantidad física disponible para esta variante |
| `precioAjuste` | `Double` | Ajuste opcional sobre el precio base (+/- COP) |

### 4.4. Entidad: `Servicio` (`servicios`)
| Campo | Tipo | Significado |
|---|---|---|
| `id` | `String` | ID único MongoDB |
| `nombre` | `String` | Nombre del servicio (ej. "Balayage Orgánico") |
| `descripcion` | `String` | Detalle del procedimiento capilar |
| `precio` | `Double` | Valor total del servicio en COP |
| `duracionMinutos` | `Integer` | Tiempo de ocupación en agenda (default: 60 min) |
| `categoria` | `String` | Categoría del servicio |
| `profesionales` | `List<String>` | Lista de especialistas habilitados |
| `imagenUrl` | `String` | Imagen ilustrativa |
| `activo` | `Boolean` | Habilitado para reserva pública |

### 4.5. Entidad: `Kit` (`kits`)
| Campo | Tipo | Significado |
|---|---|---|
| `id` | `String` | ID único MongoDB |
| `nombre` | `String` | Nombre del kit promocional (ej. "Kit Reparación Total") |
| `descripcion` | `String` | Descripción y beneficios del conjunto |
| `precio` | `Double` | Precio especial del paquete en COP |
| `stock` | `Integer` | Cantidad física de kits armados disponibles |
| `imagenUrl` | `String` | Imagen promocional |
| `productosIncluidos`| `List<String>` | Nombres de productos que conforman el kit |
| `activo` | `Boolean` | Visibilidad en catálogo |

### 4.6. Entidades de Agenda y Configuración
- `ConfiguracionAgenda`: Horarios base del salón (`diasSemanaHabilitados`, `horaApertura`, `horaCierre`, `intervaloMinutos`).
- `BloqueoHorario`: Bloqueo puntual de una fecha y hora (`fecha`, `hora`, `motivo`).
- `BloqueoRecurrente`: Bloqueo semanal recurrente (`diaSemana` 1-7, `hora`, `motivo`).
- `ExcepcionAgenda`: Cierres completos por festivos o mantenimiento (`fecha`, `motivo`, `bloqueado`).
- `Administrador`: Credenciales del staff (`username`, `password` hasheado con BCrypt, `nombre`, `rol` = `ROLE_ADMIN`).
- `Banner`: Banners publicitarios en carrusel superior (`titulo`, `subtitulo`, `imagenUrl`, `orden`, `activo`, `enlace`).

---

## 5. CATÁLOGO COMPLETO DE LA API REST

| Método | Ruta | Acceso | Request Body / Params | Response | Controller | Service | Descripción |
|---|---|---|---|---|---|---|---|
| `POST` | `/api/auth/login` | **Público** | `AuthRequest` (`username`, `password`) | `AuthResponse` (`token`, `rol`) | `AuthController` | `JwtService` | Autenticación y generación de JWT |
| `GET` | `/api/public/config` | **Público** | Ninguno | Map (`wompiPublicKey`, `info`) | `PublicConfigController` | N/A | Parámetros públicos de Wompi y Salón |
| `GET` | `/api/public/catalogo` | **Público** | Ninguno | Map (`servicios`, `productos`, `kits`) | `PublicCatalogController` | N/A | Catálogo unificado y optimizado para el home |
| `GET` | `/api/agenda/disponibilidad` | **Público** | `?fecha=YYYY-MM-DD` | `List<String>` (horarios disponibles) | `AgendaController` | `ReservaService` | Cálculo en tiempo real de slots libres |
| `POST` | `/api/reservas` | **Público** | `Reserva` (datos cliente, items, fecha, entrega) | `Reserva` (201 Created) | `ReservaController` | `ReservaService`, `WASvc` | Creación de reserva / pedido con validaciones |
| `POST` | `/api/reservas/preparar-pago`| **Público** | `Map` (`id` o `codigoReserva`) | `Map` (checkout Wompi, firma SHA-256) | `ReservaController` | `WompiService`, `ReservaSvc` | Genera firma de integridad y monta checkout |
| `POST` | `/api/reservas/retomar-pago` | **Público** | `Map` (`codigoReserva`, `telefono`) | `Map` (checkout actualizado Wompi) | `ReservaController` | `WompiService`, `ReservaSvc` | Re-valida stock y reactiva pago expirado |
| `POST` | `/api/reservas/consultar` | **Público** | `GestionReservaRequest` (`codigo`, `tel`) | `Reserva` (200 OK) | `ReservaController` | `ReservaService` | Consulta unificada para clientes |
| `PATCH`| `/api/reservas/{id}/reprogramar` | **Público** | `GestionReservaRequest` (`fecha`, `hora`) | `Reserva` (200 OK) | `ReservaController` | `ReservaService` | Reprogramación cliente (sujeta a regla 24h) |
| `POST` | `/api/reservas/{id}/solicitar-cancelacion`| **Público** | `SolicitudCancelacionRequest` (`codigo`, `tel`, `motivo`)| `Reserva` (200 OK) | `ReservaController` | `ReservaService`, `EmailSvc`| Cancela (>24h) o solicita aprobación (<=24h) |
| `POST` | `/api/wompi/webhook` | **Público** | Payload Evento Wompi + Checksum Header | `Map` (`status: "OK"`) | `WompiWebhookController` | `WompiService`, `ReservaSvc` | Webhook asíncrono de Wompi con checksum |
| `GET` | `/api/reservas` | **ADMIN** | `?fecha=&historial=&tipo=` | `List<Reserva>` | `ReservaController` | `ReservaService` | Listado administrativo con filtros avanzados |
| `PATCH`| `/api/reservas/{id}/aprobar` | **ADMIN** | Ninguno | `Reserva` (200 OK) | `ReservaController` | `ReservaService`, `WASvc` | Aprobación manual de comprobante de pago |
| `PATCH`| `/api/reservas/{id}/denegar` | **ADMIN** | `DenegacionRequest` (`motivo`) | `Reserva` (200 OK) | `ReservaController` | `ReservaService`, `WASvc` | Denegación manual con liberación de stock |
| `PATCH`| `/api/reservas/{id}/cancelar-admin` | **ADMIN** | `CancelacionAdminRequest` (`motivo`) | `Reserva` (200 OK) | `ReservaController` | `ReservaService`, `WASvc` | Cancelación forzada por el salón |
| `PATCH`| `/api/reservas/{id}/aprobar-cancelacion`| **ADMIN** | Ninguno | `Reserva` (200 OK) | `ReservaController` | `ReservaService` | Aprueba solicitud de cancelación de cliente |
| `PATCH`| `/api/reservas/{id}/rechazar-cancelacion`| **ADMIN** | `RechazoCancelacionRequest` (`motivo`)| `Reserva` (200 OK) | `ReservaController` | `ReservaService` | Rechaza solicitud de cancelación de cliente |
| `PATCH`| `/api/reservas/{id}/en-preparacion` | **ADMIN** | Ninguno | `Reserva` (200 OK) | `ReservaController` | `ReservaService` | Transición de pedido a "En preparación" |
| `PATCH`| `/api/reservas/{id}/listo-recoger` | **ADMIN** | Ninguno | `Reserva` (200 OK) | `ReservaController` | `ReservaService`, `EmailSvc`| Notifica al cliente pedido listo para pickup |
| `PATCH`| `/api/reservas/{id}/entregar` | **ADMIN** | Ninguno | `Reserva` (200 OK) | `ReservaController` | `ReservaService` | Marca pedido como "Entregado" |
| `GET` | `/api/dashboard/resumen` | **ADMIN** | Ninguno | Map (KPIs, finanzas, conversión, agenda) | `DashboardController` | `ReservaService` | Resumen analítico y financiero completo |
| `GET` | `/api/productos` | **Público** / **ADMIN**| Ninguno | `List<Producto>` | `ProductoController` | N/A | CRUD completo de productos |
| `POST` | `/api/productos` | **ADMIN** | `Producto` | `Producto` (201 Created) | `ProductoController` | N/A | Crear producto con variantes |
| `PUT` | `/api/productos/{id}` | **ADMIN** | `Producto` | `Producto` (200 OK) | `ProductoController` | N/A | Actualizar producto y stock |
| `DELETE`| `/api/productos/{id}` | **ADMIN** | Ninguno | 204 No Content | `ProductoController` | N/A | Eliminar producto |
| `GET` | `/api/servicios` | **Público** / **ADMIN**| Ninguno | `List<Servicio>` | `ServicioController` | N/A | CRUD completo de servicios |
| `GET` | `/api/kits` | **Público** / **ADMIN**| Ninguno | `List<Kit>` | `KitController` | N/A | CRUD completo de kits |
| `GET` | `/api/categorias-producto`| **Público** / **ADMIN**| Ninguno | `List<CategoriaProducto>` | `CategoriaController` | N/A | CRUD categorías de productos |
| `GET` | `/api/categorias-servicio`| **Público** / **ADMIN**| Ninguno | `List<CategoriaServicio>` | `CategoriaServicioController`| N/A | CRUD categorías de servicios |
| `GET` | `/api/banners` | **Público** / **ADMIN**| Ninguno | `List<Banner>` | `BannerController` | N/A | CRUD banners publicitarios |
| `GET` | `/api/agenda/configuracion` | **ADMIN** | Ninguno | `ConfiguracionAgenda` | `AgendaController` | N/A | Consulta configuración de horarios |
| `PUT` | `/api/agenda/configuracion` | **ADMIN** | `ConfiguracionAgenda` | `ConfiguracionAgenda` | `AgendaController` | N/A | Modifica horarios base del salón |
| `POST` | `/api/agenda/bloqueos` | **ADMIN** | `BloqueoHorario` | `BloqueoHorario` | `AgendaController` | N/A | Agrega bloqueo de horario puntual |
| `DELETE`| `/api/agenda/bloqueos/{id}` | **ADMIN** | Ninguno | 204 No Content | `AgendaController` | N/A | Elimina bloqueo puntual |
| `POST` | `/api/agenda/bloqueos-recurrentes`| **ADMIN** | `BloqueoRecurrente` | `BloqueoRecurrente` | `AgendaController` | N/A | Agrega bloqueo recurrente semanal |
| `DELETE`| `/api/agenda/bloqueos-recurrentes/{id}`| **ADMIN**| Ninguno | 204 No Content | `AgendaController` | N/A | Elimina bloqueo recurrente |
| `POST` | `/api/agenda/excepciones`| **ADMIN** | `ExcepcionAgenda` | `ExcepcionAgenda` | `AgendaController` | N/A | Agrega día festivo / cierre especial |
| `DELETE`| `/api/agenda/excepciones/{id}`| **ADMIN** | Ninguno | 204 No Content | `AgendaController` | N/A | Elimina día festivo / excepción |

---

## 6. CAPA DE SERVICIOS Y LÓGICA DE NEGOCIO

### 6.1. `ReservaService`
Es el núcleo transaccional del sistema.
- **Responsabilidades:**
  - Normalización y cálculo de precios de catálogo en backend (evita manipulación de precios desde el cliente).
  - Cálculo de anticipos: 25% para servicios, 100% para productos y kits.
  - Reserva atómica de inventario (`reservarInventario`) y liberación atómica (`liberarInventario`).
  - Validación de transiciones de estado legales (`validarTransicion`).
  - Control de expiración de pagos (15 minutos de retención).
  - Políticas de cancelación (>24 horas cancela directamente; <=24 horas entra a `Solicitud Cancelación`).
  - Idempotencia en confirmación de pagos vía Wompi o manual.

### 6.2. `WompiService`
Maneja la integración criptográfica y de red con la pasarela Wompi.
- **Responsabilidades:**
  - Cálculo de la firma de integridad SHA-256:  
    `SHA-256(referencia + montoEnCentavos + "COP" + integritySecret)`
  - Validación del checksum SHA-256 en webhooks entrantes:  
    `SHA-256(propiedadesDelEventoConcatenadas + timestamp + eventsSecret)`
  - Consulta del estado de transacciones en la API REST de Wompi (`/v1/transactions/{id}`).
  - Proceso de auto-recuperación de transacciones pendientes o huérfanas.

### 6.3. `EmailNotificationService`
Gestiona el despacho asíncrono de correos transaccionales a través de la API REST v3 de Brevo (`https://api.brevo.com/v3/smtp/email`).
- **Templates implementados:**
  - `enviarConfirmacionReserva`: Confirmación de cita con fecha, hora, especialista, anticipo, saldo y recordatorio de cancelación con 24h de anticipación.
  - `enviarConfirmacionPedido`: Confirmación de pedido de productos con desglose de items, método de entrega (*Envío a Domicilio con dirección completa* o *Recogida en Tienda*).
  - `enviarConfirmacionCompraMixta`: Confirmación dual que incluye sección de turno en salón y sección de productos.
  - `enviarPedidoListoParaRecoger`: Notificación al cliente indicando que su pedido está empacado en el local (exclusivo para pickup).
  - `enviarNotificacionCancelacion`: Confirmación de cancelación exitosa y liberación de turno/stock.
  - `enviarNotificacionSolicitudCancelacion`: Aviso al cliente de que su solicitud tardía (<=24h) entró a revisión del administrador.
- **Resiliencia:** Si `MAIL_ENABLED=false` o no hay `BREVO_API_KEY`, el servicio captura el error, registra un log informativo y permite que el flujo de compra/reserva continúe sin romper la experiencia del usuario.

### 6.4. `WhatsAppNotificationService`
Genera enlaces URL codificados (`https://wa.me/57...`) y resúmenes de texto formateados para atención al cliente y confirmación de turnos.

---

## 7. CICLO DE VIDA DE RESERVAS, PEDIDOS Y COMPRAS MIXTAS

### 7.1. Cita Pura de Servicios
1. Cliente selecciona servicio y fecha.
2. `AgendaController` entrega horarios disponibles reales (excluyendo citas activas, bloqueos y festivos).
3. Backend valida que el horario no esté en el pasado y no esté ocupado.
4. Se crea en estado `Pendiente Pago` con retención de 15 minutos.
5. Tras pago exitoso en Wompi (o aprobación manual de transferencia), pasa a `Confirmado`.
6. En el día agendado, pasa a `En curso` durante el turno y a `Realizada` al finalizar.

### 7.2. Pedido Puro de Productos / Kits
1. Cliente agrega productos/kits al carrito y selecciona método de entrega:
   - **Domicilio**: Campo de dirección obligatorio (`direccionEntrega`).
   - **Pickup**: Dirección no obligatoria.
2. Al crearse la reserva, el stock se descuenta inmediatamente de forma atómica (`inventarioReservado = true`).
3. **No bloquea ningún horario en la agenda** (`esPedidoPuro() == true`).
4. Tras confirmación del pago, pasa a `Pago Confirmado`.
5. En el panel de administración sigue el pipeline:
   `Pago Confirmado` -> `En preparación` -> `Listo para recoger` (si es pickup, envía email Brevo) -> `Entregado`.
6. Vive exclusivamente en la sección **Pedidos** del panel administrativo y nunca contamina la agenda ni las citas.

### 7.3. Compra Mixta (Servicio + Producto)
1. El carrito combina servicios y productos.
2. El servicio bloquea el turno en la agenda; los productos reservan stock atómico.
3. Requiere dirección si se selecciona envío a domicilio para los productos.
4. Tras el pago, la parte del servicio se visualiza en Citas/Agenda y la parte del producto en Pedidos.

---

## 8. GESTIÓN DE INVENTARIO ATÓMICO Y CONCURRENCIA

### 8.1. Descuento Atómico con MongoDB Query Criteria
Para evitar sobreventa (overselling) ante múltiples clientes comprando la última unidad al mismo milisegundo:
```java
// Producto sin variantes
Query q = new Query(Criteria.where("id").is(prodId).and("stock").gte(cantidad));
Update u = new Update().inc("stock", -cantidad);
Producto p = mongoTemplate.findAndModify(q, u, Producto.class);
if (p == null) throw new IllegalStateException("Stock insuficiente...");

// Producto con variante específica
Query q = new Query(Criteria.where("id").is(prodId).and("variantes.sku").is(sku).and("variantes.stock").gte(cantidad));
Update u = new Update().inc("variantes.$.stock", -cantidad);
Producto p = mongoTemplate.findAndModify(q, u, Producto.class);
if (p == null) throw new IllegalStateException("Stock insuficiente para la variante...");
```

### 8.2. Liberación Idempotente de Stock
La liberación de inventario verifica el flag booleano `inventarioReservado`:
- Solo si `Boolean.TRUE.equals(reserva.getInventarioReservado())`, incrementa el stock atómicamente con `$inc: +cantidad` y conmuta `inventarioReservado = false`.
- Llamadas duplicadas o repetidas a `liberarInventario()` son completamente inocuas.

### 8.3. Prevención de Doble Reserva en Agenda
MongoDB cuenta con un **índice compuesto único parcial**:
- **Nombre:** `reserva_fecha_hora_activa_idx`
- **Campos:** `{ "fechaCita": 1, "horaCita": 1 }`
- **Filtro parcial:**  
  `{ "archivada": { "$ne": true }, "estado": { "$in": ["Confirmado", "Pendiente Comprobante", "Pendiente Pago", "Pendiente Reprogramación", "En curso"] } }`
- Si dos peticiones simultáneas intentan agendar la misma fecha y hora para una cita, la segunda es rechazada inmediatamente por MongoDB con `DuplicateKeyException`, el backend captura la excepción, libera cualquier producto del carrito y responde con `HTTP 409 Conflict`.

---

## 9. MOTOR DE AGENDA, HORARIOS Y BLOQUEOS

### 9.1. Algoritmo de Disponibilidad (`AgendaController.java`)
Para una fecha dada `YYYY-MM-DD`:
1. **Validación de Apertura:**
   - Si la fecha tiene una `ExcepcionAgenda` activa con `bloqueado = true`, retorna lista vacía `[]`.
   - Si el día de la semana no está habilitado en `ConfiguracionAgenda`, retorna `[]`.
2. **Generación de Slots:**
   - Se generan slots cada `intervaloMinutos` (default: 30 min) desde `horaApertura` (08:00 AM) hasta `horaCierre` (07:00 PM).
3. **Filtrado de Horarios Pasados:**
   - Si la fecha es hoy, se eliminan todos los slots cuya hora sea menor o igual a la hora actual en zona `America/Bogota` (`HorarioUtil.isPastTimeSlot`).
4. **Filtrado de Ocupación por Citas Activas:**
   - Se consultan las reservas no archivadas de la fecha cuyo estado bloquee agenda.
   - Si una reserva está en `Pendiente Pago` pero ya pasaron 15 minutos (`estaExpirada == true`), se marca como expirada, se libera su inventario y **su horario se vuelve a liberar inmediatamente**.
5. **Filtrado de Bloqueos:**
   - Se descuentan los slots en `BloqueoHorario` (puntuales) y `BloqueoRecurrente` (semanales).
6. **Retorno:** Lista ordenada de horarios disponibles listos para selección en el frontend.

---

## 10. PASARELA DE PAGOS WOMPI

### 10.1. Generación de Checkout (`/api/reservas/preparar-pago`)
1. Backend valida que el monto en centavos coincida con el anticipo (citas) o subtotal (pedidos).
2. Genera una referencia única de pago:  
   `ISV-<TIMESTAMP>-<4_DIGITOS_HEX>`
3. Calcula el hash SHA-256 con el `wompi.integrity-secret`.
4. Retorna al frontend: `publicKey`, `currency: "COP"`, `amountInCents`, `reference`, `signature: { checksum: "<hash>" }`, `redirectUrl`.

### 10.2. Procesamiento de Webhook (`/api/wompi/webhook`)
1. Recibe payload del evento (`transaction.updated`).
2. Valida la firma del evento usando `x-event-checksum` o la firma de propiedades con `wompi.events-secret`.
3. Si la transacción está `APPROVED`:
   - Busca la reserva por `referenciaWompi`.
   - Si no estaba aprobada, llama a `reservaService.aprobarConWompi(...)`.
   - Actualiza `estadoPago = "APROBADO"`, `estado = "Confirmado"` (o `"Pago Confirmado"` si es pedido).
   - Registra `idTransaccionWompi`, `fechaPago = LocalDate.now()`.
   - Despacha correo Brevo y genera notificación WhatsApp.
4. Si la transacción está `DECLINED`, `ERROR` o `VOIDED`:
   - Marca `estadoPago = "RECHAZADO"`, libera el inventario y libera el turno de agenda.

---

## 11. SEGURIDAD, AUTENTICACIÓN Y AUTORIZACIÓN

### 11.1. Arquitectura de Seguridad
- **Framework:** Spring Security 6 + Stateless Session Management.
- **Algoritmo de Hashing:** `BCryptPasswordEncoder` (fuerza 10).
- **Tokens:** JWT firmado con HMAC-SHA256 (`jjwt-api 0.11.5`), expiración configurable (default: 24 horas).
- **Filtro:** `JwtAuthenticationFilter` intercepta cada petición, extrae el header `Authorization: Bearer <token>`, valida firma y expiración, y establece el `SecurityContextHolder`.

### 11.2. Matriz de Autorización
- **Rutas Públicas (`permitAll`):**
  - `/`, `/index.html`, `/css/**`, `/js/**`, `/images/**`, `/favicon.ico`, `/robots.txt`, `/sitemap.xml`
  - `POST /api/auth/login`
  - `GET /api/public/**`
  - `GET /api/agenda/disponibilidad`
  - `POST /api/reservas`, `POST /api/reservas/preparar-pago`, `POST /api/reservas/retomar-pago`, `POST /api/reservas/consultar`
  - `PATCH /api/reservas/{id}/reprogramar`, `POST /api/reservas/{id}/solicitar-cancelacion`
  - `POST /api/wompi/webhook`
  - `GET /api/productos`, `GET /api/servicios`, `GET /api/kits`, `GET /api/banners`, `GET /api/categorias-producto`, `GET /api/categorias-servicio`
- **Rutas Protegidas (`hasAuthority("ROLE_ADMIN")`):**
  - `GET /api/dashboard/**`
  - `GET /api/reservas` (con filtros administrativos)
  - `PATCH /api/reservas/{id}/aprobar`, `/denegar`, `/cancelar-admin`, `/aprobar-cancelacion`, `/rechazar-cancelacion`, `/en-preparacion`, `/listo-recoger`, `/entregar`
  - `POST/PUT/DELETE /api/productos/**`, `/servicios/**`, `/kits/**`, `/banners/**`, `/categorias-producto/**`, `/categorias-servicio/**`
  - `GET/PUT /api/agenda/configuracion`, `POST/DELETE /api/agenda/bloqueos/**`, `/agenda/bloqueos-recurrentes/**`, `/agenda/excepciones/**`
  - `/api/admin/**`

---

## 12. FRONTEND (ARQUITECTURA SPA VANILLA)

- **Tecnologías:** HTML5 Semántico, Vanilla CSS3 (Custom Properties / Design Tokens, Flexbox, CSS Grid, Glassmorphism, Micro-animaciones) y Vanilla JavaScript ES6+ modular.
- **Sin Frameworks Pesados:** Cero dependencias npm en frontend, tiempo de carga ultrarrápido y compatibilidad móvil nativa.
- **Gestión de Estado:**
  - `currentUser`: Almacenado en `sessionStorage` (`isivi_token`, `isivi_user`).
  - `cart`: Carrito reactivo en memoria (`isivi_cart`), persistido en `localStorage` para resiliencia entre pestañas.
  - `activeFilters`: Filtros de categorías, fecha y tipo de vista.
- **Controladores de Vistas en `isivi.js`:**
  - Catálogos interactivos con modales de detalle y selección de variantes de producto.
  - Carrito lateral con cálculo automático de totales, toggle de domicilio/pickup con input de dirección obligatoria.
  - Modal de Checkout integrado con Widget Wompi (`checkout.wompi.co/widget.js`).
  - Pantallas de confirmación (`showSuccessConfirmation`) especializadas para Cita, Pedido Puro y Compra Mixta.
  - Módulo de consulta unificada (`handleLookupSubmit`) con visualización de estado, tracker de 4 pasos para pedidos, y opciones de cancelación / reprogramación con validación de 24 horas.

---

## 13. PANEL ADMINISTRATIVO

Accesible mediante el botón de acceso staff o `/login`.
1. **Dashboard:** Métricas de ventas hoy, ventas del mes, citas de hoy desglosadas (confirmadas, en curso, pendientes), tasa de conversión de checkout y widget de *"Próximas Citas"* en tiempo real.
2. **Agenda Visual:** Grilla horaria interactiva por especialista, creación de bloqueos manuales y festivos.
3. **Citas y Reservas:** Gestión de turnos, confirmación de comprobantes, reprogramaciones y aprobaciones de cancelación.
4. **Pedidos:** Sección dedicada para despacho de productos físicos con botones de cambio de estado (*"Pasar a Preparación"*, *"Listo para Recoger"*, *"Entregar"*).
5. **Catálogos (CRUD):** Formularios para crear/editar productos con variantes, servicios con duración y profesionales, kits y banners.
6. **Historial:** Archivo histórico con búsqueda por código, cliente o teléfono.

---

## 14. SISTEMA DE NOTIFICACIONES

### 14.1. Brevo API v3 (Emails Transaccionales)
- Conexión vía HTTP POST a `https://api.brevo.com/v3/smtp/email` con cabecera `api-key`.
- Renderiza templates HTML profesionales con colores de marca (`#D4AF37` dorado, `#1A1A1A` negro elegante), botones de acción y resúmenes estructurados.
- Eventos disparadores:
  - Creación y confirmación de Cita / Pedido / Compra Mixta.
  - Pedido Listo para Recogida en Salón.
  - Cancelación de Cita / Solicitud de Cancelación en Revisión.

### 14.2. WhatsApp
- Construcción de mensajes de bienvenida, recordatorio y confirmación mediante `WhatsAppNotificationService.resumen(...)`.
- Enlaces con esquema `https://wa.me/573004381831?text=...` para comunicación directa en 1 clic.

---

## 15. ESTRATEGIA DE PRUEBAS Y ASEGURAMIENTO DE CALIDAD

La suite consta de **269 pruebas automatizadas (100% de aprobación)** distribuidas en 38 clases:

| Categoría | Clases de Prueba Principales | Riesgos Protegidos |
|---|---|---|
| **Seguridad y JWT** | `SecurityAuthTest` | Inyección de rutas, tokens inválidos, expiración, protección ROLE_ADMIN |
| **Reservas y Ciclo de Vida** | `BusinessE2ETest`, `ReservationEmailAndLookupTest`, `PurchaseSuccessViewTest` | Flujo completo, vistas diferenciadas, correos Brevo |
| **Concurrencia e Inventario** | `ReservaConcurrencyTest`, `ReservationHoldReleaseTest`, `ReservaExpirationAndConcurrencyTest` | Sobreventa de stock, colisión en agenda, retención 15 min |
| **Políticas de Cancelación** | `CancellationPolicyTest`, `ReservationHistoryTest` | Bloqueo <=24h, cancelación >24h, visibilidad en historial |
| **Wompi Gateway** | `WompiSandboxE2ETest`, `WompiPaymentAuditTest`, `WompiRecoveryTest`, `ResumePaymentRegressionTest` | Firmas SHA-256, webhooks repetidos, retoma de pagos expirados |
| **Agenda y Bloqueos** | `AgendaManagementTest`, `RecurringAgendaBlocksTest`, `HorarioValidationTest` | Overlap de especialistas, festivos, turnos pasados |
| **Domicilio y UX** | `DeliveryAddressTest`, `UxEnhancementsValidationTest`, `OrderWorkflowTest` | Dirección obligatoria en domicilio, tracking de pedidos |
| **Persistencia y Resiliencia**| `PersistenceAuditTest`, `GlobalResilienceAuditTest`, `QuickDateFiltersTest` | No eliminación en Mongo, resiliencia ante caídas de Brevo |

---

## 16. VARIABLES DE ENTORNO Y CONFIGURACIÓN

| Variable | Requerida | Uso / Componente | Comportamiento si Falta |
|---|---|---|---|
| `MONGODB_URI` | **Sí** | `application.properties` -> Conexión MongoDB Atlas | Fallo en arranque (`BeanCreationException`) |
| `JWT_SECRET` | **Sí** | `JwtService` -> Firma de tokens de autenticación | Usa clave por defecto o falla si no cumple 256 bits |
| `WOMPI_PUBLIC_KEY` | **Sí** | `WompiService`, `PublicConfigController` -> Checkout | Modo simulación / Widget no abre |
| `WOMPI_PRIVATE_KEY`| **Sí** | `WompiService` -> Consultas REST a Wompi | No puede consultar estado de transacciones |
| `WOMPI_INTEGRITY_SECRET`| **Sí**| `WompiService` -> Firma SHA-256 de checkout | Pagos rechazados por firma inválida |
| `WOMPI_EVENTS_SECRET`| **Sí** | `WompiWebhookController` -> Checksum de webhooks | Webhooks rechazados por seguridad |
| `BREVO_API_KEY` | Opcional | `EmailNotificationService` -> Envío de correos | Log de advertencia; correos se omiten sin romper |
| `MAIL_ENABLED` | Opcional | `application.properties` (default: `true`) | Si `false`, desactiva despacho Brevo |
| `PORT` | Opcional | Puerto de escucha en Render (default: `8080`) | Usa `8080` por defecto |

---

## 17. DESPLIEGUE E INFRAESTRUCTURA EN RENDER

- **Plataforma:** Render (`https://isivi-app.onrender.com/`)
- **Runtime:** Java 17 LTS (Eclipse Temurin / OpenJDK 17)
- **Build Command:** `mvn clean package -DskipTests`
- **Start Command:** `java -Dserver.port=$PORT -jar target/isivi-app-1.0.0.jar`
- **Docker:** `Dockerfile` multi-stage disponible (`maven:3.9.6-eclipse-temurin-17` -> `eclipse-temurin:17-jre-jammy`).
- **Health Check & Static:** Render sirve el archivo JAR embebido con Tomcat; `static/index.html` es el welcome page servido automáticamente en la raíz `/`.
- **Base de Datos:** MongoDB Atlas Replica Set (AWS us-east-1).

---

## 18. CATÁLOGO EXHAUSTIVO DE REGLAS DE NEGOCIO

1. **Anticipos:** Toda cita de servicios exige exactamente el **25% de anticipo** para confirmarse; pedidos de productos/kits exigen el **100% de pago**.
2. **Retención de Turno y Stock:** Al crearse una reserva en `Pendiente Pago`, el turno en agenda y el stock de productos quedan retenidos por **15 minutos**. Si no se completa el pago en ese lapso, el sistema libera automáticamente el turno y el stock.
3. **Política de Cancelación Estricta:**
   - Si faltan **más de 24 horas** para la cita: El cliente puede cancelar directamente desde la web, liberando la agenda y notificando por email.
   - Si faltan **24 horas o menos**: El sistema **bloquea la cancelación automática** y crea una `Solicitud Cancelación`, la cual debe ser revisada y aprobada o rechazada manualmente por el administrador.
4. **Política de Reprogramación:** Solo se permite reprogramar citas que tengan más de 24 horas de antelación y hacia horarios que se encuentren 100% disponibles.
5. **Dirección en Domicilios:** Si `tipoEntrega == "domicilio"`, el campo `direccionEntrega` es estrictamente obligatorio (error `HTTP 400 DIRECCION_REQUERIDA`). Si es `"pickup"`, la dirección es opcional.
6. **Separación de Pedidos:** Los pedidos puros de productos **nunca** bloquean agenda ni aparecen en las listas de citas o métricas de turnos.
7. **Idempotencia de Pagos y Webhooks:** Si Wompi envía múltiples veces el mismo webhook de aprobación, el backend verifica el estado previo y no duplica transiciones ni confirmaciones.

---

## 19. GRAFO DE DEPENDENCIAS ENTRE COMPONENTES

```
ReservaController
  ├── ReservaService
  │     ├── ProductoRepository
  │     ├── KitRepository
  │     ├── ServicioRepository
  │     ├── ReservaRepository
  │     ├── MongoTemplate
  │     └── EmailNotificationService
  ├── WompiService
  ├── EmailNotificationService
  └── WhatsAppNotificationService

DashboardController
  ├── ReservaRepository
  ├── ProductoRepository
  ├── KitRepository
  └── ReservaService

AgendaController
  ├── ReservaRepository
  ├── ConfiguracionAgendaRepository
  ├── BloqueoHorarioRepository
  ├── BloqueoRecurrenteRepository
  ├── ExcepcionAgendaRepository
  └── ReservaService

WompiWebhookController
  ├── WompiService
  ├── ReservaService
  ├── EmailNotificationService
  └── WhatsAppNotificationService
```

---

## 20. FLUJOS OPERATIVOS PASO A PASO

### Flujo A: Compra de Producto con Entrega a Domicilio
1. Cliente navega el catálogo, selecciona producto y variante.
2. Abre el carrito y elige *"Envío a Domicilio"*.
3. Ingresa su dirección obligatoria en el campo `#cart-delivery-address`.
4. Ingresa nombre, teléfono y correo electrónico.
5. Clic en *"Pagar con Wompi"*.
6. Backend ejecuta `POST /api/reservas`: valida dirección, descuenta stock atómicamente y genera código `ISV-...`.
7. Backend ejecuta `POST /api/reservas/preparar-pago`: genera firma SHA-256 y abre Widget Wompi.
8. Cliente paga con tarjeta/PSE/Nequi.
9. Wompi confirma transacción y notifica a `/api/wompi/webhook`.
10. Backend pasa el pedido a `Pago Confirmado` y despacha email de confirmación con la dirección de entrega.
11. En el frontend se muestra el modal: *"¡Tu pedido fue confirmado con éxito! 🎉"*.
12. Admin visualiza el pedido en la sección **Pedidos**, lo pasa a *"En preparación"* y finalmente a *"Entregado"*.

---

## 21. MÁQUINAS DE ESTADOS Y TRANSICIONES CANÓNICAS

### 21.1. Máquina de Estados de Citas
```
[Inicio] ──> Pendiente Pago (Retención 15m)
                 │
                 ├── (Pago Wompi Aprobado / Comprobante OK) ──> Confirmado
                 │                                                │
                 │                                                ├── (Horario transcurre) ──> En curso ──> Realizada
                 │                                                │
                 │                                                ├── (Cancelación >24h) ──> Cancelada
                 │                                                │
                 │                                                └── (Cancelación <=24h) ──> Solicitud Cancelación
                 │                                                                               ├── (Aprobada Admin) ──> Cancelada
                 │                                                                               └── (Rechazada Admin) ──> Confirmado
                 │
                 └── (Tiempo expira sin pago / Rechazo) ──> Expirada / Denegada (Libera turno)
```

### 21.2. Máquina de Estados de Pedidos
```
[Inicio] ──> Pendiente Pago (Descuento Stock Atómico)
                 │
                 ├── (Pago Aprobado) ──> Pago Confirmado
                 │                             │
                 │                             └──> En preparación
                 │                                     │
                 │                                     ├── [Pickup] ──> Listo para recoger (Email) ──> Entregado
                 │                                     │
                 │                                     └── [Domicilio] ──────────────────────────────> Entregado
                 │
                 └── (Pago Rechazado / Expirado) ──> Expirada / Cancelada (Libera Stock Atómico)
```

---

## 22. RIESGOS CONOCIDOS Y CASOS BORDE CRÍTICOS

1. **Zona Horaria del Servidor:** Toda la lógica temporal de la agenda, expiraciones y cálculos de 24 horas debe operar en `ZoneId.of("America/Bogota")`. Si el servidor ejecuta en UTC sin la configuración de zona, los horarios pueden desfasarse 5 horas.
2. **Expiración de Tokens JWT:** El frontend maneja la expiración de sesión redirigiendo a la pantalla de login al recibir `HTTP 401 Unauthorized` o `HTTP 403 Forbidden`.
3. **Bloqueo de Modificación de Precios en Frontend:** Los precios enviados en los payloads JSON son revalidados contra la base de datos en `ReservaService.prepararNuevaReserva`. Nunca se debe confiar en el subtotal enviado por el navegador.

---

## 23. AUDITORÍA DE DISCREPANCIAS (CÓDIGO VS DOCS PREVIAS)

- **Texto de Consulta Pública:** Anteriormente la documentación mencionaba *"Consultar mi reserva"*. El código actual utiliza **"Consultar reserva o pedido"** para reflejar la unificación de citas y pedidos de productos.
- **Validación de Dirección:** Previamente los pedidos asumían pickup por defecto. En la versión actual, si se selecciona `"domicilio"`, la dirección es obligatoria en backend y frontend.
- **Clasificación de Estados:** Los pedidos de productos utilizan estados dedicados (`Pago Confirmado`, `En preparación`, `Listo para recoger`, `Entregado`) y no los estados de citas (`Confirmado`, `En curso`, `Realizada`).

---

## 24. LO QUE UN NUEVO DESARROLLADOR / AGENTE DEBE SABER ANTES DE TOCAR ISIVI

1. **NO mezclar Citas y Pedidos:** Las citas usan el motor de `AgendaController` y bloquean slots; los pedidos son compras de stock físico y usan `inventarioReservado`. Nunca permitas que un pedido puro bloquee agenda.
2. **Cuidado con `archivarReservasVencidas()`:** Esta tarea programada archiva citas pasadas y pedidos terminales. Nunca archives pedidos activos (`Pago Confirmado`, `En preparación`, `Listo para recoger`) por fecha.
3. **Firma SHA-256 de Wompi:** La concatenación de valores para la firma de integridad debe seguir exactamente el orden: `referencia + montoEnCentavos + "COP" + integritySecret`.
4. **Idempotencia de Inventario:** Cada vez que liberes inventario, hazlo a través de `reservaService.liberarInventario(reserva)` para asegurar que el flag booleano prevenga doble devolución.
5. **No romper los 269 Tests:** Cualquier cambio en modelos, controladores o servicios debe validarse ejecutando `mvn test` para garantizar cero regresiones en la suite de 269 pruebas.
