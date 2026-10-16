package com.example.inventario.Repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.inventario.Entidades.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
}
