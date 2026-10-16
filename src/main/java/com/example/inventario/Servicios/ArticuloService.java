package com.example.inventario.Servicios;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.inventario.Entidades.Articulo;
import com.example.inventario.Repositorios.ArticuloRepository;

@Service
public class ArticuloService {

    @Autowired
    private ArticuloRepository articuloRepository;

   
    public List<Articulo> listarTodos() {
        return articuloRepository.findAll();
    }

    
    public Optional<Articulo> obtenerPorId(Integer id) {
        return articuloRepository.findById(id);
    }

    
    public Optional<Articulo> obtenerPorCodigoInterno(String codigoInterno) {
        return articuloRepository.findByCodigoInterno(codigoInterno);
    }

    public Articulo guardar(Articulo articulo) {
        // validar que el código  no exista previamente
        if (articuloRepository.existsByCodigoInterno(articulo.getCodigoInterno())) {
            throw new IllegalArgumentException("Ya existe un artículo registrado con el código interno: " + articulo.getCodigoInterno());
        }

        if (articulo.getStockActual() == null) {
            articulo.setStockActual(BigDecimal.ZERO);
        }

        
        if (articulo.getStockMinimo() == null) {
            articulo.setStockMinimo(BigDecimal.ZERO);
        }

        return articuloRepository.save(articulo);
    }

    // Actualizar datos del artículo
    public Articulo actualizar(Integer id, Articulo datosActualizados) {
        return articuloRepository.findById(id).map(articuloExistente -> {
            
           
            if (!articuloExistente.getCodigoInterno().equalsIgnoreCase(datosActualizados.getCodigoInterno()) &&
                articuloRepository.existsByCodigoInterno(datosActualizados.getCodigoInterno())) {
                throw new IllegalArgumentException("El código interno " + datosActualizados.getCodigoInterno() + " ya pertenece a otro artículo.");
            }

            articuloExistente.setCodigoInterno(datosActualizados.getCodigoInterno());
            articuloExistente.setDescripcion(datosActualizados.getDescripcion());
            articuloExistente.setCategoria(datosActualizados.getCategoria());
            articuloExistente.setUnidadMedida(datosActualizados.getUnidadMedida());
            articuloExistente.setUbicacion(datosActualizados.getUbicacion());
            articuloExistente.setStockMinimo(datosActualizados.getStockMinimo());

            return articuloRepository.save(articuloExistente);
        }).orElseThrow(() -> new RuntimeException("Artículo no encontrado con ID: " + id));
    }

    // Eliminar un artículo por su ID
    public void eliminar(Integer id) {
        articuloRepository.deleteById(id);
    }

    // Buscar artículos 
    public List<Articulo> buscarPorDescripcion(String texto) {
        return articuloRepository.findByDescripcionContainingIgnoreCase(texto);
    }

    // Obtener lista de artículos con alerta de bajo stock 
    public List<Articulo> listarArticulosBajoStock() {
        return articuloRepository.findArticulosBajoStock();
    }
}