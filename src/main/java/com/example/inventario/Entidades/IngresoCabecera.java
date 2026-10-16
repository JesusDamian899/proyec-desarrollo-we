package com.example.inventario.Entidades;

import jakarta.persistence.*;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "Ingreso_Cabecera")
public class IngresoCabecera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ingreso")
    private Integer idIngreso;

    @ManyToOne
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    @Column(name = "documento_referencia", nullable = false, length = 50)
    private String documentoReferencia;

    @Column(name = "fecha_recepcion")
    private LocalDateTime fechaRecepcion;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "estado", length = 20)
    private String estado = "COMPLETADO";

    // ¡La relación que une la Cabecera con sus Detalles!
    @OneToMany(mappedBy = "ingresoCabecera", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IngresoDetalle> detalles;

    public IngresoCabecera() {}

    // Getters y Setters... (omito el cuerpo para ahorrar espacio, generalos en tu IDE)
    public Integer getIdIngreso() { return idIngreso; }
    public void setIdIngreso(Integer idIngreso) { this.idIngreso = idIngreso; }
    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }
    public String getDocumentoReferencia() { return documentoReferencia; }
    public void setDocumentoReferencia(String documentoReferencia) { this.documentoReferencia = documentoReferencia; }
    public LocalDateTime getFechaRecepcion() { return fechaRecepcion; }
    public void setFechaRecepcion(LocalDateTime fechaRecepcion) { this.fechaRecepcion = fechaRecepcion; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public List<IngresoDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<IngresoDetalle> detalles) { this.detalles = detalles; }
}
