package com.example.inventario.Servicios;
import java.util.List;

public record LoginResponse(
    String token,
    String tipo,
    Long idUsuario,
    String username,
    String nombreCompleto,
    List<String> roles,
    List<String> permisos,
    long expiraEnMs
) {}