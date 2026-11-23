package com.example.inventario.DTOs;

public record PermisoResponse(
    Long idPermiso,
    String codigo,
    String nombre,
    String modulo,
    String ruta,
    String tipo,
    boolean mostrarEnMenu,
    Integer orden
) {}
