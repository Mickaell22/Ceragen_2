package com.example.ceragen_2.model;

import java.math.BigDecimal;

public class Especialidad {
    private Integer id;
    private String nombre;
    private String descripcion;
    private BigDecimal costoConsulta;

    public Especialidad() {
    }

    public Especialidad(Integer id, String nombre, String descripcion, BigDecimal costoConsulta) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costoConsulta = costoConsulta;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getCostoConsulta() {
        return costoConsulta;
    }

    public void setCostoConsulta(BigDecimal costoConsulta) {
        this.costoConsulta = costoConsulta;
    }

    @Override
    public String toString() {
        return nombre + " - $" + costoConsulta;
    }
}