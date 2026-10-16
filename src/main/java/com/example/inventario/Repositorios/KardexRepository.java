package com.example.inventario.Repositorios;

import com.example.inventario.Entidades.Kardex;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface KardexRepository extends JpaRepository<Kardex, Integer> {


    List<Kardex> findByArticulo_IdArticuloOrderByFechaMovimientoDesc(Integer idArticulo);

    
    List<Kardex> findAllByOrderByFechaMovimientoDesc();

   
    List<Kardex> findByTipoMovimientoIgnoreCaseOrderByFechaMovimientoDesc(String tipoMovimiento);

  
    List<Kardex> findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(LocalDateTime inicio, LocalDateTime fin);
}