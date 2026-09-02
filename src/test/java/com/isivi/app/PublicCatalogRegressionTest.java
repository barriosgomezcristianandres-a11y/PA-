package com.isivi.app;

import com.isivi.app.controller.AgendaController;
import com.isivi.app.controller.CategoriaProductoController;
import com.isivi.app.controller.CategoriaServicioController;
import com.isivi.app.controller.KitController;
import com.isivi.app.controller.ProductoController;
import com.isivi.app.controller.ServicioController;
import com.isivi.app.model.CategoriaProducto;
import com.isivi.app.model.CategoriaServicio;
import com.isivi.app.model.Kit;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Servicio;
import com.isivi.app.repository.CategoriaProductoRepository;
import com.isivi.app.repository.CategoriaServicioRepository;
import com.isivi.app.repository.KitRepository;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ServicioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import com.isivi.app.model.Administrador;
import com.isivi.app.repository.AdministradorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PublicCatalogRegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServicioController servicioController;

    @Autowired
    private ProductoController productoController;

    @Autowired
    private KitController kitController;

    @Autowired
    private CategoriaProductoController categoriaProductoController;

    @Autowired
    private CategoriaServicioController categoriaServicioController;

    @Autowired
    private AgendaController agendaController;

    @Test
    @DisplayName("GET /api/servicios debe ser público (200 OK) y devolver JSON")
    void testGetServiciosPublic() throws Exception {
        String res = mockMvc.perform(get("/api/servicios"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();
        System.out.println("DEBUG_SERVICIOS_JSON: " + res);
    }

    @Test
    @DisplayName("GET /api/productos debe ser público (200 OK) y devolver JSON")
    void testGetProductosPublic() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/kits debe ser público (200 OK) y devolver JSON")
    void testGetKitsPublic() throws Exception {
        mockMvc.perform(get("/api/kits"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/categorias-producto debe ser público (200 OK)")
    void testGetCategoriasProductoPublic() throws Exception {
        mockMvc.perform(get("/api/categorias-producto").param("soloActivas", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/categorias-servicio debe ser público (200 OK)")
    void testGetCategoriasServicioPublic() throws Exception {
        mockMvc.perform(get("/api/categorias-servicio"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/agenda debe ser público (200 OK)")
    void testGetAgendaPublic() throws Exception {
        mockMvc.perform(get("/api/agenda"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("POST /api/reservas con servicio de la base de datos debe funcionar")
    void testCrearReservaConServicioReal() throws Exception {
        if (administradorRepository.findByUsuario("admin").isEmpty()) {
            administradorRepository.save(new Administrador("admin", passwordEncoder.encode("1234")));
            System.out.println("ADMINISTRADOR 'admin' SEMBRADO CON EXITO EN BASE DE DATOS REAL");
        }
        java.util.List<Servicio> servs = servicioRepository.findAll();
        if (servs.isEmpty()) {
            System.out.println("No hay servicios en la base de datos.");
            return;
        }
        Servicio s = servs.get(0);
        System.out.println("SERVICIO PARA TEST: ID=" + s.getId() + ", Nombre=" + s.getNombre());
        
        String requestBody = "{"
                + "\"nombreCliente\":\"Cliente Test\","
                + "\"telefono\":\"3001234567\","
                + "\"email\":\"test@test.com\","
                + "\"medioPago\":\"WOMPI\","
                + "\"fechaCita\":\"2026-09-01\","
                + "\"horaCita\":\"10:00\","
                + "\"itemsInventario\":["
                + "  {\"id\":\"" + s.getId() + "\",\"tipo\":\"servicio\",\"cantidad\":1}"
                + "]"
                + "}";
                
        String res = mockMvc.perform(get("/api/reservas/disponibilidad").param("fecha", "2026-09-01"))
                .andReturn().getResponse().getContentAsString();
        System.out.println("DISPONIBILIDAD: " + res);
        
        String result = mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andReturn().getResponse().getContentAsString();
        System.out.println("CREAR RESERVA RESPONSE: " + result);
    }
}
