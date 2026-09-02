package com.isivi.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "experiencias_isivi")
public class ExperienciaIsivi {

    @Id
    private String id;
    private String nombreMostrar;
    private String testimonio;
    private Integer calificacion; // 1 - 5
    private String imagenPrincipal;
    private String imagenAntes;
    private String imagenDespues;
    private String tipoRelacionado; // PRODUCTO, KIT, SERVICIO, NINGUNA
    private String elementoRelacionadoId;
    private Boolean autorizacionPublicacion = false;
    private Boolean destacada = false;
    private Boolean activa = true;
    private Integer orden = 0;
    private String fit = "cover";
    private Double zoom = 1.0;
    private Integer posX = 50;
    private Integer posY = 50;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ExperienciaIsivi() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombreMostrar() {
        return nombreMostrar;
    }

    public void setNombreMostrar(String nombreMostrar) {
        this.nombreMostrar = nombreMostrar;
    }

    public String getTestimonio() {
        return testimonio;
    }

    public void setTestimonio(String testimonio) {
        this.testimonio = testimonio;
    }

    public Integer getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Integer calificacion) {
        this.calificacion = calificacion;
    }

    public String getImagenPrincipal() {
        return imagenPrincipal;
    }

    public void setImagenPrincipal(String imagenPrincipal) {
        this.imagenPrincipal = imagenPrincipal;
    }

    public String getImagenAntes() {
        return imagenAntes;
    }

    public void setImagenAntes(String imagenAntes) {
        this.imagenAntes = imagenAntes;
    }

    public String getImagenDespues() {
        return imagenDespues;
    }

    public void setImagenDespues(String imagenDespues) {
        this.imagenDespues = imagenDespues;
    }

    public String getTipoRelacionado() {
        return tipoRelacionado;
    }

    public void setTipoRelacionado(String tipoRelacionado) {
        this.tipoRelacionado = tipoRelacionado;
    }

    public String getElementoRelacionadoId() {
        return elementoRelacionadoId;
    }

    public void setElementoRelacionadoId(String elementoRelacionadoId) {
        this.elementoRelacionadoId = elementoRelacionadoId;
    }

    public Boolean getAutorizacionPublicacion() {
        return autorizacionPublicacion;
    }

    public void setAutorizacionPublicacion(Boolean autorizacionPublicacion) {
        this.autorizacionPublicacion = autorizacionPublicacion;
    }

    public Boolean getDestacada() {
        return destacada;
    }

    public void setDestacada(Boolean destacada) {
        this.destacada = destacada;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public String getFit() {
        return fit != null ? fit : "cover";
    }

    public void setFit(String fit) {
        this.fit = fit;
    }

    public Double getZoom() {
        return zoom != null ? zoom : 1.0;
    }

    public void setZoom(Double zoom) {
        this.zoom = zoom;
    }

    public Integer getPosX() {
        return posX != null ? posX : 50;
    }

    public void setPosX(Integer posX) {
        this.posX = posX;
    }

    public Integer getPosY() {
        return posY != null ? posY : 50;
    }

    public void setPosY(Integer posY) {
        this.posY = posY;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
