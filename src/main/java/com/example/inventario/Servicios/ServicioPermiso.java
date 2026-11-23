package com.example.inventario.Servicios;

import com.example.inventario.DTOs.PermisoResponse;

import java.util.List;

public interface ServicioPermiso {
    List<PermisoResponse> listar();
    PermisoResponse buscarPorId(Long id);
}
