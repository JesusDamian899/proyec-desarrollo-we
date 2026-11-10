package com.example.inventario.Repositorios;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.inventario.Entidades.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    boolean existsByNombreIgnoreCase(String nombre);
    Optional<Categoria> findByNombreIgnoreCase(String nombre);
}

