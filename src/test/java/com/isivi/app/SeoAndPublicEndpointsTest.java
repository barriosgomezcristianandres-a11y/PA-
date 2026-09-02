package com.isivi.app;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SeoAndPublicEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("1. robots.txt es público, responde 200 y protege rutas administrativas")
    public void testRobotsTxt() throws Exception {
        mockMvc.perform(get("/robots.txt"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User-agent: *")))
                .andExpect(content().string(containsString("Allow: /")))
                .andExpect(content().string(containsString("Disallow: /admin")))
                .andExpect(content().string(containsString("Disallow: /api/")))
                .andExpect(content().string(containsString("Sitemap: https://isivi-app.onrender.com/sitemap.xml")));
    }

    @Test
    @DisplayName("2. sitemap.xml es público, responde 200 y contiene la URL canónica principal")
    public void testSitemapXml() throws Exception {
        mockMvc.perform(get("/sitemap.xml"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<loc>https://isivi-app.onrender.com/</loc>")))
                .andExpect(content().string(containsString("<changefreq>weekly</changefreq>")));
    }

    @Test
    @DisplayName("3. index.html contiene meta tags SEO, Open Graph, Twitter Cards y Schema.org JSON-LD")
    public void testIndexHtmlSeoMetas() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<title id=\"site-title-tag\">ISIVI")))
                .andExpect(content().string(containsString("<meta name=\"description\"")))
                .andExpect(content().string(containsString("<link rel=\"canonical\" href=\"https://isivi-app.onrender.com/\">")))
                .andExpect(content().string(containsString("<meta property=\"og:title\"")))
                .andExpect(content().string(containsString("<meta property=\"og:image\"")))
                .andExpect(content().string(containsString("<meta name=\"twitter:card\"")))
                .andExpect(content().string(containsString("\"@type\": \"BeautySalon\"")))
                .andExpect(content().string(containsString("Cartagena")));
    }

    @Test
    @DisplayName("4. /api/agenda es público y provee la fuente de verdad para horarios de atención")
    public void testAgendaPublica() throws Exception {
        mockMvc.perform(get("/api/agenda"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diasLaborales").isArray())
                .andExpect(jsonPath("$.horarios").isArray());
    }

    @Test
    @DisplayName("5. Schema.org JSON-LD en index.html es exacto y consistente con datos reales del negocio")
    public void testSchemaOrgExactitud() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"isivi-schema-jsonld\"")))
                .andExpect(content().string(containsString("\"Sunday\"")))
                .andExpect(content().string(containsString("\"Tuesday\"")))
                .andExpect(content().string(not(containsString("\"Monday\"")))) // Lunes cerrado omitido
                .andExpect(content().string(containsString("\"opens\": \"08:00\"")))
                .andExpect(content().string(containsString("\"closes\": \"19:00\"")))
                .andExpect(content().string(containsString("\"paymentAccepted\": \"Wompi, Transferencia Bancolombia, Transferencia Nequi, Transferencia Daviplata\"")));
    }

    @Autowired
    private com.isivi.app.repository.ReservaRepository publicReservaRepository;

    @Test
    @DisplayName("6. /api/reservas/consultar es público pero no expone PII ni secretos financieros en JSON")
    public void testConsultarNoExponePIINiSecretos() throws Exception {
        com.isivi.app.model.Reserva r = new com.isivi.app.model.Reserva();
        r.setNombreCliente("Cliente Secreto");
        r.setTelefono("3009998888");
        r.setEmail("secreto@isivi.com");
        r.setDireccionEntrega("Calle Falsa 123");
        r.setCodigoReserva("ISV-SEC-99");
        r.setReferenciaWompi("REF-SEC-99");
        r.setTransaccionWompiId("TX-SEC-99");
        r.setEstado("Pendiente Comprobante");
        publicReservaRepository.save(r);

        try {
            mockMvc.perform(get("/api/reservas/consultar?query=3009998888"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombreCliente").value("Cliente Secreto"))
                    .andExpect(jsonPath("$.codigoReserva").value("ISV-SEC-99"))
                    // Validar que NO contiene campos sensibles
                    .andExpect(jsonPath("$.email").doesNotExist())
                    .andExpect(jsonPath("$.telefono").doesNotExist())
                    .andExpect(jsonPath("$.direccionEntrega").doesNotExist())
                    .andExpect(jsonPath("$.referenciaWompi").doesNotExist())
                    .andExpect(jsonPath("$.transaccionWompiId").doesNotExist());
        } finally {
            publicReservaRepository.delete(r);
        }
    }
}
