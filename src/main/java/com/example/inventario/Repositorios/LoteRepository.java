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
    @Query("SELECT l FROM Lote l WHERE l.producto.id = :productoId "
            + "AND l.cantidad > 0 ORDER BY l.fechaVencimiento ASC, l.id ASC")
    List<Lote> buscarDisponiblesPorProductoFIFO(@Param("productoId") Long productoId);

    Optional<Lote> findByProducto_IdAndCodigoLoteAndUbicacion_Id(
            Long productoId, String codigoLote, Long ubicacionId);

    @EntityGraph(attributePaths = {"producto", "ubicacion"})
    @Query("SELECT l FROM Lote l WHERE l.fechaVencimiento BETWEEN :desde AND :hasta "
            + "AND l.cantidad > 0 ORDER BY l.fechaVencimiento ASC")
    List<Lote> buscarProximosAVencer(@Param("desde") LocalDate desde,
                                     @Param("hasta") LocalDate hasta);

    @EntityGraph(attributePaths = {"producto", "ubicacion"})
    List<Lote> findByProducto_IdOrderByFechaVencimientoAsc(Long productoId);
}
