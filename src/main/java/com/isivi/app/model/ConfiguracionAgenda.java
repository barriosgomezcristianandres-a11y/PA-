package com.isivi.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "configuracion_agenda")
public class ConfiguracionAgenda {
    @Id
    private String id = "principal";
    private List<Integer> diasLaborales;
    private List<String> horarios;

    @Transient
    private List<ExcepcionAgenda> excepciones;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public List<Integer> getDiasLaborales() { return diasLaborales; }
    public void setDiasLaborales(List<Integer> diasLaborales) { this.diasLaborales = diasLaborales; }
    public List<String> getHorarios() { return horarios; }
    public void setHorarios(List<String> horarios) { this.horarios = horarios; }
    public List<ExcepcionAgenda> getExcepciones() { return excepciones; }
    public void setExcepciones(List<ExcepcionAgenda> excepciones) { this.excepciones = excepciones; }
}
