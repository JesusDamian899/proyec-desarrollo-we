package com.example.inventario.Entidades;


import jakarta.persistence.*;

@Entity
@Table(name = "permiso")
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Long idPermiso;

    @Column(nullable = false, unique = true, length = 60)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 40)
    private String modulo;

    @Column(length = 120)
    private String ruta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPermiso tipo;

    @Column(name = "mostrar_en_menu", nullable = false)
    private boolean mostrarEnMenu = false;

    @Column(nullable = false)
    private Integer orden = 0;

    public Permiso() { }

    public Long getIdPermiso() { return idPermiso; }
    public void setIdPermiso(Long idPermiso) { this.idPermiso = idPermiso; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }

    public String getRuta() { return ruta; }
    public void setRuta(String ruta) { this.ruta = ruta; }

    public TipoPermiso getTipo() { return tipo; }
    public void setTipo(TipoPermiso tipo) { this.tipo = tipo; }

    public boolean isMostrarEnMenu() { return mostrarEnMenu; }
    public void setMostrarEnMenu(boolean mostrarEnMenu) { this.mostrarEnMenu = mostrarEnMenu; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
}