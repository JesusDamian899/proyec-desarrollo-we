package com.example.inventario.Servicios;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.inventario.Entidades.Categoria;
import com.example.inventario.Repositorios.CategoriaRepository;

@Service 
public class CategoriaService {

    @Autowired 
    private CategoriaRepository categoriaRepository;

   
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    
    public Optional<Categoria> obtenerPorId(Integer id) {
        return categoriaRepository.findById(id);
    }

   
    public Categoria guardar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    
    public Categoria actualizar(Integer id, Categoria categoriaActualizada) {

        return categoriaRepository.findById(id).map(catExistente -> {
            catExistente.setNombre(categoriaActualizada.getNombre());
            return categoriaRepository.save(catExistente);
        }).orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con el ID: " + id));
    }


    public void eliminar(Integer id) {
        if (!categoriaRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Categoría no encontrada con el ID: " + id);
        }
        categoriaRepository.deleteById(id);
    }
}