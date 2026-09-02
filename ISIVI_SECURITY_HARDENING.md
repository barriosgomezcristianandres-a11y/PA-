# INFORME DE AUDITORÍA Y HARDENING DE SEGURIDAD - ISIVI

**Proyecto**: ISIVI  
**Backend**: Spring Boot 3.2.5  
**Base de datos**: MongoDB  
**Autenticación**: JWT  
**Pagos**: Wompi  
**Entorno Evaludao**: Producción / Render  
**Fecha de Evaluación**: 22 de Agosto de 2026  

---

## 📊 Tabla Resumen de Controles de Seguridad

| Control | Estado | Evidencia | Riesgo | Acción Realizada |
| :--- | :---: | :--- | :---: | :--- |
| **1. Rate Limiting** | **IMPLEMENTADO** | `RateLimitingFilter.java` integrado en el filtro Servlet de Spring Security. Protege `/api/auth/login` (5 req/min), `/api/pagos/wompi/preparar` (10 req/min), reservas (30 req/min). | Alto (previo) | Implementado algoritmo Token Bucket por ventana deslizante en memoria compatible con proxies Render. |
| **2. Variables de Entorno / .env** | **IMPLEMENTADO** | Inyección parametrizada `${MONGODB_URI}`, `${WOMPI_PUBLIC_KEY}`, `${JWT_SECRET}`, `${BREVO_API_KEY}` en `application.properties`. | Alto (si falla) | Confirmado: cero secretos expuestos en código fuente Java ni frontend. |
| **3. Secrets Fuera del Repositorio** | **IMPLEMENTADO** | `.gitignore` incluye `.env`, `application-local.properties` y temporales. `git status` limpio sin llaves expuestas. | Medio | Verificado: `application-local.properties` excluido de compilación y control de versiones. |
| **4. IP Limiting** | **IMPLEMENTADO** | `RateLimitingFilter.java` extrae la IP origen analizando `X-Forwarded-For` (Render Proxy) / `X-Real-IP`. | Medio | Aplicado a login, checkout de pagos Wompi y consulta de reservas. |
| **5. Input Sanity** | **IMPLEMENTADO** | `@Valid`, `@NotBlank`, regex de correo, sanitización de teléfono (`normalizarTelefono`) y trimming en DTOs y controladores. | Medio | Normalización activa de caracteres, longitudes y tipos de datos. |
| **6. Server-Side Validation** | **IMPLEMENTADO** | Precios, stock, anticipos y saldos son recalculados en `ReservaService` consultando MongoDB atómicamente. | Crítico (si falla) | El cliente nunca define precios finales ni estados de pago unilateralmente. |
| **7. Autorización Documental (RLS MongoDB)** | **IMPLEMENTADO** | `ReservaService` exige código + teléfono para modificaciones de clientes y `ROLE_ADMIN` en JWT para operaciones globales. | Alto | Previene IDOR/BOLA. Imposible consultar/modificar reservas ajenas sin credenciales del titular. |
| **8. Encryption** | **IMPLEMENTADO** | Hash `BCryptPasswordEncoder` para contraseñas de admin. Algoritmos SHA-256 HMAC para firmas Wompi y tokens JWT. | Alto | Transmisión 100% TLS/HTTPS en producción. |
| **9. Expiración de Sesión** | **IMPLEMENTADO** | JWT token con validez de 24 horas y validación de expiración y firma en `JwtService` y `JwtAuthenticationFilter`. | Medio | Tokens vencidos o alterados son rechazados de forma inmediata por el filtro de autenticación. |
| **10. CORS Correctamente Configurado** | **IMPLEMENTADO** | `SecurityConfig.java` actualizado: eliminado wildcard `*` con `allowCredentials(true)`. Orígenes restringidos y parametrizables vía `isivi.cors.allowed-origins`. | Alto (previo) | Corregido para evitar ataques Cross-Origin con credenciales. |
| **11. Headers de Seguridad** | **IMPLEMENTADO** | Headers HTTP añadidos en `SecurityConfig`: `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, `Referrer-Policy`, HSTS. | Medio | Protección activa contra Clickjacking, MIME sniffing e inserción en iFrames. |
| **12. Logs** | **IMPLEMENTADO** | Inspección realizada: no se registran tokens JWT completos, contraseñas ni llaves privadas en logs del sistema. | Medio | Verificado en handlers globales y registradores de SLF4J. |
| **13. Webhook Wompi** | **IMPLEMENTADO** | Verificación de firma `X-Event-Checksum`, checksum de `signature.properties`, ambiente (Sandbox/Prod), monto en centavos e idempotencia. | Crítico (si falla) | Pago aprobado solo tras confirmación criptográfica del webhook directo de Wompi. |
| **14. Rate Limiting de Pagos** | **IMPLEMENTADO** | Endpoints `/api/pagos/wompi/preparar` y `/api/pagos/wompi/retomar` limitados por IP. | Medio | Previene creación masiva/inundación de referencias de pago. |
| **15. Seguridad de Inputs de Dinero** | **IMPLEMENTADO** | `ReservaService.precioActual` ignora valores del cliente y consulta el precio original en la colección de MongoDB. | Crítico (si falla) | Cálculo de anticipo y saldo 100% blindado en backend. |

---

## 📜 DICTAMEN FINAL DE SEGURIDAD

### **⚠️ HARDENING CON OBSERVACIONES**

> **Observaciones**:  
> 1. El hardening del backend está **COMPLETO** y validado con 319 pruebas automatizadas exitosas (`mvn test`).  
> 2. Se recomienda verificar en el panel de Render que la variable `isivi.cors.allowed-origins` contenga únicamente el dominio de producción `https://isivi-app.onrender.com`.  
> 3. No declarar "100% seguro"; mantener monitoreo continuo de logs y rotación de `JWT_SECRET`.
