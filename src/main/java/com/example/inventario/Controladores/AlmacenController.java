package com.example.inventario.Controladores;

import com.example.inventario.DTOs.*;
import com.example.inventario.Entidades.IngresoCabecera;
import com.example.inventario.Entidades.SalidaProduccionCabecera;
import com.example.inventario.Entidades.TipoMovimiento;
import com.example.inventario.Entidades.TransferenciaCabecera;
import com.example.inventario.Servicios.AlmacenService;
import com.example.inventario.Servicios.ArticuloService;
import com.example.inventario.Entidades.Articulo;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/almacen", "/api"})
@CrossOrigin(origins = "*")
public class AlmacenController {

    private final AlmacenService almacenService;
    private final ArticuloService articuloService;

    public AlmacenController(AlmacenService almacenService, ArticuloService articuloService) {
        this.almacenService = almacenService;
        this.articuloService = articuloService;
    }

    @GetMapping("/ingresos")
    public List<MovimientoResponseDTO> listarIngresos() {
        return almacenService.listarMovimientosPorTipo(TipoMovimiento.INGRESO);
    }

    @GetMapping("/ingresos/documentos")
    public List<IngresoCabecera> listarDocumentosIngreso() {
        return almacenService.listarDocumentosIngreso();
    }

    @PostMapping("/ingresos")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoResponseDTO registrarIngreso(@Valid @RequestBody IngresoStockRequestDTO solicitud) {
        return almacenService.registrarIngreso(solicitud);
    }

    @GetMapping("/salidas")
    public List<MovimientoResponseDTO> listarSalidas() {
        return almacenService.listarMovimientosPorTipo(TipoMovimiento.SALIDA);
    }

    @GetMapping("/salidas/documentos")
    public List<SalidaProduccionCabecera> listarDocumentosSalida() {
        return almacenService.listarDocumentosSalida();
    }

    @PostMapping("/salidas")
    public List<MovimientoResponseDTO> registrarSalida(@Valid @RequestBody SalidaStockRequestDTO solicitud) {
        return almacenService.registrarSalidaFIFO(solicitud);
    }

    @GetMapping("/transferencias")
    public List<MovimientoResponseDTO> listarTransferencias() {
        return almacenService.listarMovimientosPorTipo(TipoMovimiento.TRANSFERENCIA);
    }

    @GetMapping("/transferencias/documentos")
    public List<TransferenciaCabecera> listarDocumentosTransferencia() {
        return almacenService.listarDocumentosTransferencia();
    }

    @PostMapping("/transferencias")
    public MovimientoResponseDTO registrarTransferencia(
            @Valid @RequestBody TransferenciaStockRequestDTO solicitud) {
        return almacenService.registrarTransferencia(solicitud);
    }

    @GetMapping("/movimientos")
    public List<MovimientoResponseDTO> listarMovimientos() {
        return almacenService.listarMovimientos();
    }

    @GetMapping("/lotes")
    public List<LoteResponseDTO> listarLotes() {
        return almacenService.listarTodosLosLotes();
    }

    @GetMapping({"/lotes/vencimiento", "/reportes/vencimientos"})
    public List<LoteResponseDTO> listarLotesProximosAVencer() {
        return almacenService.listarLotesProximosAVencer();
    }

    @GetMapping("/reportes/minimos")
    public List<Articulo> listarArticulosBajoMinimo() {
        return articuloService.listarArticulosBajoStock();
    }

    @GetMapping("/lotes/articulo/{articuloId}")
    public List<LoteResponseDTO> listarLotesPorArticulo(@PathVariable Integer articuloId) {
        return almacenService.listarLotesPorArticulo(articuloId);
    }
}

