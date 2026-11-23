package com.example.inventario.Servicios;

import com.example.inventario.Entidades.UnidadMedida;
import com.example.inventario.Excepciones.RecursoNoEncontradoException;
import com.example.inventario.Excepciones.ReglaNegocioException;
import com.example.inventario.Repositorios.UnidadMedidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UnidadMedidaService {

    private final UnidadMedidaRepository unidadMedidaRepository;

    public UnidadMedidaService(UnidadMedidaRepository unidadMedidaRepository) {
        this.unidadMedidaRepository = unidadMedidaRepository;
    }

    @Transactional(readOnly = true)
    public List<UnidadMedida> listarTodas() {
        return unidadMedidaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<UnidadMedida> obtenerPorId(Integer id) {
        return unidadMedidaRepository.findById(id);
    }

    @Transactional
    public UnidadMedida guardar(UnidadMedida unidadMedida) {
        if (unidadMedida.getNombre() == null || unidadMedida.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la unidad de medida es obligatorio.");
        }
        if (unidadMedida.getAbreviatura() == null || unidadMedida.getAbreviatura().isBlank()) {
            throw new IllegalArgumentException("La abreviatura de la unidad de medida es obligatoria.");
        }
        String abrev = unidadMedida.getAbreviatura().trim().toUpperCase();
        if (unidadMedidaRepository.existsByAbreviaturaIgnoreCase(abrev)) {
            throw new ReglaNegocioException("La abreviatura '" + abrev + "' ya esta registrada.");
        }
        unidadMedida.setNombre(unidadMedida.getNombre().trim());
        unidadMedida.setAbreviatura(abrev);
        return unidadMedidaRepository.save(unidadMedida);
    }

    @Transactional
    public UnidadMedida actualizar(Integer id, UnidadMedida datosActualizados) {
        if (datosActualizados.getNombre() == null || datosActualizados.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la unidad de medida es obligatorio.");
        }
        if (datosActualizados.getAbreviatura() == null || datosActualizados.getAbreviatura().isBlank()) {
            throw new IllegalArgumentException("La abreviatura de la unidad de medida es obligatoria.");
        }
        String nuevaAbrev = datosActualizados.getAbreviatura().trim().toUpperCase();
        return unidadMedidaRepository.findById(id).map(unidadExistente -> {
            if (!unidadExistente.getAbreviatura().equalsIgnoreCase(nuevaAbrev)
                    && unidadMedidaRepository.existsByAbreviaturaIgnoreCase(nuevaAbrev)) {
                throw new ReglaNegocioException("La abreviatura '" + nuevaAbrev + "' ya esta registrada.");
            }
            unidadExistente.setNombre(datosActualizados.getNombre().trim());
            unidadExistente.setAbreviatura(nuevaAbrev);
            return unidadMedidaRepository.save(unidadExistente);
        }).orElseThrow(() -> new RecursoNoEncontradoException("Unidad de medida no encontrada con ID: " + id));
    }

    @Transactional
    public void eliminar(Integer id) {
        if (!unidadMedidaRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Unidad de medida no encontrada con ID: " + id);
        }
        unidadMedidaRepository.deleteById(id);
    }
}