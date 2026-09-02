package com.isivi.app.model;

/** Referencia inmutable del catálogo incluida en una reserva o pedido. */
public class ItemReserva {
    private String id;
    private String tipo; // servicio | producto | kit
    private String nombre;
    private String varianteId;
    private String varianteNombre;
    private Integer cantidad = 1;
    /** Precio y subtotal capturados al crear el pedido/reserva. */
    private Double precioUnitario;
    private Double subtotal;

    public ItemReserva() {}

    public ItemReserva(String id, String tipo, Integer cantidad) {
        this.id = id;
        this.tipo = tipo;
        this.cantidad = cantidad;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getVarianteId() { return varianteId; }
    public void setVarianteId(String varianteId) { this.varianteId = varianteId; }
    public String getVarianteNombre() { return varianteNombre; }
    public void setVarianteNombre(String varianteNombre) { this.varianteNombre = varianteNombre; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
}
