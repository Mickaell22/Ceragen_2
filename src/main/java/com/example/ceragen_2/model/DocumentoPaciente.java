package com.example.ceragen_2.model;

import java.time.LocalDateTime;

public class DocumentoPaciente {
    private Integer id;
    private Integer pacienteId;
    private String nombreArchivo;
    private String tipoDocumento;
    private String rutaArchivo;
    private LocalDateTime fechaSubida;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getPacienteId() { return pacienteId; }
    public void setPacienteId(Integer pacienteId) { this.pacienteId = pacienteId; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }

    public LocalDateTime getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(LocalDateTime fechaSubida) { this.fechaSubida = fechaSubida; }
}
