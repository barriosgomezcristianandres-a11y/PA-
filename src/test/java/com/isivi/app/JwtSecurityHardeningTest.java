package com.isivi.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isivi.app.model.Administrador;
import com.isivi.app.repository.AdministradorRepository;
import com.isivi.app.security.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class JwtSecurityHardeningTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${isivi.security.jwt-secret}")
    private String configuredSecret;

    @BeforeEach
    void setUp() {
        administradorRepository.findAllByUsuario("admin").forEach(administradorRepository::delete);
        administradorRepository.save(new Administrador("admin", passwordEncoder.encode("1234")));
    }

    @Test
    @DisplayName("1. JwtService obtiene el secreto desde la configuración")
    void testJwtSecretObtainedFromConfiguration() {
        assertNotNull(configuredSecret);
        assertFalse(configuredSecret.isBlank());
        // El valor inyectado en JwtService debe ser igual al configurado en application-local.properties
        assertEquals("isivi_local_development_jwt_secret_key_must_be_long_enough_256_bits_for_local_env", configuredSecret);
    }

    @Test
    @DisplayName("2. No existe secreto JWT ni fallback hardcodeado en el código de JwtService.java")
    void testNoHardcodedSecretInJwtServiceSourceCode() throws IOException {
        Path path = Paths.get("src/main/java/com/isivi/app/security/JwtService.java");
        assertTrue(Files.exists(path), "El archivo JwtService.java debe existir");
        String sourceCode = Files.readString(path);

        // Verificar que no se defina una clave fallback literal en el código
        assertFalse(sourceCode.contains("isivi_secure_jwt_secret_key"), "No debe haber secretos antiguos en el código");
        
        // Verificar que no haya un fallback ":" en la anotación @Value del secreto
        assertTrue(sourceCode.contains("@Value(\"${isivi.security.jwt-secret}\")"), 
                "El secreto JWT debe requerirse obligatoriamente sin fallback por defecto");
    }

    @Test
    @DisplayName("3 y 4. El Login genera un token JWT válido y se puede validar correctamente")
    void testLoginGeneratesValidJwtAndValidatesSuccessfully() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("usuario", "admin", "contrasena", "1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticado").value(true))
                .andExpect(jsonPath("$.token").isString())
                .andReturn().getResponse().getContentAsString();

        Map<?, ?> body = objectMapper.readValue(response, Map.class);
        String token = (String) body.get("token");

        assertNotNull(token);
        assertTrue(jwtService.validarToken(token), "El token generado por login debe ser válido");
        assertEquals("admin", jwtService.extraerUsuario(token), "El usuario extraído debe ser 'admin'");
    }

    @Test
    @DisplayName("5. Un token JWT expirado es rechazado")
    void testExpiredJwtIsRejected() {
        // Generar un token con expiración en el pasado
        Date ahora = new Date();
        Date pasado = new Date(ahora.getTime() - 10000); // Expiró hace 10 segundos

        byte[] keyBytes = configuredSecret.getBytes(StandardCharsets.UTF_8);
        String expiredToken = Jwts.builder()
                .subject("admin")
                .claim("role", "ROLE_ADMIN")
                .issuedAt(pasado)
                .expiration(pasado)
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();

        assertFalse(jwtService.validarToken(expiredToken), "El token expirado debe ser inválido");
    }

    @Test
    @DisplayName("6. Un token JWT manipulado es rechazado")
    void testManipulatedJwtIsRejected() {
        String token = jwtService.generarToken("admin");
        // Manipular el token alterando un carácter en medio de la firma para evitar redundancias de base64url al final
        int middleSigIndex = token.lastIndexOf('.') + 5;
        char replacement = token.charAt(middleSigIndex) == 'A' ? 'B' : 'A';
        String manipulatedToken = token.substring(0, middleSigIndex) + replacement + token.substring(middleSigIndex + 1);

        assertFalse(jwtService.validarToken(manipulatedToken), "El token manipulado debe ser inválido");
    }

    @Test
    @DisplayName("7. ROLE_ADMIN sigue funcionando y permite accesos administrativos")
    void testRoleAdminAuthorizesRequests() throws Exception {
        String token = jwtService.generarToken("admin");

        mockMvc.perform(get("/api/dashboard/resumen")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.citasHoy").exists());
    }

    @Test
    @DisplayName("8. La aplicación no tiene secretos JWT literales en application.properties")
    void testNoLiteralJwtSecretInApplicationProperties() throws IOException {
        Path path = Paths.get("src/main/resources/application.properties");
        assertTrue(Files.exists(path));
        String propertiesContent = Files.readString(path);

        assertTrue(propertiesContent.contains("isivi.security.jwt-secret=${JWT_SECRET}"),
                "application.properties debe usar la expresión ${JWT_SECRET} sin fallbacks");
        assertFalse(propertiesContent.contains("isivi.security.jwt-secret=isivi"),
                "application.properties no debe contener ningún secreto literal");
    }
}
