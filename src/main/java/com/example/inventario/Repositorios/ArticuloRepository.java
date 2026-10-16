package com.example.inventario.Repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.inventario.Entidades.Articulo;


public interface ArticuloRepository extends JpaRepository<Articulo, Integer> {
    // Vital para la H.U.4: Validar que el código no se duplique
    boolean existsByCodigoInterno(String codigoInterno);
    Optional<Articulo> findByCodigoInterno(String codigoInterno);

    List<Articulo> findByDescripcionContainingIgnoreCase(String texto);

    @Query("SELECT a FROM Articulo a WHERE a.stockActual <= a.stockMinimo")
    List<Articulo> findArticulosBajoStock();
}