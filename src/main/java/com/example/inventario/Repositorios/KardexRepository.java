package com.example.inventario.Repositorios;

import com.example.inventario.Entidades.Kardex;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface KardexRepository extends JpaRepository<Kardex, Integer> {

    @EntityGraph(attributePaths = {"articulo", "articulo.categoria", "articulo.unidadMedida", "articulo.ubicacion", "usuario"})
    List<Kardex> findByArticulo_IdArticuloOrderByFechaMovimientoDesc(Integer idArticulo);

    @EntityGraph(attributePaths = {"articulo", "articulo.categoria", "articulo.unidadMedida", "articulo.ubicacion", "usuario"})
    List<Kardex> findAllByOrderByFechaMovimientoDesc();

    @EntityGraph(attributePaths = {"articulo", "articulo.categoria", "articulo.unidadMedida", "articulo.ubicacion", "usuario"})
    List<Kardex> findByTipoMovimientoIgnoreCaseOrderByFechaMovimientoDesc(String tipoMovimiento);

    @EntityGraph(attributePaths = {"articulo", "articulo.categoria", "articulo.unidadMedida", "articulo.ubicacion", "usuario"})
    List<Kardex> findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(LocalDateTime inicio, LocalDateTime fin);
}