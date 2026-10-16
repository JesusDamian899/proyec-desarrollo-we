package com.example.inventario.Controladores;

import com.example.inventario.DTOs.*;
import com.example.inventario.Entidades.Lote;
import com.example.inventario.Servicios.AlmacenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/almacen")
public class AlmacenController {

    private final AlmacenService almacenService;

    public AlmacenController(AlmacenService almacenService) {
        this.almacenService = almacenService;
    }

    @PostMapping("/ingresos")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoResponseDTO registrarIngreso(@Valid @RequestBody IngresoStockRequestDTO solicitud) {
        return almacenService.registrarIngreso(solicitud);
    }

    @PostMapping("/salidas")
    public List<MovimientoResponseDTO> registrarSalida(@Valid @RequestBody SalidaStockRequestDTO solicitud) {
        return almacenService.registrarSalidaFIFO(solicitud);
    }

    @PostMapping("/transferencias")
    public MovimientoResponseDTO registrarTransferencia(
            @Valid @RequestBody TransferenciaStockRequestDTO solicitud) {
        return almacenService.registrarTransferencia(solicitud);
    }

    @GetMapping("/lotes/vencimiento")
    public List<Lote> listarLotesProximosAVencer() {
        return almacenService.listarLotesProximosAVencer();
    }

    @GetMapping("/lotes/producto/{productoId}")
    public List<Lote> listarLotesPorProducto(@PathVariable Long productoId) {
        return almacenService.listarLotesPorProducto(productoId);
    }
}
