package com.example.inventario.Entidades;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Transferencia_Cabecera")
public class TransferenciaCabecera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transferencia")
    private Integer idTransferencia;

    @Column(name = "fecha_transferencia")
    private LocalDateTime fechaTransferencia;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "motivo", length = 255)
    private String motivo;

    @OneToMany(mappedBy = "transferenciaCabecera", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransferenciaDetalle> detalles;

    public TransferenciaCabecera() {}

    // Getters y Setters...
    public Integer getIdTransferencia() { return idTransferencia; }
    public void setIdTransferencia(Integer idTransferencia) { this.idTransferencia = idTransferencia; }
    public LocalDateTime getFechaTransferencia() { return fechaTransferencia; }
    public void setFechaTransferencia(LocalDateTime fechaTransferencia) { this.fechaTransferencia = fechaTransferencia; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public List<TransferenciaDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<TransferenciaDetalle> detalles) { this.detalles = detalles; }
}