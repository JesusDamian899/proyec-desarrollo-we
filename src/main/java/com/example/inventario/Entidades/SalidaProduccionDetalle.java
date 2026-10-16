package com.example.inventario.Entidades;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Salida_Produccion_Detalle")
public class SalidaProduccionDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_salida")
    private Integer idDetalleSalida;

    @ManyToOne
    @JoinColumn(name = "id_salida", nullable = false)
    private SalidaProduccionCabecera salidaCabecera;

    @ManyToOne
    @JoinColumn(name = "id_articulo", nullable = false)
    private Articulo articulo;

    // Esta relación es el secreto para mantener la trazabilidad del Lote (FIFO)
    @ManyToOne
    @JoinColumn(name = "id_detalle_ingreso", nullable = false)
    private IngresoDetalle loteIngreso;

    @Column(name = "cantidad_entregada", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadEntregada;

    public SalidaProduccionDetalle() {}

    // Getters y Setters...
    public Integer getIdDetalleSalida() { return idDetalleSalida; }
    public void setIdDetalleSalida(Integer idDetalleSalida) { this.idDetalleSalida = idDetalleSalida; }
    public SalidaProduccionCabecera getSalidaCabecera() { return salidaCabecera; }
    public void setSalidaCabecera(SalidaProduccionCabecera salidaCabecera) { this.salidaCabecera = salidaCabecera; }
    public Articulo getArticulo() { return articulo; }
    public void setArticulo(Articulo articulo) { this.articulo = articulo; }
    public IngresoDetalle getLoteIngreso() { return loteIngreso; }
    public void setLoteIngreso(IngresoDetalle loteIngreso) { this.loteIngreso = loteIngreso; }
    public BigDecimal getCantidadEntregada() { return cantidadEntregada; }
    public void setCantidadEntregada(BigDecimal cantidadEntregada) { this.cantidadEntregada = cantidadEntregada; }
}