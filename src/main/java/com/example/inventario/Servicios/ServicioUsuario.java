package com.example.inventario.Servicios;

import com.example.inventario.DTOs.UsuarioRequest;
import com.example.inventario.DTOs.UsuarioResponse;

import java.util.List;

public interface ServicioUsuario {
    List<UsuarioResponse> listar();
    UsuarioResponse buscarPorId(Long id);
    UsuarioResponse guardar(UsuarioRequest peticion);
    UsuarioResponse cambiarEstado(Long id, boolean activo);
}