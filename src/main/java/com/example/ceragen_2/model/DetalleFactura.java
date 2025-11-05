package com.example.ceragen_2.model;

public class DetalleFactura {
    private Integer id;
    private Integer facturaId;
    private Integer citaId;
    private Integer especialidadId;
    private String descripcion;
    private Double precioUnitario;
    private Integer cantidad;
    private Double total;

    // Campos relacionados (no en la BD)
    private String especialidadNombre;

    public DetalleFactura() {
    }

    public DetalleFactura(Integer id, Integer facturaId, Integer citaId, Integer especialidadId,
                          String descripcion, Double precioUnitario, Integer cantidad, Double total) {
        this.id = id;
        this.facturaId = facturaId;
        this.citaId = citaId;
        this.especialidadId = especialidadId;
        this.descripcion = descripcion;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.total = total;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFacturaId() {
        return facturaId;
    }

    public void setFacturaId(Integer facturaId) {
        this.facturaId = facturaId;
    }

    public Integer getCitaId() {
        return citaId;
    }

    public void setCitaId(Integer citaId) {
        this.citaId = citaId;
    }

    public Integer getEspecialidadId() {
        return especialidadId;
    }

    public void setEspecialidadId(Integer especialidadId) {
        this.especialidadId = especialidadId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getEspecialidadNombre() {
        return especialidadNombre;
    }

    public void setEspecialidadNombre(String especialidadNombre) {
        this.especialidadNombre = especialidadNombre;
    }

    @Override
    public String toString() {
        return "DetalleFactura{" +
                "descripcion='" + descripcion + '\'' +
                ", cantidad=" + cantidad +
                ", total=" + total +
                '}';
    }
}
