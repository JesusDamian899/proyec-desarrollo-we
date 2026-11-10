package com.example.inventario.Entidades;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Ingreso_Detalle")
public class IngresoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_ingreso")
    private Integer idDetalleIngreso;

    // Vínculo hacia atrás a la cabecera
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "id_ingreso", nullable = false)
    private IngresoCabecera ingresoCabecera;

    @ManyToOne
    @JoinColumn(name = "id_articulo", nullable = false)
    private Articulo articulo;

    @Column(name = "cantidad_recibida", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadRecibida;

    @Column(name = "numero_lote", nullable = false, length = 50)
    private String numeroLote;

    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;

    public IngresoDetalle() {}

    // Getters y Setters... (omito el cuerpo, generalos en tu IDE)
    public Integer getIdDetalleIngreso() { return idDetalleIngreso; }
    public void setIdDetalleIngreso(Integer idDetalleIngreso) { this.idDetalleIngreso = idDetalleIngreso; }
    public IngresoCabecera getIngresoCabecera() { return ingresoCabecera; }
    public void setIngresoCabecera(IngresoCabecera ingresoCabecera) { this.ingresoCabecera = ingresoCabecera; }
    public Articulo getArticulo() { return articulo; }
    public void setArticulo(Articulo articulo) { this.articulo = articulo; }
    public BigDecimal getCantidadRecibida() { return cantidadRecibida; }
    public void setCantidadRecibida(BigDecimal cantidadRecibida) { this.cantidadRecibida = cantidadRecibida; }
    public String getNumeroLote() { return numeroLote; }
    public void setNumeroLote(String numeroLote) { this.numeroLote = numeroLote; }
    public LocalDateTime getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDateTime fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
}
