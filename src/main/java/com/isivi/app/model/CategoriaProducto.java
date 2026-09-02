package com.isivi.app.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categorias_producto")
public class CategoriaProducto {

    @Id
    private String id;

    @NotBlank
    private String nombre;

    private Boolean activo = true;

    private String tipo; // PRODUCTO | KIT

    public CategoriaProducto() {}

    public CategoriaProducto(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
        this.activo = true;
    }

    public CategoriaProducto(String id, String nombre, Boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.activo = activo != null ? activo : true;
    }

    public CategoriaProducto(String id, String nombre, Boolean activo, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.activo = activo != null ? activo : true;
        this.tipo = tipo;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo != null ? activo : true; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
