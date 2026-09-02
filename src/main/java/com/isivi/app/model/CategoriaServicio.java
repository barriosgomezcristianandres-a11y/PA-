package com.isivi.app.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categorias_servicio")
public class CategoriaServicio {
    @Id private String id;
    @NotBlank private String nombre;

    public CategoriaServicio() {}
    public CategoriaServicio(String id, String nombre) { this.id = id; this.nombre = nombre; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
