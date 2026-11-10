package com.example.inventario.Entidades;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "Salida_Produccion_Cabecera")
public class SalidaProduccionCabecera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_salida")
    private Integer idSalida;

    @Column(name = "orden_produccion", nullable = false, length = 50)
    private String ordenProduccion;

    @Column(name = "fecha_salida")
    private LocalDateTime fechaSalida = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuarioDespacha;

    @ManyToOne
    @JoinColumn(name = "id_solicitante", nullable = false)
    private Usuario usuarioSolicitante;

    @Column(name = "estado", length = 20)
    private String estado = "COMPLETADO";

    @OneToMany(mappedBy = "salidaCabecera", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalidaProduccionDetalle> detalles = new ArrayList<>();

    public SalidaProduccionCabecera() {}

    // Getters y Setters...
    public Integer getIdSalida() { return idSalida; }
    public void setIdSalida(Integer idSalida) { this.idSalida = idSalida; }
    public String getOrdenProduccion() { return ordenProduccion; }
    public void setOrdenProduccion(String ordenProduccion) { this.ordenProduccion = ordenProduccion; }
    public LocalDateTime getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDateTime fechaSalida) { this.fechaSalida = fechaSalida; }
    public Usuario getUsuarioDespacha() { return usuarioDespacha; }
    public void setUsuarioDespacha(Usuario usuarioDespacha) { this.usuarioDespacha = usuarioDespacha; }
    public Usuario getUsuarioSolicitante() { return usuarioSolicitante; }
    public void setUsuarioSolicitante(Usuario usuarioSolicitante) { this.usuarioSolicitante = usuarioSolicitante; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public List<SalidaProduccionDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<SalidaProduccionDetalle> detalles) { this.detalles = detalles; }
}
