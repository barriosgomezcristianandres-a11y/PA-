package com.isivi.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Document(collection = "bloqueos_horario")
@CompoundIndex(name = "fecha_hora_unica", def = "{'fechaCita': 1, 'horaCita': 1}", unique = true)
public class BloqueoHorario {
    @Id
    private String id;
    private LocalDate fechaCita;
    private String horaCita;
    private String motivo;
    private String creadoPor;
    private Instant fechaCreacion;

    public BloqueoHorario() {
        this.fechaCreacion = Instant.now();
    }

    public BloqueoHorario(LocalDate fechaCita, String horaCita) {
        this.fechaCita = fechaCita;
        this.horaCita = horaCita;
        this.fechaCreacion = Instant.now();
    }

    public BloqueoHorario(LocalDate fechaCita, String horaCita, String motivo, String creadoPor) {
        this.fechaCita = fechaCita;
        this.horaCita = horaCita;
        this.motivo = motivo;
        this.creadoPor = creadoPor;
        this.fechaCreacion = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public LocalDate getFechaCita() { return fechaCita; }
    public void setFechaCita(LocalDate fechaCita) { this.fechaCita = fechaCita; }
    public String getHoraCita() { return horaCita; }
    public void setHoraCita(String horaCita) { this.horaCita = horaCita; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getCreadoPor() { return creadoPor; }
    public void setCreadoPor(String creadoPor) { this.creadoPor = creadoPor; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Instant fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
