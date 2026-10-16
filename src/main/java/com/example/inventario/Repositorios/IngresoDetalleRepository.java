package com.example.inventario.Repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.inventario.Entidades.IngresoDetalle;


public interface IngresoDetalleRepository extends JpaRepository<IngresoDetalle, Integer> {
    // FIFO
    // Este query busca los lotes de un artículo ordenados desde el más antiguo al más nuevo.
    @Query("SELECT d FROM IngresoDetalle d WHERE d.articulo.idArticulo = :idArticulo ORDER BY d.ingresoCabecera.fechaRecepcion ASC")
    List<IngresoDetalle> encontrarLotesParaFifo(@Param("idArticulo") Integer idArticulo);
}