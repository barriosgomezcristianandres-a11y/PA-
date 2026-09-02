package com.isivi.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Document(collection = "excepciones_agenda")
public class ExcepcionAgenda {

    @Id
    private String id;

    @Indexed(unique = true)
    private LocalDate fecha;

    /**
     * Tipo de excepción:
     * CERRADO: El día no tendrá atención al público.
     * HORARIO_ESPECIAL: El día tendrá horarios y turnos específicos definidos en 'horarios'.
     */
    private String tipo; // "CERRADO" o "HORARIO_ESPECIAL"

    private List<String> horarios; // Solo si tipo == "HORARIO_ESPECIAL"
    private String motivo; // Ej. "Festivo", "Mantenimiento de sede", "Horario extendido"
    private String creadoPor;
    private Instant fechaCreacion;

    public ExcepcionAgenda() {
        this.fechaCreacion = Instant.now();
    }

    public ExcepcionAgenda(LocalDate fecha, String tipo, List<String> horarios, String motivo, String creadoPor) {
        this.fecha = fecha;
        this.tipo = tipo;
        this.horarios = horarios;
        this.motivo = motivo;
        this.creadoPor = creadoPor;
        this.fechaCreacion = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public List<String> getHorarios() { return horarios; }
    public void setHorarios(List<String> horarios) { this.horarios = horarios; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getCreadoPor() { return creadoPor; }
    public void setCreadoPor(String creadoPor) { this.creadoPor = creadoPor; }

    public Instant getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Instant fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
