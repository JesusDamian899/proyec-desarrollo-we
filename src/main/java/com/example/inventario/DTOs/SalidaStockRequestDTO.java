package com.example.inventario.DTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record SalidaStockRequestDTO(
        @NotNull @Positive Integer articuloId,
        @NotNull @Positive BigDecimal cantidadRequerida) {
}
