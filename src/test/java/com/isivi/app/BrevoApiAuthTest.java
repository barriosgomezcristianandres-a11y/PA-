package com.isivi.app;

import com.isivi.app.model.Administrador;
import com.isivi.app.repository.AdministradorRepository;
import com.isivi.app.security.JwtService;
import com.isivi.app.service.EmailNotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "isivi.brevo.api-key=xkeysib-mock-test-auth-key-9999",
        "isivi.brevo.base-url=https://api.brevo.com/v3/smtp/email",
        "app.mail.from=contacto@isivi.com",
        "app.mail.enabled=true"
})
public class BrevoApiAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private MockRestServiceServer mockServer;
    private String validAdminToken;

    @BeforeEach
    void setUp() {
        administradorRepository.findByUsuario("admin_auth_test").ifPresent(administradorRepository::delete);
        administradorRepository.save(new Administrador("admin_auth_test", passwordEncoder.encode("secretAdminPass")));
        validAdminToken = jwtService.generarToken("admin_auth_test");

        RestClient.Builder builder = RestClient.builder();
        this.mockServer = MockRestServiceServer.bindTo(builder).build();
        emailNotificationService.setRestClient(builder.build());
    }

    @AfterEach
    void tearDown() {
        administradorRepository.findByUsuario("admin_auth_test").ifPresent(administradorRepository::delete);
    }

    @Test
    @DisplayName("1. GET /api/admin/email/brevo-auth-test responde BREVO_AUTH_OK cuando Brevo devuelve 200")
    void testBrevoAuthSuccess() throws Exception {
        mockServer.expect(requestTo("https://api.brevo.com/v3/account"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("api-key", "xkeysib-mock-test-auth-key-9999"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"email\":\"contacto@isivi.com\",\"firstName\":\"ISIVI\",\"lastName\":\"Admin\"}"));

        String response = mockMvc.perform(get("/api/admin/email/brevo-auth-test")
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.code").value("BREVO_AUTH_OK"))
                .andReturn().getResponse().getContentAsString();

        mockServer.verify();
        assertFalse(response.contains("xkeysib-mock-test-auth-key-9999"), "No debe exponer la API key");
    }

    @Test
    @DisplayName("2. GET /api/admin/email/brevo-auth-test responde BREVO_AUTH_401 cuando Brevo devuelve 401")
    void testBrevoAuth401() throws Exception {
        mockServer.expect(requestTo("https://api.brevo.com/v3/account"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("api-key", "xkeysib-mock-test-auth-key-9999"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":\"unauthorized\",\"message\":\"Key not found\"}"));

        mockMvc.perform(get("/api/admin/email/brevo-auth-test")
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.code").value("BREVO_AUTH_401"));

        mockServer.verify();
    }

    @Test
    @DisplayName("3. GET /api/admin/email/brevo-auth-test responde BREVO_AUTH_403 cuando Brevo devuelve 403")
    void testBrevoAuth403() throws Exception {
        mockServer.expect(requestTo("https://api.brevo.com/v3/account"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("api-key", "xkeysib-mock-test-auth-key-9999"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":\"forbidden\",\"message\":\"Not allowed\"}"));

        mockMvc.perform(get("/api/admin/email/brevo-auth-test")
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.code").value("BREVO_AUTH_403"));

        mockServer.verify();
    }

    @Test
    @DisplayName("4. GET /api/admin/email/brevo-auth-test responde BREVO_TIMEOUT ante fallo de red")
    void testBrevoAuthTimeout() throws Exception {
        mockServer.expect(requestTo("https://api.brevo.com/v3/account"))
                .andExpect(method(HttpMethod.GET))
                .andRespond((request) -> {
                    throw new org.springframework.web.client.ResourceAccessException("Connect timed out", new SocketTimeoutException("connect timeout"));
                });

        mockMvc.perform(get("/api/admin/email/brevo-auth-test")
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.code").value("BREVO_TIMEOUT"));

        mockServer.verify();
    }

    @Test
    @DisplayName("5. GET /api/admin/email/brevo-auth-test sin autenticación devuelve 401 Unauthorized")
    void testBrevoAuthUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/email/brevo-auth-test"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("NO_AUTORIZADO"));
    }
}
