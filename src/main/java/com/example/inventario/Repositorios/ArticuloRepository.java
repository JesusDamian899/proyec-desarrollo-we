package com.example.inventario.Repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.inventario.Entidades.Articulo;


public interface ArticuloRepository extends JpaRepository<Articulo, Integer> {

    @Override
    @EntityGraph(attributePaths = {"categoria", "unidadMedida", "ubicacion"})
    List<Articulo> findAll();

    @Override
    @EntityGraph(attributePaths = {"categoria", "unidadMedida", "ubicacion"})
    Optional<Articulo> findById(Integer id);

    // Vital para la H.U.4: Validar que el código no se duplique
    boolean existsByCodigoInterno(String codigoInterno);

    @EntityGraph(attributePaths = {"categoria", "unidadMedida", "ubicacion"})
    Optional<Articulo> findByCodigoInterno(String codigoInterno);

    @EntityGraph(attributePaths = {"categoria", "unidadMedida", "ubicacion"})
    List<Articulo> findByDescripcionContainingIgnoreCase(String texto);

    @EntityGraph(attributePaths = {"categoria", "unidadMedida", "ubicacion"})
    @Query("SELECT a FROM Articulo a WHERE a.stockActual <= a.stockMinimo")
    List<Articulo> findArticulosBajoStock();
}