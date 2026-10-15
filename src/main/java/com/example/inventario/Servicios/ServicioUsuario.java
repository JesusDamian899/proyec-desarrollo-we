package com.example.inventario.Servicios;

import java.util.List;

public interface ServicioUsuario {
    List<UsuarioResponse> listar();
    UsuarioResponse buscarPorId(Long id);
    UsuarioResponse guardar(UsuarioRequest peticion);
}