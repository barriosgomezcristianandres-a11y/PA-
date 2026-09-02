# REGISTRO DE MODIFICACIONES Y HARDENING DE SEGURIDAD - ISIVI

**Fecha**: 22 de Agosto de 2026  
**Proyecto**: ISIVI Backend (Spring Boot)  

---

## 🛠️ Modificaciones de Código Realizadas

### 1. `RateLimitingFilter.java` [NUEVO]
- **Archivo**: `src/main/java/com/isivi/app/security/RateLimitingFilter.java`
- **Cambio**: Se implementó un filtro de protección de rate limiting por dirección IP con ventana deslizante Token Bucket en memoria concurrente (`ConcurrentHashMap`).
- **Comportamiento**:
  - Extrae la IP cliente real resolviendo las cabeceras de proxy de confianza `X-Forwarded-For` / `X-Real-IP`.
  - Limita `/api/auth/login` a **5 peticiones por minuto** por IP (prevención de fuerza bruta).
  - Limita `/api/pagos/wompi/preparar` y `/api/pagos/wompi/retomar` a **10 peticiones por minuto** por IP.
  - Limita endpoints públicos sensibles a **30 peticiones por minuto** por IP.
  - Limita peticiones generales a **120 peticiones por minuto** por IP.
  - Retorna `HTTP 429 Too Many Requests` cuando se excede la cuota.

### 2. `SecurityConfig.java` [MODIFICADO]
- **Archivo**: `src/main/java/com/isivi/app/security/SecurityConfig.java`
- **Cambio 1 (Headers de Seguridad HTTP)**:
  - `X-Frame-Options: DENY` (Protección contra Clickjacking).
  - `X-Content-Type-Options: nosniff` (Prevención de MIME sniffing).
  - `Referrer-Policy: strict-origin-when-cross-origin`.
  - `Strict-Transport-Security: max-age=31536000; includeSubDomains` (HSTS).
- **Cambio 2 (Hardening de CORS)**:
  - Eliminado la combinación insegura `allowedOriginPatterns("*")` con `allowCredentials(true)`.
  - Configurado `allowedOrigins` parametrizable mediante la variable `${isivi.cors.allowed-origins:https://isivi-app.onrender.com,...}`.
- **Cambio 3 (Filtro Rate Limiting)**:
  - Registrado `RateLimitingFilter` antes del filtro de autenticación JWT.

### 3. `RateLimitingAndSecurityHeadersTest.java` [NUEVO]
- **Archivo**: `src/test/java/com/isivi/app/RateLimitingAndSecurityHeadersTest.java`
- **Cambio**: Creadas pruebas unitarias y de integración que verifican la presencia de los headers de seguridad en las respuestas HTTP y la activación del error HTTP 429 al superar 5 intentos fallidos de login desde una misma IP.

---

## 🧪 Pruebas Ejecutadas y Verificación

1. **`mvn test -Dtest=RateLimitingAndSecurityHeadersTest`**:
   - `BUILD SUCCESS` (2/2 pruebas pasadas en 6.9 segundos).
2. **`mvn test` (Suite Completa de Pruebas de Unidad e Integración)**:
   - `BUILD SUCCESS` (319/319 pruebas pasadas, 0 fallos, 0 errores).
3. **`mvn clean package -DskipTests`**:
   - `BUILD SUCCESS` (Compilación de artefacto `.jar` listo para producción).
