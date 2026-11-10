package com.example.inventario.Repositorios;

import com.example.inventario.Entidades.Lote;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Lote l WHERE l.articulo.idArticulo = :articuloId "
            + "AND l.cantidad > 0 ORDER BY l.fechaIngreso ASC, l.id ASC")
    List<Lote> buscarDisponiblesPorArticuloFIFO(@Param("articuloId") Integer articuloId);

    Optional<Lote> findByArticulo_IdArticuloAndCodigoLoteAndUbicacion_Id(
            Integer articuloId, String codigoLote, Long ubicacionId);

    @EntityGraph(attributePaths = {"articulo", "ubicacion"})
    @Query("SELECT l FROM Lote l WHERE l.fechaVencimiento BETWEEN :desde AND :hasta "
            + "AND l.cantidad > 0 ORDER BY l.fechaVencimiento ASC")
    List<Lote> buscarProximosAVencer(@Param("desde") LocalDate desde,
                                     @Param("hasta") LocalDate hasta);

    @EntityGraph(attributePaths = {"articulo", "ubicacion"})
    List<Lote> findByArticulo_IdArticuloOrderByFechaVencimientoAsc(Integer articuloId);

    @EntityGraph(attributePaths = {"articulo", "ubicacion"})
    List<Lote> findAllByOrderByFechaVencimientoAsc();
}

