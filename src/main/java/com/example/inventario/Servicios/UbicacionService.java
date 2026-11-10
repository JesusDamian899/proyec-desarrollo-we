package com.example.inventario.Servicios;

import com.example.inventario.Entidades.Ubicacion;
import com.example.inventario.Repositorios.UbicacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UbicacionService {

    @Autowired
    private UbicacionRepository ubicacionRepository;

    @Transactional(readOnly = true)
    public List<Ubicacion> listarTodas() {
        return ubicacionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Ubicacion> obtenerPorId(Long id) {
        return ubicacionRepository.findById(id);
    }

    @Transactional
    public Ubicacion guardar(Ubicacion ubicacion) {
        if (ubicacion.getCodigo() != null && ubicacionRepository.existsByCodigoIgnoreCase(ubicacion.getCodigo().trim())) {
            throw new IllegalArgumentException("Ya existe una ubicación registrada con el código: " + ubicacion.getCodigo());
        }
        if (ubicacion.getCodigo() != null) {
            ubicacion.setCodigo(ubicacion.getCodigo().trim());
        }
        return ubicacionRepository.save(ubicacion);
    }

    @Transactional
    public Ubicacion actualizar(Long id, Ubicacion datosActualizados) {
        return ubicacionRepository.findById(id).map(ubicacionExistente -> {
            if (datosActualizados.getCodigo() != null && !datosActualizados.getCodigo().isBlank()) {
                String nuevoCodigo = datosActualizados.getCodigo().trim();
                if (!ubicacionExistente.getCodigo().equalsIgnoreCase(nuevoCodigo) &&
                    ubicacionRepository.existsByCodigoIgnoreCase(nuevoCodigo)) {
                    throw new IllegalArgumentException("El código " + nuevoCodigo + " ya pertenece a otra ubicación.");
                }
                ubicacionExistente.setCodigo(nuevoCodigo);
            }
            if (datosActualizados.getNombre() != null) {
                ubicacionExistente.setNombre(datosActualizados.getNombre());
            }
            if (datosActualizados.getTipo() != null) {
                ubicacionExistente.setTipo(datosActualizados.getTipo());
            }
            return ubicacionRepository.save(ubicacionExistente);
        }).orElseThrow(() -> new RecursoNoEncontradoException("Ubicación no encontrada con ID: " + id));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!ubicacionRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Ubicación no encontrada con ID: " + id);
        }
        ubicacionRepository.deleteById(id);
    }
}