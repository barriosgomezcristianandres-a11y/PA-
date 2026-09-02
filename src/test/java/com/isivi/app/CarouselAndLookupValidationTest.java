package com.isivi.app;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CarouselAndLookupValidationTest {

    private String readClasspathFile(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        return new String(Files.readAllBytes(Paths.get(resource.getURI())));
    }

    @Test
    @DisplayName("1. isivi.js contains the fixed hasAppointment mapping and local scope declaration")
    public void testLookupFixIntegrity() throws Exception {
        String js = readClasspathFile("static/js/isivi.js");
        
        // Assert mapFromApiReserva maps hasAppointment
        assertTrue(js.contains("hasAppointment: hasAppointment"), 
                "mapFromApiReserva must return hasAppointment");
        
        // Assert handleLookupSubmit declares hasAppointment in local scope
        assertTrue(js.contains("const hasAppointment = currentLookupBooking.hasAppointment;"), 
                "handleLookupSubmit must declare hasAppointment in local scope");
    }

    @Test
    @DisplayName("2. isivi.js implements premium carousel rendering and controller lifecycle functions")
    public void testCarouselJavascriptIntegrity() throws Exception {
        String js = readClasspathFile("static/js/isivi.js");

        assertTrue(js.contains("function cleanupBannersCarousel()"), 
                "cleanupBannersCarousel must be defined");
        assertTrue(js.contains("function initializeBannersCarousel(N)"), 
                "initializeBannersCarousel must be defined");
        assertTrue(js.contains("function renderClientBanners()"), 
                "renderClientBanners must be defined");
        assertTrue(js.contains("let carouselState = {"), 
                "carouselState object must be defined");
        assertTrue(js.contains("startAutoplay()"), 
                "startAutoplay function must be called/defined");
        assertTrue(js.contains("stopAutoplay()"), 
                "stopAutoplay function must be called/defined");
        assertTrue(js.contains("moveToSlide("), 
                "moveToSlide function must be defined");
    }

    @Test
    @DisplayName("3. isivi.css contains the required styles for viewport, slides and indicators")
    public void testCarouselCssStyles() throws Exception {
        String css = readClasspathFile("static/css/isivi.css");

        assertTrue(css.contains("#client-banners .carousel-viewport"), 
                "isivi.css must contain .carousel-viewport styles");
        assertTrue(css.contains("#client-banners .carousel-slide"), 
                "isivi.css must contain .carousel-slide styles");
        assertTrue(css.contains("#client-banners .carousel-indicator"), 
                "isivi.css must contain .carousel-indicator styles");
        assertTrue(css.contains("#client-banners .indicator-progress"), 
                "isivi.css must contain .indicator-progress styles");
    }
}
