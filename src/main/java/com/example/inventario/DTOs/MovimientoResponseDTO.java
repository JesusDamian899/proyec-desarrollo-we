package com.example.inventario.DTOs;

import com.example.inventario.Entidades.MovimientoInventario;
import com.example.inventario.Entidades.TipoMovimiento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoResponseDTO(
        Long id,
        TipoMovimiento tipoMovimiento,
        Integer articuloId,
        String articuloCodigo,
        String articuloDescripcion,
        Long loteId,
        String codigoLote,
        BigDecimal cantidad,
        Long ubicacionOrigenId,
        String ubicacionOrigenNombre,
        Long ubicacionDestinoId,
        String ubicacionDestinoNombre,
        LocalDateTime fechaMovimiento) {

    public static MovimientoResponseDTO desde(MovimientoInventario movimiento) {
        return new MovimientoResponseDTO(
                movimiento.getId(),
                movimiento.getTipoMovimiento(),
                movimiento.getArticulo().getIdArticulo(),
                movimiento.getArticulo().getCodigoInterno(),
                movimiento.getArticulo().getDescripcion(),
                movimiento.getLote().getId(),
                movimiento.getLote().getCodigoLote(),
                movimiento.getCantidad(),
                movimiento.getUbicacionOrigen() == null ? null : movimiento.getUbicacionOrigen().getId(),
                movimiento.getUbicacionOrigen() == null ? null : movimiento.getUbicacionOrigen().getNombre(),
                movimiento.getUbicacionDestino() == null ? null : movimiento.getUbicacionDestino().getId(),
                movimiento.getUbicacionDestino() == null ? null : movimiento.getUbicacionDestino().getNombre(),
                movimiento.getFechaMovimiento());
    }
}

