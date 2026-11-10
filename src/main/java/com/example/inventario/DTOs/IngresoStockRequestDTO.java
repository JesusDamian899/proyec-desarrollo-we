package com.example.inventario.DTOs;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IngresoStockRequestDTO(
        @NotNull @Positive Integer articuloId,
        @NotBlank @Size(max = 80) String codigoLote,
        @NotNull @Positive BigDecimal cantidad,
        @NotNull @FutureOrPresent LocalDate fechaVencimiento,
        @NotNull @Positive Long ubicacionId,
        Integer proveedorId,
        @Size(max = 50) String documentoReferencia) {

    public IngresoStockRequestDTO(
            Integer articuloId,
            String codigoLote,
            BigDecimal cantidad,
            LocalDate fechaVencimiento,
            Long ubicacionId) {
        this(articuloId, codigoLote, cantidad, fechaVencimiento, ubicacionId, null, null);
    }
}

