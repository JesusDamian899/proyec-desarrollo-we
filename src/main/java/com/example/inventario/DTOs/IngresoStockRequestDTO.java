package com.example.inventario.DTOs;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IngresoStockRequestDTO(
        @NotNull @Positive Long productoId,
        @NotBlank @Size(max = 80) String codigoLote,
        @NotNull @Positive BigDecimal cantidad,
        @NotNull @FutureOrPresent LocalDate fechaVencimiento,
        @NotNull @Positive Long ubicacionId) {
}
