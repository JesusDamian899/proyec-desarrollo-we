package com.example.inventario.Servicios;

import com.example.inventario.DTOs.RolResponse;

import java.util.List;

public interface ServicioRol {
    List<RolResponse> listar();
    RolResponse buscarPorId(Long id);
}
