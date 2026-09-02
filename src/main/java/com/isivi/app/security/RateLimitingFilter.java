package com.isivi.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Filtro de Rate Limiting contextual por IP o identidad (para administradores).
 * Permite prevenir ataques de fuerza bruta y abuso de API, sin penalizar cargas legítimas de administración.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    // Límites para público general y endpoints específicos (por IP)
    private static final int MAX_LOGIN_ATTEMPTS_PER_MIN = 5;
    private static final int MAX_PAYMENT_ATTEMPTS_PER_MIN = 10;
    private static final int MAX_PUBLIC_SENSITIVE_PER_MIN = 30;
    private static final int MAX_GENERAL_API_PER_MIN = 120;

    // Límites para administradores autenticados (por usuario)
    private static final int MAX_ADMIN_READ_PER_MIN = 600;
    private static final int MAX_ADMIN_WRITE_PER_MIN = 120;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 1. Exclusión de recursos estáticos, frontend y pings de salud
        if (isStaticResource(path) || "/api/ping".equalsIgnoreCase(path) || "/api/health".equalsIgnoreCase(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Determinar si el usuario está autenticado como administrador
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.isAuthenticated() &&
                auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        String bucketKey;
        int maxPermitidos;

        if (isAdmin) {
            // Rate limiting por identidad del administrador
            String adminUser = auth.getName();
            String metodo = request.getMethod();
            boolean isRead = "GET".equalsIgnoreCase(metodo);

            maxPermitidos = isRead ? MAX_ADMIN_READ_PER_MIN : MAX_ADMIN_WRITE_PER_MIN;
            bucketKey = "admin:" + adminUser + ":" + (isRead ? "READ" : "WRITE");
        } else {
            // Rate limiting público por dirección IP
            String clientIp = obtenerIpCliente(request);
            maxPermitidos = obtenerLimiteParaRutaPublica(path);
            bucketKey = "public:" + clientIp + ":" + obtenerCategoriaRutaPublica(path);
        }

        TokenBucket bucket = buckets.computeIfAbsent(bucketKey, k -> new TokenBucket(maxPermitidos, 60000));

        if (!bucket.consumir()) {
            long segundosEspera = bucket.getSegundosRestantes();
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(segundosEspera));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(String.format(
                    "{\"error\":\"RATE_LIMIT_EXCEEDED\",\"mensaje\":\"Demasiadas peticiones. Por favor intenta de nuevo en %d segundos.\",\"retryAfter\":%d}",
                    segundosEspera, segundosEspera
            ));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isStaticResource(String path) {
        return path.endsWith(".html") || path.endsWith(".css") || path.endsWith(".js")
                || path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".ico")
                || path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/assets/")
                || path.startsWith("/images/");
    }

    private int obtenerLimiteParaRutaPublica(String path) {
        if ("/api/uploads/log-fallback".equalsIgnoreCase(path)) {
            return 5; // Límite estricto de 5 logs de fallback por minuto por IP para evitar abuso
        }
        if ("/api/auth/login".equalsIgnoreCase(path)) {
            return MAX_LOGIN_ATTEMPTS_PER_MIN;
        }
        if (path.startsWith("/api/pagos/wompi/preparar") || path.startsWith("/api/pagos/wompi/retomar")) {
            return MAX_PAYMENT_ATTEMPTS_PER_MIN;
        }
        if (path.startsWith("/api/reservas") || path.startsWith("/api/pagos")) {
            return MAX_PUBLIC_SENSITIVE_PER_MIN;
        }
        return MAX_GENERAL_API_PER_MIN;
    }

    private String obtenerCategoriaRutaPublica(String path) {
        if ("/api/uploads/log-fallback".equalsIgnoreCase(path)) return "FALLBACK_LOG";
        if ("/api/auth/login".equalsIgnoreCase(path)) return "LOGIN";
        if (path.startsWith("/api/pagos/wompi/preparar") || path.startsWith("/api/pagos/wompi/retomar")) return "PAYMENT";
        if (path.startsWith("/api/reservas")) return "RESERVAS";
        return "GENERAL";
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }

    // Estructura interna Token Bucket por ventana de tiempo con cálculo de espera
    private static class TokenBucket {
        private final int capacidad;
        private final long ventanaMs;
        private final AtomicInteger tokens;
        private volatile long ultimoReinicio;

        public TokenBucket(int capacidad, long ventanaMs) {
            this.capacidad = capacidad;
            this.ventanaMs = ventanaMs;
            this.tokens = new AtomicInteger(capacidad);
            this.ultimoReinicio = System.currentTimeMillis();
        }

        public synchronized boolean consumir() {
            long ahora = System.currentTimeMillis();
            if (ahora - ultimoReinicio > ventanaMs) {
                tokens.set(capacidad);
                ultimoReinicio = ahora;
            }
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }

        public long getSegundosRestantes() {
            long ahora = System.currentTimeMillis();
            long transcurrido = ahora - ultimoReinicio;
            long restanteMs = ventanaMs - transcurrido;
            return Math.max(1, (long) Math.ceil(restanteMs / 1000.0));
        }
    }
}
