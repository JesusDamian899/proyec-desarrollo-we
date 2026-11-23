package com.example.inventario.DTOs;

import java.util.Set;

public record RolResponse(
    Long idRol,
    String nombre,
    String descripcion,
    boolean activo,
    Set<String> permisos
) {}
