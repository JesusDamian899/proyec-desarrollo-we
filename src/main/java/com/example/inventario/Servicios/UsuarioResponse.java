package com.example.inventario.Servicios;

import java.time.LocalDateTime;
import java.util.Set;

public record UsuarioResponse(
    Long idUsuario,
    String username,
    String nombreCompleto,
    String email,
    boolean activo,
    LocalDateTime fechaCreacion,
    Set<String> roles
) {}