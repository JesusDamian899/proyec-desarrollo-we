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
@Table(name = "Transferencia_Detalle")
public class TransferenciaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_transferencia")
    private Integer idDetalleTransferencia;

    @ManyToOne
    @JoinColumn(name = "id_transferencia", nullable = false)
    private TransferenciaCabecera transferenciaCabecera;

    @ManyToOne
    @JoinColumn(name = "id_articulo", nullable = false)
    private Articulo articulo;

    @ManyToOne
    @JoinColumn(name = "id_ubicacion_origen", nullable = false)
    private Ubicacion ubicacionOrigen;

    @ManyToOne
    @JoinColumn(name = "id_ubicacion_destino", nullable = false)
    private Ubicacion ubicacionDestino;

    @Column(name = "cantidad_movida", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadMovida;

    public TransferenciaDetalle() {}

    // Getters y Setters...
    public Integer getIdDetalleTransferencia() { return idDetalleTransferencia; }
    public void setIdDetalleTransferencia(Integer idDetalleTransferencia) { this.idDetalleTransferencia = idDetalleTransferencia; }
    public TransferenciaCabecera getTransferenciaCabecera() { return transferenciaCabecera; }
    public void setTransferenciaCabecera(TransferenciaCabecera transferenciaCabecera) { this.transferenciaCabecera = transferenciaCabecera; }
    public Articulo getArticulo() { return articulo; }
    public void setArticulo(Articulo articulo) { this.articulo = articulo; }
    public Ubicacion getUbicacionOrigen() { return ubicacionOrigen; }
    public void setUbicacionOrigen(Ubicacion ubicacionOrigen) { this.ubicacionOrigen = ubicacionOrigen; }
    public Ubicacion getUbicacionDestino() { return ubicacionDestino; }
    public void setUbicacionDestino(Ubicacion ubicacionDestino) { this.ubicacionDestino = ubicacionDestino; }
    public BigDecimal getCantidadMovida() { return cantidadMovida; }
    public void setCantidadMovida(BigDecimal cantidadMovida) { this.cantidadMovida = cantidadMovida; }
}