package com.isivi.app.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.isivi.app.config.CloudinaryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    private CloudinaryConfig cloudinaryConfig;

    @Autowired(required = false)
    private Cloudinary cloudinary;

    @PostMapping("/imagen")
    public ResponseEntity<?> subirImagen(@RequestParam("file") MultipartFile file) {
        if (!cloudinaryConfig.configurado() || cloudinary == null) {
            logger.warn("Falla en la subida: Cloudinary no está configurado.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "CONFIG_ERROR", "mensaje", "Cloudinary no está configurado en el servidor."));
        }

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "VALIDATION_ERROR", "mensaje", "El archivo de imagen no puede estar vacío."));
        }

        // Validar tamaño máximo 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "VALIDATION_ERROR", "mensaje", "La imagen no puede superar los 5 MB."));
        }

        // Validar tipo de contenido
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equalsIgnoreCase("image/jpeg") 
                && !contentType.equalsIgnoreCase("image/jpg") 
                && !contentType.equalsIgnoreCase("image/png") 
                && !contentType.equalsIgnoreCase("image/webp"))) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "VALIDATION_ERROR", "mensaje", "Solo se permiten imágenes JPG, PNG o WebP."));
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "isivi/productos"
            ));
            String secureUrl = (String) uploadResult.get("secure_url");
            return ResponseEntity.ok(Map.of("secure_url", secureUrl));
        } catch (IOException e) {
            logger.error("Error al subir archivo a Cloudinary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "UPLOAD_ERROR", "mensaje", "Error al subir la imagen a Cloudinary: " + e.getMessage()));
        }
    }

    @PostMapping("/log-fallback")
    public ResponseEntity<?> registrarFallback(@RequestBody Map<String, String> payload) {
        String motivo = payload.getOrDefault("motivo", "No especificado");
        String detalle = payload.getOrDefault("detalle", "");
        logger.warn("FALLBACK A BASE64 DETECTADO EN EL CLIENTE - Motivo: {} - Detalle: {}", motivo, detalle);
        return ResponseEntity.ok().build();
    }
}
