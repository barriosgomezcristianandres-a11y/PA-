package com.isivi.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "bloqueos_recurrentes")
public class BloqueoRecurrente {

    @Id
    private String id;

    /** 0 = Domingo, 1 = Lunes, 2 = Martes, 3 = Miércoles, 4 = Jueves, 5 = Viernes, 6 = Sábado */
    private Integer diaSemana;

    private List<String> horarios = new ArrayList<>();
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String motivo;
    private Boolean activo = true;
    private String creadoPor;

    public BloqueoRecurrente() {}

    public BloqueoRecurrente(Integer diaSemana, List<String> horarios, LocalDate fechaInicio, LocalDate fechaFin, String motivo, String creadoPor) {
        this.diaSemana = diaSemana;
        this.horarios = horarios != null ? horarios : new ArrayList<>();
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.motivo = motivo;
        this.creadoPor = creadoPor;
        this.activo = true;
    }

    public boolean aplicaAFecha(LocalDate fecha) {
        if (!Boolean.TRUE.equals(activo)) return false;
        if (fecha == null || diaSemana == null) return false;
        if (fecha.getDayOfWeek().getValue() % 7 != diaSemana) return false;
        if (fechaInicio != null && fecha.isBefore(fechaInicio)) return false;
        if (fechaFin != null && fecha.isAfter(fechaFin)) return false;
        return true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(Integer diaSemana) {
        this.diaSemana = diaSemana;
    }

    public List<String> getHorarios() {
        return horarios;
    }

    public void setHorarios(List<String> horarios) {
        this.horarios = horarios;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }
}
