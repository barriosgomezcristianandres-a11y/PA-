package com.isivi.app.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.core.mapping.Field;

public class VarianteProducto {

    @Field("id")
    private String id;

    @NotBlank
    private String nombre;

    @NotNull
    @Min(0)
    private Double precio;

    private Double precioAnterior;

    @Min(0)
    private Integer cantidad = 0;

    private Boolean enStock = true;

    private Boolean activo = true;

    public VarianteProducto() {}

    public VarianteProducto(String id, String nombre, Double precio, Integer cantidad, Boolean enStock, Boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad != null ? cantidad : 0;
        this.enStock = enStock != null ? enStock : true;
        this.activo = activo != null ? activo : true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public Double getPrecioAnterior() { return precioAnterior; }
    public void setPrecioAnterior(Double precioAnterior) { this.precioAnterior = precioAnterior; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Boolean getEnStock() { return enStock; }
    public void setEnStock(Boolean enStock) { this.enStock = enStock; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    @org.springframework.data.annotation.Transient
    private Boolean temporalmenteReservado = false;

    public Boolean getTemporalmenteReservado() {
        return temporalmenteReservado != null && temporalmenteReservado;
    }

    public void setTemporalmenteReservado(Boolean temporalmenteReservado) {
        this.temporalmenteReservado = temporalmenteReservado;
    }
}
