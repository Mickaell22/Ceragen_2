package com.example.ceragen_2.model;

import java.time.LocalDateTime;

public class Profesional {

    // ==============================
    // Campos principales de la tabla
    // ==============================
    private Integer id;
    private String cedula;
    private String nombres;
    private String apellidos;
    private Integer especialidadId;
    private String telefono;           // mapeado a columna "celular" en BD
    private String email;
    private String numeroLicencia;
    private Boolean activo;
    private Integer usuarioId;

    // Nuevos campos según el esquema
    private String modalidadAtencion;     // PRESENCIAL / TELECONSULTA / MIXTA
    private String tipoUsuarioRegistra;   // ADMIN / RECEPCIONISTA
    private LocalDateTime fechaRegistro;  // fecha_registro de la BD

    // ==============================
    // Campos derivados / relacionados
    // ==============================
    private String especialidadNombre;

    public Profesional() {
    }

    // ==============================
    // Getters y Setters
    // ==============================

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(final String cedula) {
        this.cedula = cedula;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(final String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(final String apellidos) {
        this.apellidos = apellidos;
    }

    public Integer getEspecialidadId() {
        return especialidadId;
    }

    public void setEspecialidadId(final Integer especialidadId) {
        this.especialidadId = especialidadId;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(final String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getNumeroLicencia() {
        return numeroLicencia;
    }

    public void setNumeroLicencia(final String numeroLicencia) {
        this.numeroLicencia = numeroLicencia;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(final Boolean activo) {
        this.activo = activo;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(final Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getModalidadAtencion() {
        return modalidadAtencion;
    }

    public void setModalidadAtencion(final String modalidadAtencion) {
        this.modalidadAtencion = modalidadAtencion;
    }

    public String getTipoUsuarioRegistra() {
        return tipoUsuarioRegistra;
    }

    public void setTipoUsuarioRegistra(final String tipoUsuarioRegistra) {
        this.tipoUsuarioRegistra = tipoUsuarioRegistra;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(final LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getEspecialidadNombre() {
        return especialidadNombre;
    }

    public void setEspecialidadNombre(final String especialidadNombre) {
        this.especialidadNombre = especialidadNombre;
    }

    // ==============================
    // Métodos de ayuda
    // ==============================

    public String getNombreCompleto() {
        return "Dr. " + nombres + " " + apellidos;
    }

    @Override
    public String toString() {
        String base = getNombreCompleto();
        if (especialidadNombre != null && !especialidadNombre.isBlank()) {
            base += " - " + especialidadNombre;
        }
        if (modalidadAtencion != null && !modalidadAtencion.isBlank()) {
            base += " (" + modalidadAtencion + ")";
        }
        return base;
    }
}
