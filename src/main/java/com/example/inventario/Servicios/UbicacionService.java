package com.example.inventario.Servicios;

import com.example.inventario.Entidades.Ubicacion;
import com.example.inventario.Repositorios.UbicacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UbicacionService {

    @Autowired
    private UbicacionRepository ubicacionRepository;

    public List<Ubicacion> listarTodas() {
        return ubicacionRepository.findAll();
    }

    public Optional<Ubicacion> obtenerPorId(Long id) {
        return ubicacionRepository.findById(id);
    }

    public Ubicacion guardar(Ubicacion ubicacion) {
        return ubicacionRepository.save(ubicacion);
    }

    public Ubicacion actualizar(Long id, Ubicacion datosActualizados) {
        return ubicacionRepository.findById(id).map(ubicacionExistente -> {
            ubicacionExistente.setNombre(datosActualizados.getNombre());
            ubicacionExistente.setCodigo(datosActualizados.getCodigo());
            ubicacionExistente.setTipo(datosActualizados.getTipo());
            return ubicacionRepository.save(ubicacionExistente);
        }).orElseThrow(() -> new RuntimeException("Ubicación no encontrada con ID: " + id));
    }

    public void eliminar(Long id) {
        ubicacionRepository.deleteById(id);
    }
}