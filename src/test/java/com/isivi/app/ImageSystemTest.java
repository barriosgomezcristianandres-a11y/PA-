package com.isivi.app;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.isivi.app.controller.MigrationController;
import com.isivi.app.model.Banner;
import com.isivi.app.model.Kit;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Servicio;
import com.isivi.app.repository.BannerRepository;
import com.isivi.app.repository.KitRepository;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ServicioRepository;
import com.isivi.app.util.ImageHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest
@org.springframework.test.context.TestPropertySource(properties = {
    "isivi.cloudinary.cloud-name=test-cloud",
    "isivi.cloudinary.api-key=test-key",
    "isivi.cloudinary.api-secret=test-secret"
})
public class ImageSystemTest {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private KitRepository kitRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private BannerRepository bannerRepository;

    @Autowired
    private MigrationController migrationController;

    @MockBean
    private Cloudinary cloudinary;

    private Uploader mockUploader;

    private static final String VALID_BASE64_1 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
    private static final String VALID_BASE64_2 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggA==";
    private static final String VALID_BASE64_3 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggQ==";

    @BeforeEach
    void setUp() {
        productoRepository.deleteAll();
        kitRepository.deleteAll();
        servicioRepository.deleteAll();
        bannerRepository.deleteAll();

        mockUploader = Mockito.mock(Uploader.class);
        Mockito.when(cloudinary.uploader()).thenReturn(mockUploader);
    }

    @Test
    @DisplayName("1-3: Clasificación correcta de orígenes de imagen")
    void testClasificacionOrigenes() {
        // 1. URL Cloudinary -> CLOUDINARY
        assertEquals(ImageHelper.CLOUDINARY, ImageHelper.clasificarOrigen("https://res.cloudinary.com/isivi/image/upload/v12345/prod.jpg"));
        
        // 2. Base64 -> BASE64
        assertEquals(ImageHelper.BASE64, ImageHelper.clasificarOrigen(VALID_BASE64_1));

        // 3. URL desconocida -> UNKNOWN
        assertEquals(ImageHelper.UNKNOWN, ImageHelper.clasificarOrigen("https://images.google.com/otro.png"));
        assertEquals(ImageHelper.UNKNOWN, ImageHelper.clasificarOrigen(""));
        assertEquals(ImageHelper.UNKNOWN, ImageHelper.clasificarOrigen(null));
    }

