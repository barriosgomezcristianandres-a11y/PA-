# ESPECIFICACIÓN DE SEGURIDAD Y AUTENTICACIÓN — ISIVI

## 1. Modelo de Seguridad
ISIVI utiliza **Spring Security 6** configurado en modo **Stateless** (sin sesiones HTTP en servidor), utilizando JSON Web Tokens (**JWT**) con firma criptográfica HMAC-SHA256.

---

## 2. Autenticación de Administradores
- **Endpoint:** `POST /api/auth/login`
- **Request:** `{ "username": "admin", "password": "<RAW_PASSWORD>" }`
- **Verificación:** `BCryptPasswordEncoder` compara el hash almacenado en la colección `administradores`.
- **Emisión de Token:** Si las credenciales coinciden, `JwtService.generarToken(username, "ROLE_ADMIN")` produce un JWT que contiene:
  - `sub`: Nombre de usuario
  - `role`: `"ROLE_ADMIN"`
  - `iat`: Timestamp de emisión
  - `exp`: Timestamp de expiración (24 horas por defecto)

---

## 3. Cadena de Filtros de Seguridad (`SecurityConfig.java`)

```
Petición Entrante HTTP
       │
       ▼
CorsConfigurationSource (Permite GET, POST, PUT, PATCH, DELETE, OPTIONS con headers de autorización)
       │
       ▼
DisableEncodeUrlFilter
       │
       ▼
JwtAuthenticationFilter (Extrae header Authorization: Bearer <TOKEN>)
  ├─> Token Válido ──> Establece UsernamePasswordAuthenticationToken en SecurityContextHolder con autoridad ROLE_ADMIN
  └─> Token Inválido o Ausente ──> Deja el contexto anónimo (continúa evaluación)
       │
       ▼
AuthorizationFilter
  ├─> Rutas Públicas (PermitAll) ──> Procesa petición
  └─> Rutas Protegidas (/api/dashboard/**, /api/reservas/** con mutaciones, /api/productos POST/PUT/DELETE, etc.)
        ├─> Si tiene ROLE_ADMIN ──> Procesa petición
        └─> Si no tiene ROLE_ADMIN ──> Retorna HTTP 401 Unauthorized o HTTP 403 Forbidden
```

---

## 4. Validación de Webhooks de Wompi
Para proteger el endpoint público `POST /api/wompi/webhook` contra peticiones maliciosas o suplantación de pagos:
1. El webhook de Wompi incluye una firma criptográfica en el header `x-event-checksum` o dentro del campo `signature.checksum`.
2. El backend concatena las propiedades del evento en el orden estricto especificado por Wompi, agrega el timestamp y el secreto de eventos (`WOMPI_EVENTS_SECRET`), y calcula el hash SHA-256.
3. Si los hashes no coinciden, la petición es rechazada de inmediato con `HTTP 401 / 400`.

---

## 5. Prevención de Inyecciones y Fugas
1. **Sin Inyección NoSQL:** Todas las consultas en `ReservaService` y repositorios de Spring Data utilizan objetos parametrizados (`Criteria`, `Query`, `Update`) sin interpolación directa de strings.
2. **Normalización de Teléfonos:** Los números de teléfono se sanean con `replaceAll("\\D", "")` antes de cualquier consulta.
3. **CORS Restringido:** Permite los orígenes de producción configurados y peticiones locales durante desarrollo.
