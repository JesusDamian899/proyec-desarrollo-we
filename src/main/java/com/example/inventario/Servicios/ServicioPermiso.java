package com.example.inventario.Servicios;
import java.util.List;

public interface ServicioPermiso {
    List<PermisoResponse> listar();
    PermisoResponse buscarPorId(Long id);
}