    @Test
    @DisplayName("4: Migración exitosa de Base64 a Cloudinary")
    void testMigracionExitosa() throws Exception {
        Producto p = new Producto("Champú", 15000.0, "Detalle", VALID_BASE64_1, true);
        p.setOrigenImagen(ImageHelper.BASE64);
        productoRepository.save(p);

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://res.cloudinary.com/isivi/image/upload/v123/champ.jpg");
        Mockito.when(mockUploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        MigrationController.MigrationRequest req = new MigrationController.MigrationRequest();
        req.setDryRun(false);

        ResponseEntity<?> response = migrationController.migrarImagenes(req);
        assertEquals(200, response.getStatusCode().value());

        Producto actualizado = productoRepository.findById(p.getId()).orElseThrow();
        assertEquals("https://res.cloudinary.com/isivi/image/upload/v123/champ.jpg", actualizado.getImagenUrl());
        assertEquals(ImageHelper.CLOUDINARY, actualizado.getOrigenImagen());
    }

    @Test
    @DisplayName("5: Migración fallida mantiene BASE64")
    void testMigracionFallidaMantieneBase64() throws Exception {
        Producto p = new Producto("Crema", 18000.0, "Detalle", VALID_BASE64_1, true);
        p.setOrigenImagen(ImageHelper.BASE64);
        productoRepository.save(p);

        Mockito.when(mockUploader.upload(any(byte[].class), anyMap())).thenThrow(new RuntimeException("Cloudinary error"));

        MigrationController.MigrationRequest req = new MigrationController.MigrationRequest();
        req.setDryRun(false);

        ResponseEntity<?> response = migrationController.migrarImagenes(req);
        assertEquals(200, response.getStatusCode().value());

        Producto actualizado = productoRepository.findById(p.getId()).orElseThrow();
        assertEquals(VALID_BASE64_1, actualizado.getImagenUrl());
        assertEquals(ImageHelper.BASE64, actualizado.getOrigenImagen());
    }

    @Test
    @DisplayName("6: No duplicar Cloudinary (ignora imágenes que ya son CLOUDINARY)")
    void testNoDuplicarCloudinary() throws Exception {
        Producto p = new Producto("Aceite", 25000.0, "Detalle", "https://res.cloudinary.com/isivi/image/upload/v123/aceite.jpg", true);
        p.setOrigenImagen(ImageHelper.CLOUDINARY);
        productoRepository.save(p);

        MigrationController.MigrationRequest req = new MigrationController.MigrationRequest();
        req.setDryRun(false);

        ResponseEntity<?> response = migrationController.migrarImagenes(req);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(0, body.get("productosMigrados"));

        Mockito.verify(mockUploader, Mockito.never()).upload(any(byte[].class), anyMap());
    }

    @Test
    @DisplayName("8: DryRun no modifica base de datos")
    void testDryRunNoModifica() throws Exception {
        Producto p = new Producto("Tónico", 30000.0, "Detalle", VALID_BASE64_1, true);
        p.setOrigenImagen(ImageHelper.BASE64);
        productoRepository.save(p);

        MigrationController.MigrationRequest req = new MigrationController.MigrationRequest();
        req.setDryRun(true);

        ResponseEntity<?> response = migrationController.migrarImagenes(req);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(1, body.get("productosPendientes"));
        assertEquals(0, body.get("productosMigrados"));

        Producto noModificado = productoRepository.findById(p.getId()).orElseThrow();
        assertEquals(VALID_BASE64_1, noModificado.getImagenUrl());
        assertEquals(ImageHelper.BASE64, noModificado.getOrigenImagen());
    }

    @Test
    @DisplayName("9: Migración parcial continúa aunque una imagen falle")
    void testMigracionParcialContinua() throws Exception {
        Producto p1 = new Producto("Falla", 10000.0, "Detalle", VALID_BASE64_2, true);
        p1.setOrigenImagen(ImageHelper.BASE64);
        productoRepository.save(p1);

        Producto p2 = new Producto("Exito", 15000.0, "Detalle", VALID_BASE64_3, true);
        p2.setOrigenImagen(ImageHelper.BASE64);
        productoRepository.save(p2);

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://res.cloudinary.com/isivi/image/upload/v123/exito.jpg");

        // mockUploader fallará para la primera subida y tendrá éxito para la segunda
        Mockito.when(mockUploader.upload(any(byte[].class), anyMap()))
                .thenThrow(new RuntimeException("Error Cloudinary"))
                .thenReturn(uploadResult);

        MigrationController.MigrationRequest req = new MigrationController.MigrationRequest();
        req.setDryRun(false);

        ResponseEntity<?> response = migrationController.migrarImagenes(req);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(1, body.get("productosMigrados"));
        assertEquals(1, body.get("productosFallidos"));

        Producto act1 = productoRepository.findById(p1.getId()).orElseThrow();
        assertEquals(ImageHelper.BASE64, act1.getOrigenImagen());

        Producto act2 = productoRepository.findById(p2.getId()).orElseThrow();
        assertEquals(ImageHelper.CLOUDINARY, act2.getOrigenImagen());
        assertEquals("https://res.cloudinary.com/isivi/image/upload/v123/exito.jpg", act2.getImagenUrl());
    }

    @Test
    @DisplayName("10-13: Conservar propiedades de imagen (zoom, posiciones y destino del Banner)")
    void testConservarPropiedadesEntidades() throws Exception {
        Banner b = new Banner("Promo", "Desc", VALID_BASE64_1, "Boton", "#destino", true);
        b.setOrigenImagen(ImageHelper.BASE64);
        b.setImageZoom(2.5);
        b.setImagePosX(20.0);
        b.setImagePosY(80.0);
        b.setTipoDestino("PRODUCTO");
        b.setDestinoId("prod-999");
        bannerRepository.save(b);

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://res.cloudinary.com/isivi/image/upload/v123/banner.jpg");
        Mockito.when(mockUploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        MigrationController.MigrationRequest req = new MigrationController.MigrationRequest();
        req.setDryRun(false);

        migrationController.migrarImagenes(req);

        Banner actBanner = bannerRepository.findById(b.getId()).orElseThrow();
        assertEquals("https://res.cloudinary.com/isivi/image/upload/v123/banner.jpg", actBanner.getImagenUrl());
        assertEquals(ImageHelper.CLOUDINARY, actBanner.getOrigenImagen());

        // Verificar que no se perdieron las propiedades
        assertEquals(2.5, actBanner.getImageZoom());
        assertEquals(20.0, actBanner.getImagePosX());
        assertEquals(80.0, actBanner.getImagePosY());
        assertEquals("PRODUCTO", actBanner.getTipoDestino());
        assertEquals("prod-999", actBanner.getDestinoId());
        assertEquals("Promo", actBanner.getTitulo());
        assertEquals("Desc", actBanner.getDescripcion());
        assertEquals("Boton", actBanner.getTextoBoton());
    }
}
