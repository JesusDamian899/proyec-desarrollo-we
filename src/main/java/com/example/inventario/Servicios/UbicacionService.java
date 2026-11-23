package com.example.inventario.Servicios;

import com.example.inventario.Entidades.Ubicacion;
import com.example.inventario.Excepciones.RecursoNoEncontradoException;
import com.example.inventario.Excepciones.ReglaNegocioException;
import com.example.inventario.Repositorios.UbicacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UbicacionService {

    private final UbicacionRepository ubicacionRepository;

    public UbicacionService(UbicacionRepository ubicacionRepository) {
        this.ubicacionRepository = ubicacionRepository;
    }

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
        if (ubicacion.getCodigo() == null || ubicacion.getCodigo().isBlank()) {
            throw new IllegalArgumentException("El codigo de la ubicacion es obligatorio.");
        }
        if (ubicacion.getNombre() == null || ubicacion.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la ubicacion es obligatorio.");
        }
        String codigo = ubicacion.getCodigo().trim().toUpperCase();
        if (ubicacionRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ReglaNegocioException("Ya existe una ubicacion registrada con el codigo: " + codigo);
        }
        ubicacion.setCodigo(codigo);
        ubicacion.setNombre(ubicacion.getNombre().trim());
        if (ubicacion.getTipo() == null || ubicacion.getTipo().isBlank()) {
            ubicacion.setTipo("ESTANTERIA");
        } else {
            ubicacion.setTipo(ubicacion.getTipo().trim().toUpperCase());
        }
        return ubicacionRepository.save(ubicacion);
    }

    @Transactional
    public Ubicacion actualizar(Long id, Ubicacion datosActualizados) {
        return ubicacionRepository.findById(id).map(ubicacionExistente -> {
            if (datosActualizados.getCodigo() != null && !datosActualizados.getCodigo().isBlank()) {
                String nuevoCodigo = datosActualizados.getCodigo().trim().toUpperCase();
                if (!ubicacionExistente.getCodigo().equalsIgnoreCase(nuevoCodigo)
                        && ubicacionRepository.existsByCodigoIgnoreCase(nuevoCodigo)) {
                    throw new ReglaNegocioException("El codigo " + nuevoCodigo + " ya pertenece a otra ubicacion.");
                }
                ubicacionExistente.setCodigo(nuevoCodigo);
            }
            if (datosActualizados.getNombre() != null && !datosActualizados.getNombre().isBlank()) {
                ubicacionExistente.setNombre(datosActualizados.getNombre().trim());
            }
            if (datosActualizados.getTipo() != null && !datosActualizados.getTipo().isBlank()) {
                ubicacionExistente.setTipo(datosActualizados.getTipo().trim().toUpperCase());
            }
            return ubicacionRepository.save(ubicacionExistente);
        }).orElseThrow(() -> new RecursoNoEncontradoException("Ubicacion no encontrada con ID: " + id));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!ubicacionRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Ubicacion no encontrada con ID: " + id);
        }
        ubicacionRepository.deleteById(id);
    }
}