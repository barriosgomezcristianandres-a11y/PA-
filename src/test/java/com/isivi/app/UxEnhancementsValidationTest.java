package com.isivi.app;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UxEnhancementsValidationTest {

    private String readClasspathFile(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    @DisplayName("1. index.html contiene la barra contextual móvil con soporte de accesibilidad y safe-area")
    public void testIndexHtmlMobileContextBar() throws Exception {
        String html = readClasspathFile("static/index.html");
        assertTrue(html.contains("id=\"mobile-context-bar\""), "Debe existir el elemento #mobile-context-bar");
        assertTrue(html.contains("id=\"mobile-context-label\""), "Debe existir el label contextual");
        assertTrue(html.contains("id=\"mobile-context-subtext\""), "Debe existir el subtexto contextual");
        assertTrue(html.contains("id=\"mobile-context-btn\""), "Debe existir el botón de acción");
        assertTrue(html.contains("onclick=\"handleMobileContextAction()\""), "Debe invocar handleMobileContextAction()");
        assertTrue(html.contains("aria-label=\"Barra de acción contextual\""), "Debe tener aria-label descriptivo");
    }

    @Test
    @DisplayName("2. index.html unifica la reserva dentro del carrito y no contiene seccion de calendario en la pagina principal")
    public void testIndexHtmlBookingProgressStepperInCartDrawer() throws Exception {
        String html = readClasspathFile("static/index.html");

        // El drawer del carrito debe ser el centro del proceso con el stepper, desglose y calendario compacto
        assertTrue(html.contains("id=\"cart-drawer\""), "Debe existir #cart-drawer");
        assertTrue(html.contains("id=\"booking-progress-stepper\""), "Debe existir #booking-progress-stepper dentro del carrito");
        assertTrue(html.contains("id=\"cart-service-financial-card\""), "Debe existir #cart-service-financial-card");
        assertTrue(html.contains("id=\"cart-schedule-section\""), "Debe existir #cart-schedule-section dentro del carrito");
        assertTrue(html.contains("id=\"cart-calendar-days-grid\""), "Debe existir #cart-calendar-days-grid");
        assertTrue(html.contains("id=\"cart-time-slots\""), "Debe existir #cart-time-slots");

        // La página principal ya no debe tener la sección grande de reserva ni el título duplicado
        assertFalse(html.contains("id=\"reserva\""), "No debe existir <section id=\"reserva\"> en la página principal");
        assertFalse(html.contains("Calendario de Disponibilidad Real"), "No debe contener el bloque 'Calendario de Disponibilidad Real' en la página principal");
        assertFalse(html.contains("Paso 2 de 3"), "No debe contener el texto 'Paso 2 de 3'");
        
        // Elementos semánticos del stepper dentro del carrito
        assertTrue(html.contains("id=\"booking-progress-fill\""), "Debe existir la barra de relleno");
        assertTrue(html.contains("id=\"step-node-service\""), "Debe existir el nodo Servicio");
        assertTrue(html.contains("id=\"step-node-date\""), "Debe existir el nodo Fecha");
        assertTrue(html.contains("id=\"step-node-time\""), "Debe existir el nodo Hora");
        assertTrue(html.contains("id=\"step-node-data\""), "Debe existir el nodo Datos");
        assertTrue(html.contains("id=\"step-node-confirm\""), "Debe existir el nodo Confirmar");
    }

    @Test
    @DisplayName("3. isivi.css contiene los estilos del stepper, barra contextual y animaciones accesibles")
    public void testIsiviCssStylesAndAccessibility() throws Exception {
        String css = readClasspathFile("static/css/isivi.css");
        assertTrue(css.contains(".isivi-mobile-context-bar"), "Debe contener la clase .isivi-mobile-context-bar");
        assertTrue(css.contains("safe-area-inset-bottom"), "Debe respetar safe-area-inset-bottom para iOS");
        assertTrue(css.contains(".isivi-progress-stepper"), "Debe contener estilos para el stepper");
        assertTrue(css.contains(".isivi-progress-fill"), "Debe contener estilos para la barra de progreso");
        assertTrue(css.contains(".animate-badge-pop"), "Debe contener la animación de pulso del carrito");
        assertTrue(css.contains("prefers-reduced-motion: reduce"), "Debe respetar la directiva prefers-reduced-motion");
    }

    @Test
    @DisplayName("4. isivi.js implementa el cálculo en tiempo real de progreso, barra contextual y feedback")
    public void testIsiviJsFunctions() throws Exception {
        String js = readClasspathFile("static/js/isivi.js");
        assertTrue(js.contains("function updateBookingProgressTracker()"), "Debe implementar updateBookingProgressTracker");
        assertTrue(js.contains("function updateMobileContextBar()"), "Debe implementar updateMobileContextBar");
        assertTrue(js.contains("function handleMobileContextAction()"), "Debe implementar handleMobileContextAction");
        assertTrue(js.contains("animate-badge-pop"), "Debe animar el badge de carrito en addItemToCart");
        assertTrue(js.contains("window.addEventListener('scroll', updateMobileContextBar"), "Debe escuchar evento scroll");
        assertTrue(js.contains("window.addEventListener('resize', () =>"), "Debe escuchar evento resize");
    }

    @Test
    @DisplayName("5. index.html e isivi.js garantizan la integridad del catalogo de servicios, productos, kits y null-safety")
    public void testCatalogIntegrityAndNullSafety() throws Exception {
        String html = readClasspathFile("static/index.html");
        assertTrue(html.contains("id=\"services-grid\""), "Debe existir #services-grid");
        assertTrue(html.contains("id=\"products-grid\""), "Debe existir #products-grid");
        assertTrue(html.contains("id=\"kits-grid\""), "Debe existir #kits-grid");
        assertTrue(html.contains("id=\"service-category-tabs\""), "Debe existir #service-category-tabs");
        assertTrue(html.contains("id=\"client-banners\""), "Debe existir #client-banners");
        assertTrue(html.contains("Agenda tus servicios de peluquería con el <strong class=\"text-isivi-gold font-semibold\">25% de anticipo</strong>"), "El héroe debe atribuir el 25% de anticipo a los servicios de peluquería");

        String js = readClasspathFile("static/js/isivi.js");
        assertTrue(js.contains("function renderServicesGrid()"), "Debe existir renderServicesGrid");
        assertTrue(js.contains("function renderProductsGrid()"), "Debe existir renderProductsGrid");
        assertTrue(js.contains("function renderKitsGrid()"), "Debe existir renderKitsGrid");
        assertTrue(js.contains("function loadPublicData()"), "Debe existir loadPublicData");
        assertTrue(js.contains("renderCartCalendar()"), "renderCalendar debe sincronizar el calendario del carrito");
        assertTrue(js.contains("renderCartSchedule()"), "renderTimeSlots debe sincronizar los turnos del carrito");
    }

    @Test
    @DisplayName("6. isivi.js implementa sincronización integral de carrito, estado centralizado y preservación ante cancelación de Wompi")
    public void testCartSynchronizationAndWompiPreservation() throws Exception {
        String js = readClasspathFile("static/js/isivi.js");
        assertTrue(js.contains("function syncCartState()"), "Debe existir syncCartState()");
        assertTrue(js.contains("function resetCartState()"), "Debe existir resetCartState()");
        assertTrue(js.contains("function getCartTotalUnits()"), "Debe existir getCartTotalUnits()");
        assertTrue(js.contains("function getCartTotalAmount()"), "Debe existir getCartTotalAmount()");
        assertTrue(js.contains("function updateCartBadge()"), "Debe existir updateCartBadge()");

        // Sincronización en loadPublicData y checkWompiRedirectReturn
        assertTrue(js.contains("syncCartState();"), "loadPublicData y DOMContentLoaded deben invocar syncCartState()");

        // Preservación en Wompi: no vaciar antes del resultado, solo limpiar en APPROVED
        assertTrue(js.contains("if (status === 'APPROVED')"), "Debe validar estado APPROVED en callback de Wompi");
        assertTrue(js.contains("resetCartState();"), "Debe limpiar carrito solo tras pago APPROVED");
        assertTrue(js.contains("syncCartState();"), "Debe sincronizar carrito ante DECLINED o PENDING");
    }

    @Test
    @DisplayName("7. index.html, isivi.css e isivi.js coordinan geométricamente el botón flotante de WhatsApp con la barra contextual")
    public void testWhatsAppFloatMobileCoordination() throws Exception {
        String html = readClasspathFile("static/index.html");
        assertTrue(html.contains("id=\"whatsapp-float\""), "index.html debe definir id=\"whatsapp-float\"");
        assertTrue(html.contains("isivi-whatsapp-float"), "index.html debe tener clase isivi-whatsapp-float");

        String css = readClasspathFile("static/css/isivi.css");
        assertTrue(css.contains(".isivi-whatsapp-float"), "isivi.css debe estilizar .isivi-whatsapp-float");
        assertTrue(css.contains("body.has-mobile-context-bar .isivi-whatsapp-float"), "isivi.css debe desplazar WhatsApp hacia arriba cuando la barra está visible");
        assertTrue(css.contains("calc(4.75rem + env(safe-area-inset-bottom, 0px))"), "isivi.css debe considerar la altura de la barra + safe-area");

        String js = readClasspathFile("static/js/isivi.js");
        assertTrue(js.contains("document.body.classList.add('has-mobile-context-bar')"), "isivi.js debe añadir clase has-mobile-context-bar cuando la barra se muestra");
        assertTrue(js.contains("document.body.classList.remove('has-mobile-context-bar')"), "isivi.js debe remover clase has-mobile-context-bar cuando la barra se oculta");
    }

    @Test
    @DisplayName("8. index.html, isivi.css e isivi.js implementan el modal de detalle 'Ver más' unificado para productos, kits y servicios con detección de desbordamiento de 3 líneas")
    public void testVerMasDetailModalImplementation() throws Exception {
        String html = readClasspathFile("static/index.html");
        assertTrue(html.contains("id=\"product-detail-modal\""), "index.html debe definir #product-detail-modal");
        assertTrue(html.indexOf("id=\"product-detail-modal\"") < html.indexOf("id=\"page-admin\""), "index.html debe ubicar #product-detail-modal a nivel global fuera de #page-admin");
        assertTrue(html.contains("id=\"product-detail-duration-badge\""), "index.html debe incluir #product-detail-duration-badge para servicios");
        assertTrue(html.contains("id=\"product-detail-deposit-container\""), "index.html debe incluir #product-detail-deposit-container para anticipo de servicios");
        assertTrue(html.contains("id=\"product-detail-kit-badge\""), "index.html debe incluir #product-detail-kit-badge para kits");

        String css = readClasspathFile("static/css/isivi.css");
        assertTrue(css.contains(".isivi-line-clamp-3"), "isivi.css debe incluir .isivi-line-clamp-3");
        assertTrue(css.contains(".isivi-ver-mas-btn"), "isivi.css debe incluir .isivi-ver-mas-btn");

        String js = readClasspathFile("static/js/isivi.js");
        assertTrue(js.contains("function updateVerMasVisibility(type, items)"), "isivi.js debe implementar updateVerMasVisibility");
        assertTrue(js.contains("descEl.scrollHeight > descEl.clientHeight + 1"), "isivi.js debe medir desbordamiento mediante scrollHeight > clientHeight");
        assertTrue(js.contains("function openItemDetailModal(type, itemId)"), "isivi.js debe implementar openItemDetailModal unificado");
        assertTrue(js.contains("String(p.id) === targetIdStr"), "isivi.js debe usar coerción de String para garantizar coincidencia entre IDs numéricos del API y strings del DOM");
        assertTrue(js.contains("function openProductDetailModal(productId)"), "isivi.js debe mantener facade openProductDetailModal");
        assertTrue(js.contains("function openKitDetailModal(kitId)"), "isivi.js debe implementar facade openKitDetailModal");
        assertTrue(js.contains("function openServiceDetailModal(serviceId)"), "isivi.js debe implementar facade openServiceDetailModal");
        assertTrue(js.contains("addDetailServiceToCart"), "isivi.js debe vincular el agendamiento del modal al flujo de servicios");
    }
}



