package com.example.inventario.Servicios;
import java.util.Set;

public record RolResponse(
    Long idRol,
    String nombre,
    String descripcion,
    boolean activo,
    Set<String> permisos
) {}
