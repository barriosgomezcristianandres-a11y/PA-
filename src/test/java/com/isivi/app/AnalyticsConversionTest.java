package com.isivi.app;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AnalyticsConversionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceLoader resourceLoader;

    private String getJsContent() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:static/js/isivi.js");
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    private String getHtmlContent() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:static/index.html");
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("1. La capa de analítica centralizada trackEvent está implementada y es segura")
    void testAnalyticsLayerExistsInJs() throws Exception {
        String js = getJsContent();
        assertTrue(js.contains("function trackEvent("), "trackEvent debe estar definido en isivi.js");
        assertTrue(js.contains("window.dataLayer = window.dataLayer || [];"), "Debe soportar window.dataLayer para GTM/GA4");
        assertTrue(js.contains("window.dispatchEvent(new CustomEvent('isivi_analytics_event'"), "Debe emitir eventos personalizados para desacoplamiento");
    }

    @Test
    @DisplayName("2. Sanitización estricta de datos sensibles en analítica")
    void testSensitiveDataBlacklist() throws Exception {
        String js = getJsContent();
        assertTrue(js.contains("function sanitizeAnalyticsParams("), "Debe existir función de sanitización de analítica");
        assertTrue(js.contains("SENSITIVE_ANALYTICS_KEYS"), "Debe existir lista de exclusión estricta de claves sensibles");
        assertTrue(js.contains("'password'"), "Debe excluir password");
        assertTrue(js.contains("'card'"), "Debe excluir tarjeta");
        assertTrue(js.contains("'cvv'"), "Debe excluir cvv");
        assertTrue(js.contains("'phone'"), "Debe excluir teléfono");
        assertTrue(js.contains("'email'"), "Debe excluir email");
        assertTrue(js.contains("'address'"), "Debe excluir dirección");
    }

    @Test
    @DisplayName("3. Embudo de conversión completo instrumentado en JavaScript")
    void testConversionFunnelEventsIntegrity() throws Exception {
        String js = getJsContent();
        assertTrue(js.contains("trackEvent('page_view'"), "Debe incluir evento page_view");
        assertTrue(js.contains("trackEvent('add_to_cart'"), "Debe incluir evento add_to_cart");
        assertTrue(js.contains("trackEvent('remove_from_cart'"), "Debe incluir evento remove_from_cart");
        assertTrue(js.contains("trackEvent('select_date'"), "Debe incluir evento select_date");
        assertTrue(js.contains("trackEvent('select_time'"), "Debe incluir evento select_time");
        assertTrue(js.contains("trackEvent('begin_checkout'"), "Debe incluir evento begin_checkout");
        assertTrue(js.contains("trackEvent('select_payment_method'"), "Debe incluir evento select_payment_method");
        assertTrue(js.contains("trackEvent('payment_started'"), "Debe incluir evento payment_started");
        assertTrue(js.contains("trackEvent('payment_success'"), "Debe incluir evento payment_success");
        assertTrue(js.contains("trackEvent('payment_failure'"), "Debe incluir evento payment_failure");
        assertTrue(js.contains("trackEvent('purchase'"), "Debe incluir evento purchase");
        assertTrue(js.contains("trackEvent('reservation_confirmed'"), "Debe incluir evento reservation_confirmed");
        assertTrue(js.contains("trackEvent('whatsapp_click'"), "Debe incluir evento whatsapp_click");
        assertTrue(js.contains("trackEvent('reservation_lookup'"), "Debe incluir evento reservation_lookup");
        assertTrue(js.contains("trackEvent('reservation_rescheduled'"), "Debe incluir evento reservation_rescheduled");
        assertTrue(js.contains("trackEvent('reservation_cancelled'"), "Debe incluir evento reservation_cancelled");
    }

    @Test
    @DisplayName("4. Semántica monetaria: payment_success y purchase usan paidAmount real (25% servicios, 100% productos)")
    void testPaymentSuccessAndPurchaseMonetarySemantics() throws Exception {
        String js = getJsContent();
        assertTrue(js.contains("const paidAmount = hasServiceBooking ? deposit : total;"), "paidAmount debe diferenciar entre anticipo de servicios y total de productos");
        assertTrue(js.contains("value: paidAmount"), "payment_success y purchase deben usar paidAmount");
        assertTrue(js.contains("currency: 'COP'"), "La moneda debe ser COP");
        assertTrue(js.contains("transaction_id: booking.code"), "purchase debe usar transaction_id no sensible");
    }

    @Test
    @DisplayName("5. Conexión GA4 preparada de forma no bloqueante y resiliente")
    void testGA4LoaderResilience() throws Exception {
        String js = getJsContent();
        assertTrue(js.contains("function initGA4IfConfigured("), "Debe existir cargador no bloqueante de GA4");
        assertTrue(js.contains("initGA4IfConfigured();"), "Debe invocarse al cargar el DOM");
        String html = getHtmlContent();
        assertTrue(html.contains("Google Search Console & Google Analytics 4 (Preparado"), "index.html debe tener comentarios preparados");
    }

    @Test
    @DisplayName("6. Idempotencia y prevención de duplicados en eventos de conversión")
    void testAnalyticsIdempotencyMechanism() throws Exception {
        String js = getJsContent();
        assertTrue(js.contains("trackedUniqueAnalyticsEvents"), "Debe existir Set de deduplicación para eventos únicos");
        assertTrue(js.contains("initial_page_load"), "page_view debe tener clave única de carga inicial");
    }

    @Test
    @DisplayName("7. Enlaces de WhatsApp en index.html contienen tracking contextual no sensible")
    void testHtmlWhatsAppTracking() throws Exception {
        String html = getHtmlContent();
        assertTrue(html.contains("trackEvent('whatsapp_click', { context: 'floating_chat' })"), "Botón flotante debe medir context=floating_chat");
        assertTrue(html.contains("trackEvent('whatsapp_click', { context: 'wholesale_inquiry' })"), "Botón mayorista debe medir context=wholesale_inquiry");
    }

    @Test
    @DisplayName("8. Search Console / SEO público: sitemap.xml y robots.txt accesibles con HTTP 200")
    void testSearchConsoleSitemapAndRobotsAccessibility() throws Exception {
        mockMvc.perform(get("/robots.txt"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sitemap: https://isivi-app.onrender.com/sitemap.xml")));

        mockMvc.perform(get("/sitemap.xml"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<loc>https://isivi-app.onrender.com/</loc>")));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }
}
