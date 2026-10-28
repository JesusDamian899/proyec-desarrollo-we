package com.example.inventario.DTOs;

import com.example.inventario.Entidades.EstadoLote;
import com.example.inventario.Entidades.Lote;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoteResponseDTO(
        Long id,
        String codigoLote,
        Integer articuloId,
        String articuloCodigo,
        String articuloDescripcion,
        Long ubicacionId,
        String ubicacionNombre,
        String ubicacionCodigo,
        BigDecimal cantidad,
        LocalDate fechaVencimiento,
        LocalDate fechaIngreso,
        EstadoLote estado) {

    public static LoteResponseDTO desde(Lote lote) {
        return new LoteResponseDTO(
                lote.getId(),
                lote.getCodigoLote(),
                lote.getArticulo().getIdArticulo(),
                lote.getArticulo().getCodigoInterno(),
                lote.getArticulo().getDescripcion(),
                lote.getUbicacion().getId(),
                lote.getUbicacion().getNombre(),
                lote.getUbicacion().getCodigo(),
                lote.getCantidad(),
                lote.getFechaVencimiento(),
                lote.getFechaIngreso(),
                lote.getEstado());
    }
}
