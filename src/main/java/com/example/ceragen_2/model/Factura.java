package com.example.ceragen_2.model;

import java.time.LocalDateTime;
import java.util.List;

public class Factura {
    private Integer id;
    private String numeroFactura;
    private Integer pacienteId;
    private LocalDateTime fechaEmision;
    private String ciudad;
    private Double subtotal;
    private Double iva;
    private Double descuento;
    private Double total;
    private String metodoPago;
    private String estado;

    // Campos relacionados (no en la BD)
    private String pacienteNombre;
    private List<DetalleFactura> detalles;

    public Factura() {
    }

    public Factura(Integer id, String numeroFactura, Integer pacienteId, LocalDateTime fechaEmision,
                   String ciudad, Double subtotal, Double iva, Double descuento, Double total,
                   String metodoPago, String estado) {
        this.id = id;
        this.numeroFactura = numeroFactura;
        this.pacienteId = pacienteId;
        this.fechaEmision = fechaEmision;
        this.ciudad = ciudad;
        this.subtotal = subtotal;
        this.iva = iva;
        this.descuento = descuento;
        this.total = total;
        this.metodoPago = metodoPago;
        this.estado = estado;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public Integer getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Integer pacienteId) {
        this.pacienteId = pacienteId;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getIva() {
        return iva;
    }

    public void setIva(Double iva) {
        this.iva = iva;
    }

    public Double getDescuento() {
        return descuento;
    }

    public void setDescuento(Double descuento) {
        this.descuento = descuento;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPacienteNombre() {
        return pacienteNombre;
    }

    public void setPacienteNombre(String pacienteNombre) {
        this.pacienteNombre = pacienteNombre;
    }

    public List<DetalleFactura> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleFactura> detalles) {
        this.detalles = detalles;
    }

    @Override
    public String toString() {
        return "Factura{" +
                "id=" + id +
                ", numeroFactura='" + numeroFactura + '\'' +
                ", pacienteId=" + pacienteId +
                ", fechaEmision=" + fechaEmision +
                ", total=" + total +
                ", metodoPago='" + metodoPago + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}
