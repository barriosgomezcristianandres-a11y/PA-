package com.isivi.app.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.isivi.app.config.CloudinaryConfig;
import com.isivi.app.model.Kit;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Servicio;
import com.isivi.app.model.Banner;
import com.isivi.app.repository.KitRepository;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ServicioRepository;
import com.isivi.app.repository.BannerRepository;
import com.isivi.app.util.ImageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class MigrationController {

    private static final Logger logger = LoggerFactory.getLogger(MigrationController.class);

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private KitRepository kitRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private BannerRepository bannerRepository;

    @Autowired
    private CloudinaryConfig cloudinaryConfig;

    @Autowired(required = false)
    private Cloudinary cloudinary;

    public static class MigrationRequest {
        private Boolean dryRun = true;
        public Boolean getDryRun() { return dryRun != null ? dryRun : true; }
        public void setDryRun(Boolean dryRun) { this.dryRun = dryRun; }
    }

    @PostMapping("/migrar-imagenes")
    public ResponseEntity<?> migrarImagenes(@RequestBody(required = false) MigrationRequest request) {
        MigrationRequest req = request != null ? request : new MigrationRequest();
        boolean dryRun = req.getDryRun();

        if (!dryRun && (!cloudinaryConfig.configurado() || cloudinary == null)) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "CONFIG_ERROR", "mensaje", "Cloudinary no está configurado. No se puede realizar la migración real."));
        }

        List<String> reportLogs = new ArrayList<>();
        
        int productsToMigrate = 0;
        int productsMigrated = 0;
        int productsFailed = 0;

        int kitsToMigrate = 0;
        int kitsMigrated = 0;
        int kitsFailed = 0;

        int servicesToMigrate = 0;
        int servicesMigrated = 0;
        int servicesFailed = 0;

        int bannersToMigrate = 0;
        int bannersMigrated = 0;
        int bannersFailed = 0;

        // 1. Procesar Productos
        List<Producto> productos = productoRepository.findAll();
        for (Producto p : productos) {
            if (ImageHelper.BASE64.equals(p.getOrigenImagen())) {
                productsToMigrate++;
                if (dryRun) {
                    reportLogs.add("[DRY-RUN] Producto '" + p.getNombre() + "' (ID: " + p.getId() + ") tiene imagen base64 de tamaño aprox " + p.getImagenUrl().length() + " caracteres. Sería migrado.");
                } else {
                    try {
                        String secureUrl = subirBase64ACloudinary(p.getImagenUrl(), p.getNombre());
                        p.setImagenUrl(secureUrl);
                        p.setOrigenImagen(ImageHelper.CLOUDINARY);
                        productoRepository.save(p);
                        productsMigrated++;
                        reportLogs.add("Producto '" + p.getNombre() + "' (ID: " + p.getId() + ") migrado exitosamente. Nueva URL: " + secureUrl);
                        logger.info("Migración exitosa: Producto ID {} migrado a {}", p.getId(), secureUrl);
                    } catch (Exception e) {
                        productsFailed++;
                        p.setOrigenImagen(ImageHelper.BASE64);
                        productoRepository.save(p);
                        reportLogs.add("Error al migrar Producto '" + p.getNombre() + "' (ID: " + p.getId() + "): " + e.getMessage());
                        logger.error("Error al migrar Producto ID " + p.getId(), e);
                    }
                }
            }
        }

        // 2. Procesar Kits
        List<Kit> kits = kitRepository.findAll();
        for (Kit k : kits) {
            if (ImageHelper.BASE64.equals(k.getOrigenImagen())) {
                kitsToMigrate++;
                if (dryRun) {
                    reportLogs.add("[DRY-RUN] Kit '" + k.getNombre() + "' (ID: " + k.getId() + ") tiene imagen base64 de tamaño aprox " + k.getImagenUrl().length() + " caracteres. Sería migrado.");
                } else {
                    try {
                        String secureUrl = subirBase64ACloudinary(k.getImagenUrl(), k.getNombre());
                        k.setImagenUrl(secureUrl);
                        k.setOrigenImagen(ImageHelper.CLOUDINARY);
                        kitRepository.save(k);
                        kitsMigrated++;
                        reportLogs.add("Kit '" + k.getNombre() + "' (ID: " + k.getId() + ") migrado exitosamente. Nueva URL: " + secureUrl);
                        logger.info("Migración exitosa: Kit ID {} migrado a {}", k.getId(), secureUrl);
                    } catch (Exception e) {
                        kitsFailed++;
                        k.setOrigenImagen(ImageHelper.BASE64);
                        kitRepository.save(k);
                        reportLogs.add("Error al migrar Kit '" + k.getNombre() + "' (ID: " + k.getId() + "): " + e.getMessage());
                        logger.error("Error al migrar Kit ID " + k.getId(), e);
                    }
                }
            }
        }

        // 3. Procesar Servicios
        List<Servicio> servicios = servicioRepository.findAll();
        for (Servicio s : servicios) {
            if (ImageHelper.BASE64.equals(s.getOrigenImagen())) {
                servicesToMigrate++;
                if (dryRun) {
                    reportLogs.add("[DRY-RUN] Servicio '" + s.getNombre() + "' (ID: " + s.getId() + ") tiene imagen base64 de tamaño aprox " + s.getImagenUrl().length() + " caracteres. Sería migrado.");
                } else {
                    try {
                        String secureUrl = subirBase64ACloudinary(s.getImagenUrl(), s.getNombre());
                        s.setImagenUrl(secureUrl);
                        s.setOrigenImagen(ImageHelper.CLOUDINARY);
                        servicioRepository.save(s);
                        servicesMigrated++;
                        reportLogs.add("Servicio '" + s.getNombre() + "' (ID: " + s.getId() + ") migrado exitosamente. Nueva URL: " + secureUrl);
                        logger.info("Migración exitosa: Servicio ID {} migrado a {}", s.getId(), secureUrl);
                    } catch (Exception e) {
                        servicesFailed++;
                        s.setOrigenImagen(ImageHelper.BASE64);
                        servicioRepository.save(s);
                        reportLogs.add("Error al migrar Servicio '" + s.getNombre() + "' (ID: " + s.getId() + "): " + e.getMessage());
                        logger.error("Error al migrar Servicio ID " + s.getId(), e);
                    }
                }
            }
        }

        // 4. Procesar Banners
        List<Banner> banners = bannerRepository.findAll();
        for (Banner b : banners) {
            if (ImageHelper.BASE64.equals(b.getOrigenImagen())) {
                bannersToMigrate++;
                if (dryRun) {
                    reportLogs.add("[DRY-RUN] Banner '" + b.getTitulo() + "' (ID: " + b.getId() + ") tiene imagen base64 de tamaño aprox " + b.getImagenUrl().length() + " caracteres. Sería migrado.");
                } else {
                    try {
                        String secureUrl = subirBase64ACloudinary(b.getImagenUrl(), b.getTitulo());
                        b.setImagenUrl(secureUrl);
                        b.setOrigenImagen(ImageHelper.CLOUDINARY);
                        bannerRepository.save(b);
                        bannersMigrated++;
                        reportLogs.add("Banner '" + b.getTitulo() + "' (ID: " + b.getId() + ") migrado exitosamente. Nueva URL: " + secureUrl);
                        logger.info("Migración exitosa: Banner ID {} migrado a {}", b.getId(), secureUrl);
                    } catch (Exception e) {
                        bannersFailed++;
                        b.setOrigenImagen(ImageHelper.BASE64);
                        bannerRepository.save(b);
                        reportLogs.add("Error al migrar Banner '" + b.getTitulo() + "' (ID: " + b.getId() + "): " + e.getMessage());
                        logger.error("Error al migrar Banner ID " + b.getId(), e);
                    }
                }
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("dryRun", dryRun);
        
        response.put("productosPendientes", productsToMigrate);
        response.put("productosMigrados", productsMigrated);
        response.put("productosFallidos", productsFailed);
        
        response.put("kitsPendientes", kitsToMigrate);
        response.put("kitsMigrados", kitsMigrated);
        response.put("kitsFallidos", kitsFailed);

        response.put("serviciosPendientes", servicesToMigrate);
        response.put("serviciosMigrados", servicesMigrated);
        response.put("serviciosFallidos", servicesFailed);

        response.put("bannersPendientes", bannersToMigrate);
        response.put("bannersMigrados", bannersMigrated);
        response.put("bannersFallidos", bannersFailed);

        response.put("totalPendientes", productsToMigrate + kitsToMigrate + servicesToMigrate + bannersToMigrate);
        response.put("logs", reportLogs);

        return ResponseEntity.ok(response);
    }

    private String subirBase64ACloudinary(String base64DataUri, String nombreEntidad) throws Exception {
        int commaIndex = base64DataUri.indexOf(",");
        if (commaIndex == -1) {
            throw new IllegalArgumentException("Formato base64 de imagen no válido.");
        }
        String base64Data = base64DataUri.substring(commaIndex + 1).trim();
        byte[] bytes = Base64.getDecoder().decode(base64Data);

        Map<?, ?> uploadResult = cloudinary.uploader().upload(bytes, ObjectUtils.asMap(
                "folder", "isivi/productos",
                "public_id", UUID.randomUUID().toString() + "_" + nombreEntidad.replaceAll("[^a-zA-Z0-9]", "_")
        ));
        return (String) uploadResult.get("secure_url");
    }
}
