package com.example.inventario.Servicios;


import com.example.inventario.Entidades.Permiso;
import com.example.inventario.Entidades.Rol;
import com.example.inventario.Repositorios.RepositorioRol;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class ImplServicioRol implements ServicioRol {

    private final RepositorioRol repositorio;

    public ImplServicioRol(RepositorioRol repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolResponse> listar() {
        return repositorio.findAll().stream().map(this::aDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RolResponse buscarPorId(Long id) {
        Rol rol = repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El rol " + id + " no existe"));
        return aDto(rol);
    }

    private RolResponse aDto(Rol rol) {
        return new RolResponse(
                rol.getIdRol(),
                rol.getNombre(),
                rol.getDescripcion(),
                rol.isActivo(),
                rol.getPermisos().stream()
                   .map(Permiso::getCodigo)
                   .collect(Collectors.toCollection(TreeSet::new)));
    }
}