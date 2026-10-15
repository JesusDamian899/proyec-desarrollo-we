package com.example.inventario.Servicios;
import com.example.inventario.Entidades.Permiso;
import com.example.inventario.Repositorios.RepositorioPermiso;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ImplServicioPermiso implements ServicioPermiso {

    private final RepositorioPermiso repositorio;

    public ImplServicioPermiso(RepositorioPermiso repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermisoResponse> listar() {
        return repositorio.findAllByOrderByOrdenAsc()
                .stream()
                .map(this::aDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PermisoResponse buscarPorId(Long id) {
        Permiso permiso = repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El permiso " + id + " no existe"));
        return aDto(permiso);
    }

    private PermisoResponse aDto(Permiso p) {
        return new PermisoResponse(
                p.getIdPermiso(),
                p.getCodigo(),
                p.getNombre(),
                p.getModulo(),
                p.getRuta(),
                p.getTipo().name(),
                p.isMostrarEnMenu(),
                p.getOrden());
    }
}