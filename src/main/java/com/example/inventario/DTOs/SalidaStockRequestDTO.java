package com.example.inventario.DTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record SalidaStockRequestDTO(
        @NotNull @Positive Integer articuloId,
        @NotNull @Positive BigDecimal cantidadRequerida,
        @Size(max = 50) String ordenProduccion,
        Long usuarioSolicitanteId) {

    public SalidaStockRequestDTO(Integer articuloId, BigDecimal cantidadRequerida) {
        this(articuloId, cantidadRequerida, null, null);
    }
}

