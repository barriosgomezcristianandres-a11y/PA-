package com.isivi.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isivi.app.model.ExperienciaIsivi;
import com.isivi.app.repository.ExperienciaIsiviRepository;
import com.isivi.app.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExperienciaIsiviControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExperienciaIsiviRepository repository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private String validAdminToken;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        validAdminToken = jwtService.generarToken("admin");
    }

    @Test
    @DisplayName("Público: GET /api/experiencias-isivi solo retorna experiencias activas con autorización")
    void testGetExperienciasPublicas() throws Exception {
        ExperienciaIsivi exp1 = new ExperienciaIsivi();
        exp1.setNombreMostrar("María");
        exp1.setTestimonio("Excelente servicio");
        exp1.setCalificacion(5);
        exp1.setActiva(true);
        exp1.setAutorizacionPublicacion(true);
        repository.save(exp1);

        ExperienciaIsivi exp2 = new ExperienciaIsivi();
        exp2.setNombreMostrar("Ana");
        exp2.setTestimonio("No autorizada");
        exp2.setActiva(true);
        exp2.setAutorizacionPublicacion(false); // No debe mostrarse
        repository.save(exp2);

        ExperienciaIsivi exp3 = new ExperienciaIsivi();
        exp3.setNombreMostrar("Laura");
        exp3.setTestimonio("Inactiva");
        exp3.setActiva(false); // No debe mostrarse
        exp3.setAutorizacionPublicacion(true);
        repository.save(exp3);

        mockMvc.perform(get("/api/experiencias-isivi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombreMostrar", is("María")))
                .andExpect(jsonPath("$[0].testimonio", is("Excelente servicio")));
    }

    @Test
    @DisplayName("Admin: POST /api/experiencias-isivi falla si activa es true pero autorizacionPublicacion es false")
    void testCrearExperienciaSinAutorizacionFalla() throws Exception {
        ExperienciaIsivi exp = new ExperienciaIsivi();
        exp.setNombreMostrar("Carlos");
        exp.setTestimonio("Prueba");
        exp.setActiva(true);
        exp.setAutorizacionPublicacion(false);

        mockMvc.perform(post("/api/experiencias-isivi")
                        .header("Authorization", "Bearer " + validAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exp)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("autorización explícita")));
    }

    @Test
    @DisplayName("Admin: POST /api/experiencias-isivi crea exitosamente una experiencia")
    void testCrearExperienciaExitosa() throws Exception {
        ExperienciaIsivi exp = new ExperienciaIsivi();
        exp.setNombreMostrar("Sofia");
        exp.setTestimonio("Gran producto capilar");
        exp.setCalificacion(5);
        exp.setTipoRelacionado("PRODUCTO");
        exp.setElementoRelacionadoId("prod-123");
        exp.setActiva(true);
        exp.setAutorizacionPublicacion(true);

        mockMvc.perform(post("/api/experiencias-isivi")
                        .header("Authorization", "Bearer " + validAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exp)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nombreMostrar", is("Sofia")));
    }

    @Test
    @DisplayName("Admin/Público: Persistencia y respuesta de los parametros de encuadre de imagen (fit, zoom, posX, posY)")
    void testCrearYRecuperarExperienciaConAjusteDeEncuadre() throws Exception {
        ExperienciaIsivi exp = new ExperienciaIsivi();
        exp.setNombreMostrar("Camila");
        exp.setTestimonio("Encuadre perfecto");
        exp.setCalificacion(5);
        exp.setActiva(true);
        exp.setAutorizacionPublicacion(true);
        exp.setFit("contain");
        exp.setZoom(1.5);
        exp.setPosX(30);
        exp.setPosY(70);

        mockMvc.perform(post("/api/experiencias-isivi")
                        .header("Authorization", "Bearer " + validAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exp)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fit", is("contain")))
                .andExpect(jsonPath("$.zoom", is(1.5)))
                .andExpect(jsonPath("$.posX", is(30)))
                .andExpect(jsonPath("$.posY", is(70)));

        mockMvc.perform(get("/api/experiencias-isivi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fit", is("contain")))
                .andExpect(jsonPath("$[0].zoom", is(1.5)))
                .andExpect(jsonPath("$[0].posX", is(30)))
                .andExpect(jsonPath("$[0].posY", is(70)));
    }

    @Test
    @DisplayName("Admin: DELETE /api/experiencias-isivi/{id} elimina la experiencia")
    void testEliminarExperiencia() throws Exception {
        ExperienciaIsivi exp = new ExperienciaIsivi();
        exp.setNombreMostrar("Pedro");
        exp.setTestimonio("Tratamiento excelente");
        exp.setActiva(true);
        exp.setAutorizacionPublicacion(true);
        exp = repository.save(exp);

        mockMvc.perform(delete("/api/experiencias-isivi/" + exp.getId())
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk());

        assertFalse(repository.existsById(exp.getId()));
    }

    @Test
    @DisplayName("Frontend: Verificar que modal-exp-before-after está fuera de page-admin y page-client en index.html")
    void testModalBeforeAfterFueraDePageAdmin() throws Exception {
        java.nio.file.Path indexPath = java.nio.file.Paths.get("src/main/resources/static/index.html");
        String content = java.nio.file.Files.readString(indexPath);

        int idxClient = content.indexOf("id=\"page-client\"");
        int idxAdmin = content.indexOf("id=\"page-admin\"");
        int idxModal = content.indexOf("id=\"modal-exp-before-after\"");

        org.junit.jupiter.api.Assertions.assertTrue(idxClient != -1, "Debe existir id='page-client'");
        org.junit.jupiter.api.Assertions.assertTrue(idxAdmin != -1, "Debe existir id='page-admin'");
        org.junit.jupiter.api.Assertions.assertTrue(idxModal != -1, "Debe existir id='modal-exp-before-after'");

        int idxAdminEnd = content.indexOf("<!-- ============ MODAL: ANTES Y DESPUÉS (CLIENTE) ============ -->");
        org.junit.jupiter.api.Assertions.assertTrue(idxAdminEnd > idxAdmin, "El comentario de separación debe estar después de page-admin");
        org.junit.jupiter.api.Assertions.assertTrue(idxModal > idxAdminEnd, "modal-exp-before-after debe estar después de la sección page-admin");
    }
}
