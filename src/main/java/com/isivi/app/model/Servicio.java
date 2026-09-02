package com.isivi.app.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "servicios")
public class Servicio {

    @Id
    private String id;

    @NotBlank
    private String nombre;

    @Indexed
    private String categoria; // tratamientos | corte | color

    @NotNull
    private Double precio;

    private String duracion; // ej: "60 min"

    private String descripcion;

    private String imagenUrl;

    private Double imageZoom = 1.0;
    private Double imagePosX = 50.0;
    private Double imagePosY = 50.0;

    public Servicio() {}

    public Servicio(String nombre, String categoria, Double precio, String duracion, String descripcion, String imagenUrl) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.duracion = duracion;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public String getDuracion() { return duracion; }
    public void setDuracion(String duracion) { this.duracion = duracion; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public Double getImageZoom() { return imageZoom != null ? imageZoom : 1.0; }
    public void setImageZoom(Double imageZoom) { this.imageZoom = imageZoom != null ? imageZoom : 1.0; }

    public Double getImagePosX() { return imagePosX != null ? imagePosX : 50.0; }
    public void setImagePosX(Double imagePosX) { this.imagePosX = imagePosX != null ? imagePosX : 50.0; }

    public Double getImagePosY() { return imagePosY != null ? imagePosY : 50.0; }
    public void setImagePosY(Double imagePosY) { this.imagePosY = imagePosY != null ? imagePosY : 50.0; }

    private String origenImagen;

    public String getOrigenImagen() {
        if (origenImagen == null || origenImagen.trim().isEmpty()) {
            return com.isivi.app.util.ImageHelper.clasificarOrigen(imagenUrl);
        }
        return origenImagen;
    }

    public void setOrigenImagen(String origenImagen) {
        this.origenImagen = origenImagen;
    }
}
