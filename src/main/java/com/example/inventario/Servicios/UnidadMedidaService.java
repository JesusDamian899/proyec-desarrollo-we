package com.example.inventario.Servicios;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.inventario.Entidades.UnidadMedida;
import com.example.inventario.Repositorios.UnidadMedidaRepository;

@Service
public class UnidadMedidaService {

    @Autowired
    private UnidadMedidaRepository unidadMedidaRepository;

    // 1. Obtener todas las unidades de medida registradas
    public List<UnidadMedida> listarTodas() {
        return unidadMedidaRepository.findAll();
    }

    // 2. Buscar una unidad específica por su identificador único
    public Optional<UnidadMedida> obtenerPorId(Integer id) {
        return unidadMedidaRepository.findById(id);
    }

    // 3. Registrar una nueva unidad de medida
    public UnidadMedida guardar(UnidadMedida unidadMedida) {
        return unidadMedidaRepository.save(unidadMedida);
    }

    // 4. Modificar nombre y abreviatura de una unidad existente
    public UnidadMedida actualizar(Integer id, UnidadMedida datosActualizados) {
        return unidadMedidaRepository.findById(id).map(unidadExistente -> {
            unidadExistente.setNombre(datosActualizados.getNombre());
            unidadExistente.setAbreviatura(datosActualizados.getAbreviatura());
            return unidadMedidaRepository.save(unidadExistente);
        }).orElseThrow(() -> new RuntimeException("Unidad de medida no encontrada con ID: " + id));
    }

    // 5. Eliminar la unidad de medida por su ID
    public void eliminar(Integer id) {
        unidadMedidaRepository.deleteById(id);
    }
}