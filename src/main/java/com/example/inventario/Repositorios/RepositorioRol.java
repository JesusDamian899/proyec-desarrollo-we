package com.example.inventario.Repositorios;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import com.example.inventario.Entidades.Rol;

public interface RepositorioRol extends JpaRepository<Rol, Long> {

    @Override
    @EntityGraph(attributePaths = {"permisos"})
    List<Rol> findAll();

    @Override
    @EntityGraph(attributePaths = {"permisos"})
    Optional<Rol> findById(Long id);

    @EntityGraph(attributePaths = {"permisos"})
    Optional<Rol> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

}

