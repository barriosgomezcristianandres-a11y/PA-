package com.isivi.app;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.isivi.app.config.CloudinaryConfig;
import com.isivi.app.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Cloudinary cloudinary;

    @MockBean
    private CloudinaryConfig cloudinaryConfig;

    private String validAdminToken;

    @BeforeEach
    void setUp() {
        validAdminToken = jwtService.generarToken("admin");
        // Por defecto, configurar como disponible
        when(cloudinaryConfig.configurado()).thenReturn(true);
    }

    @Test
    @DisplayName("1. POST /api/uploads/imagen - Exitoso con Mock Cloudinary")
    void testUploadImagenSuccess() throws Exception {
        Uploader mockUploader = Mockito.mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(mockUploader);
        when(mockUploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/test-cloud/image/upload/sample.jpg"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "jpeg-image-bytes".getBytes()
        );

        mockMvc.perform(multipart("/api/uploads/imagen")
                        .file(file)
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.secure_url").value("https://res.cloudinary.com/test-cloud/image/upload/sample.jpg"));
    }

    @Test
    @DisplayName("2. POST /api/uploads/imagen - Rechazar archivo vacío")
    void testUploadImagenEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/api/uploads/imagen")
                        .file(emptyFile)
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("3. POST /api/uploads/imagen - Rechazar archivo mayor a 5MB")
    void testUploadImagenTooLarge() throws Exception {
        byte[] largeBytes = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large.png",
                MediaType.IMAGE_PNG_VALUE,
                largeBytes
        );

        mockMvc.perform(multipart("/api/uploads/imagen")
                        .file(largeFile)
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.mensaje").value("La imagen no puede superar los 5 MB."));
    }

    @Test
    @DisplayName("4. POST /api/uploads/imagen - Rechazar tipo de archivo no permitido")
    void testUploadImagenInvalidType() throws Exception {
        MockMultipartFile txtFile = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "some-text".getBytes()
        );

        mockMvc.perform(multipart("/api/uploads/imagen")
                        .file(txtFile)
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.mensaje").value("Solo se permiten imágenes JPG, PNG o WebP."));
    }

    @Test
    @DisplayName("5. POST /api/uploads/imagen - Responder 503 si Cloudinary no está configurado")
    void testUploadImagenNotConfigured() throws Exception {
        when(cloudinaryConfig.configurado()).thenReturn(false);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "jpeg-image-bytes".getBytes()
        );

        mockMvc.perform(multipart("/api/uploads/imagen")
                        .file(file)
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("CONFIG_ERROR"));
    }

    @Test
    @DisplayName("6. POST /api/uploads/log-fallback - Registrar logs sin autenticación JWT")
    void testLogFallbackPublicAccess() throws Exception {
        Map<String, String> payload = Map.of(
                "motivo", "Cloudinary desconfigurado de prueba",
                "detalle", "Simulación de error en test"
        );

        mockMvc.perform(post("/api/uploads/log-fallback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
    }
}
