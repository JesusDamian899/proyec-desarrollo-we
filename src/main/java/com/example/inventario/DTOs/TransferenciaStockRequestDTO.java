package com.example.inventario.DTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record TransferenciaStockRequestDTO(
        @NotNull @Positive Long loteId,
        @NotNull @Positive Long ubicacionOrigenId,
        @NotNull @Positive Long ubicacionDestinoId,
        @NotNull @Positive BigDecimal cantidad,
        @Size(max = 255) String motivo) {

    public TransferenciaStockRequestDTO(
            Long loteId,
            Long ubicacionOrigenId,
            Long ubicacionDestinoId,
            BigDecimal cantidad) {
        this(loteId, ubicacionOrigenId, ubicacionDestinoId, cantidad, null);
    }
}

