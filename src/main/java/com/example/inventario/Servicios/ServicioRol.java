package com.example.inventario.Servicios;


import java.util.List;

public interface ServicioRol {
    List<RolResponse> listar();
    RolResponse buscarPorId(Long id);
}
