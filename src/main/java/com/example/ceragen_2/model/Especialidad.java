package com.example.ceragen_2.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Especialidad {

    private Integer id;
    private String nombre;
    private String codigo;
    private String descripcion;
    private Integer duracionEstandarMin;
    private BigDecimal tarifaBase;
    private String estado;              // ACTIVO / INACTIVO
    private Integer usuarioCreadorId;
    private LocalDateTime fechaCreacion;

    public Especialidad() {
    }

    // =========================
    // Getters y Setters
    // =========================

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(final String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(final String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(final String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getDuracionEstandarMin() {
        return duracionEstandarMin;
    }

    public void setDuracionEstandarMin(final Integer duracionEstandarMin) {
        this.duracionEstandarMin = duracionEstandarMin;
    }

    public BigDecimal getTarifaBase() {
        return tarifaBase;
    }

    public void setTarifaBase(final BigDecimal tarifaBase) {
        this.tarifaBase = tarifaBase;
    }

    // Alias para compatibilidad con código viejo (costoConsulta)
    public BigDecimal getCostoConsulta() {
        return tarifaBase;
    }

    public void setCostoConsulta(final BigDecimal costoConsulta) {
        this.tarifaBase = costoConsulta;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(final String estado) {
        this.estado = estado;
    }

    public Integer getUsuarioCreadorId() {
        return usuarioCreadorId;
    }

    public void setUsuarioCreadorId(final Integer usuarioCreadorId) {
        this.usuarioCreadorId = usuarioCreadorId;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(final LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @Override
    public String toString() {
        String costoStr = tarifaBase != null
                ? "$" + tarifaBase.toPlainString()
                : "s/tarifa";
        return nombre + " (" + codigo + ") - " + costoStr;
    }
}
