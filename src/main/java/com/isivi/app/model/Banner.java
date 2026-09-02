package com.isivi.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "banners")
public class Banner {
    @Id private String id;
    private String titulo;
    private String descripcion;
    private String imagenUrl;
    private String textoBoton;
    private String enlaceBoton;
    private Boolean activo = true;
    private String tipoDestino = "NINGUNO";
    private String destinoId;

    private Double imageZoom = 1.0;
    private Double imagePosX = 50.0;
    private Double imagePosY = 50.0;

    public Banner() {}
    public Banner(String titulo, String descripcion, String imagenUrl, String textoBoton, String enlaceBoton, Boolean activo) {
        this.titulo = titulo; this.descripcion = descripcion; this.imagenUrl = imagenUrl;
        this.textoBoton = textoBoton; this.enlaceBoton = enlaceBoton; this.activo = activo;
    }
    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public String getTextoBoton() { return textoBoton; }
    public void setTextoBoton(String textoBoton) { this.textoBoton = textoBoton; }
    public String getEnlaceBoton() { return enlaceBoton; }
    public void setEnlaceBoton(String enlaceBoton) { this.enlaceBoton = enlaceBoton; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public String getTipoDestino() { return tipoDestino; }
    public void setTipoDestino(String tipoDestino) { this.tipoDestino = tipoDestino; }
    public String getDestinoId() { return destinoId; }
    public void setDestinoId(String destinoId) { this.destinoId = destinoId; }

    public Double getImageZoom() { return imageZoom != null ? imageZoom : 1.0; }
    public void setImageZoom(Double imageZoom) { this.imageZoom = imageZoom; }
    public Double getImagePosX() { return imagePosX != null ? imagePosX : 50.0; }
    public void setImagePosX(Double imagePosX) { this.imagePosX = imagePosX; }
    public Double getImagePosY() { return imagePosY != null ? imagePosY : 50.0; }
    public void setImagePosY(Double imagePosY) { this.imagePosY = imagePosY; }

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
