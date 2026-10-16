package com.example.inventario.DTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransferenciaStockRequestDTO(
        @NotNull @Positive Long loteId,
        @NotNull @Positive Long ubicacionOrigenId,
        @NotNull @Positive Long ubicacionDestinoId,
        @NotNull @Positive BigDecimal cantidad) {
}
