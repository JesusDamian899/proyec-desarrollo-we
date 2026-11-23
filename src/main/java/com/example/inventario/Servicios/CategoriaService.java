package com.example.inventario.Servicios;

import com.example.inventario.Entidades.Categoria;
import com.example.inventario.Excepciones.RecursoNoEncontradoException;
import com.example.inventario.Excepciones.ReglaNegocioException;
import com.example.inventario.Repositorios.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Categoria> obtenerPorId(Integer id) {
        return categoriaRepository.findById(id);
    }

    @Transactional
    public Categoria guardar(Categoria categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoria es obligatorio.");
        }
        String nombre = categoria.getNombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("La categoria '" + nombre + "' ya existe.");
        }
        categoria.setNombre(nombre);
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria actualizar(Integer id, Categoria categoriaActualizada) {
        if (categoriaActualizada.getNombre() == null || categoriaActualizada.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoria es obligatorio.");
        }
        String nuevoNombre = categoriaActualizada.getNombre().trim();
        return categoriaRepository.findById(id).map(catExistente -> {
            if (!catExistente.getNombre().equalsIgnoreCase(nuevoNombre)
                    && categoriaRepository.existsByNombreIgnoreCase(nuevoNombre)) {
                throw new ReglaNegocioException("La categoria '" + nuevoNombre + "' ya existe.");
            }
            catExistente.setNombre(nuevoNombre);
            return categoriaRepository.save(catExistente);
        }).orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada con el ID: " + id));
    }

    @Transactional
    public void eliminar(Integer id) {
        if (!categoriaRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Categoria no encontrada con el ID: " + id);
        }
        categoriaRepository.deleteById(id);
    }
}