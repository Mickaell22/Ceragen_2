package com.example.ceragen_2.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Cita {
    private Integer id;
    private Integer pacienteId;
    private Integer profesionalId;
    private LocalDateTime fechaHora;
    private String motivo;
    private String estado;
    private String observaciones;
    private LocalDateTime fechaCreacion;
    private BigDecimal costo; // Nuevo campo

    // Para mostrar datos relacionados en la tabla
    private String pacienteNombre;
    private String profesionalNombre;

    public Cita() {
    }

    public Cita(Integer id, Integer pacienteId, Integer profesionalId, LocalDateTime fechaHora,
                String motivo, String estado, String observaciones, LocalDateTime fechaCreacion) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.profesionalId = profesionalId;
        this.fechaHora = fechaHora;
        this.motivo = motivo;
        this.estado = estado;
        this.observaciones = observaciones;
        this.fechaCreacion = fechaCreacion;
    }

    //constructor agg por marco
    public Cita(Integer pacienteId, Integer profesionalId, LocalDateTime fechaHora,
                String motivo) {
        this.pacienteId = pacienteId;
        this.profesionalId = profesionalId;
        this.fechaHora = fechaHora;
        this.motivo = motivo;
    }

    public Cita(Integer pacienteId, Integer profesionalId, LocalDateTime fechaHora,
                String motivo, BigDecimal costo) {
        this.pacienteId = pacienteId;
        this.profesionalId = profesionalId;
        this.fechaHora = fechaHora;
        this.motivo = motivo;
        this.costo = costo;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Integer pacienteId) {
        this.pacienteId = pacienteId;
    }

    public Integer getProfesionalId() {
        return profesionalId;
    }

    public void setProfesionalId(Integer profesionalId) {
        this.profesionalId = profesionalId;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getPacienteNombre() {
        return pacienteNombre;
    }

    public void setPacienteNombre(String pacienteNombre) {
        this.pacienteNombre = pacienteNombre;
    }

    public String getProfesionalNombre() {
        return profesionalNombre;
    }

    public void setProfesionalNombre(String profesionalNombre) {
        this.profesionalNombre = profesionalNombre;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    @Override
    public String toString() {
        return "Cita{" +
                "id=" + id +
                ", pacienteId=" + pacienteId +
                ", profesionalId=" + profesionalId +
                ", fechaHora=" + fechaHora +
                ", estado='" + estado + '\'' +
                '}';
    }
}
