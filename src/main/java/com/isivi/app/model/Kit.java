package com.isivi.app.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "kits")
public class Kit {

    @Id
    private String id;

    @NotBlank
    private String nombre;

    @NotNull
    private Double precio;

    /** Precio acumulado individual o precio original antes de descuento */
    private Double precioAnterior;

    private String descripcion;

    private String imagenUrl;

    private Double imageZoom = 1.0;
    private Double imagePosX = 50.0;
    private Double imagePosY = 50.0;

    private Boolean enStock = true;

    @NotNull
    @Min(0)
    private Integer cantidad = 0;

    /** Referencia canónica a CategoriaProducto */
    private String categoriaId;

    public Kit() {}

    public Kit(String nombre, Double precio, String descripcion, String imagenUrl, Boolean enStock) {
        this(nombre, precio, descripcion, imagenUrl, enStock, Boolean.TRUE.equals(enStock) ? 1 : 0);
    }

    public Kit(String nombre, Double precio, String descripcion, String imagenUrl, Boolean enStock, Integer cantidad) {
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
        this.enStock = enStock;
        this.cantidad = cantidad;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public Double getPrecioAnterior() { return precioAnterior; }
    public void setPrecioAnterior(Double precioAnterior) { this.precioAnterior = precioAnterior; }

    public String getCategoriaId() { return categoriaId; }
    public void setCategoriaId(String categoriaId) { this.categoriaId = categoriaId; }

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

    public Boolean getEnStock() { return enStock; }
    public void setEnStock(Boolean enStock) { this.enStock = enStock; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

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

    @org.springframework.data.annotation.Transient
    private Boolean temporalmenteReservado = false;

    public Boolean getTemporalmenteReservado() {
        return temporalmenteReservado != null && temporalmenteReservado;
    }

    public void setTemporalmenteReservado(Boolean temporalmenteReservado) {
        this.temporalmenteReservado = temporalmenteReservado;
    }
}
