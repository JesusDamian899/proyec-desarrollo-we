package com.example.inventario.Repositorios;

import com.example.inventario.Entidades.MovimientoInventario;
import com.example.inventario.Entidades.TipoMovimiento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    @EntityGraph(attributePaths = {"articulo", "lote", "ubicacionOrigen", "ubicacionDestino"})
    List<MovimientoInventario> findAllByOrderByFechaMovimientoDesc();

    @EntityGraph(attributePaths = {"articulo", "lote", "ubicacionOrigen", "ubicacionDestino"})
    List<MovimientoInventario> findByTipoMovimientoOrderByFechaMovimientoDesc(TipoMovimiento tipoMovimiento);
}

