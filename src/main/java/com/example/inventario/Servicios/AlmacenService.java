package com.example.inventario.Servicios;

import com.example.inventario.DTOs.*;
import com.example.inventario.Entidades.*;
import com.example.inventario.Repositorios.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlmacenService {

    private static final long DIAS_PROXIMO_VENCIMIENTO = 30;

    private final ProductoRepository productoRepository;
    private final UbicacionRepository ubicacionRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public AlmacenService(ProductoRepository productoRepository,
                          UbicacionRepository ubicacionRepository,
                          LoteRepository loteRepository,
                          MovimientoInventarioRepository movimientoRepository) {
        this.productoRepository = productoRepository;
        this.ubicacionRepository = ubicacionRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Transactional
    public MovimientoResponseDTO registrarIngreso(IngresoStockRequestDTO solicitud) {
        Producto producto = productoRepository.findById(solicitud.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
        Ubicacion ubicacion = ubicacionRepository.findById(solicitud.ubicacionId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Ubicacion no encontrada"));

        Lote lote = loteRepository.findByProducto_IdAndCodigoLoteAndUbicacion_Id(
                        solicitud.productoId(), solicitud.codigoLote(), solicitud.ubicacionId())
                .orElseGet(Lote::new);

        if (lote.getId() == null) {
            lote.setProducto(producto);
            lote.setCodigoLote(solicitud.codigoLote());
            lote.setCantidad(BigDecimal.ZERO);
            lote.setFechaIngreso(LocalDate.now());
        }
        lote.setUbicacion(ubicacion);
        lote.setFechaVencimiento(solicitud.fechaVencimiento());
        lote.setCantidad(lote.getCantidad().add(solicitud.cantidad()));
        lote.setEstado(EstadoLote.DISPONIBLE);
        lote = loteRepository.save(lote);

        MovimientoInventario movimiento = crearMovimiento(
                TipoMovimiento.INGRESO, producto, lote, solicitud.cantidad(), null, ubicacion);
        return MovimientoResponseDTO.desde(movimientoRepository.save(movimiento));
    }

    @Transactional
    public List<MovimientoResponseDTO> registrarSalidaFIFO(SalidaStockRequestDTO solicitud) {
        Producto producto = productoRepository.findById(solicitud.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
        List<Lote> lotes = loteRepository.buscarDisponiblesPorProductoFIFO(solicitud.productoId());
        BigDecimal disponible = lotes.stream()
                .map(Lote::getCantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (disponible.compareTo(solicitud.cantidadRequerida()) < 0) {
            throw new ReglaNegocioException("Stock insuficiente para el producto solicitado");
        }

        BigDecimal pendiente = solicitud.cantidadRequerida();
        List<MovimientoResponseDTO> movimientos = new java.util.ArrayList<>();
        for (Lote lote : lotes) {
            if (pendiente.signum() == 0) {
                break;
            }
            BigDecimal descontar = lote.getCantidad().min(pendiente);
            Ubicacion origen = lote.getUbicacion();
            lote.setCantidad(lote.getCantidad().subtract(descontar));
            if (lote.getCantidad().signum() == 0) {
                lote.setEstado(EstadoLote.AGOTADO);
            }
            loteRepository.save(lote);

            MovimientoInventario movimiento = crearMovimiento(
                    TipoMovimiento.SALIDA, producto, lote, descontar, origen, null);
            movimientos.add(MovimientoResponseDTO.desde(movimientoRepository.save(movimiento)));
            pendiente = pendiente.subtract(descontar);
        }
        return movimientos;
    }

    @Transactional
    public MovimientoResponseDTO registrarTransferencia(TransferenciaStockRequestDTO solicitud) {
        if (solicitud.ubicacionOrigenId().equals(solicitud.ubicacionDestinoId())) {
            throw new ReglaNegocioException("La ubicacion de origen y destino deben ser diferentes");
        }

        Lote lote = loteRepository.findById(solicitud.loteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Lote no encontrado"));
        Ubicacion origen = ubicacionRepository.findById(solicitud.ubicacionOrigenId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Ubicacion de origen no encontrada"));
        Ubicacion destino = ubicacionRepository.findById(solicitud.ubicacionDestinoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Ubicacion de destino no encontrada"));

        if (!lote.getUbicacion().getId().equals(origen.getId())) {
            throw new ReglaNegocioException("El lote no se encuentra en la ubicacion de origen");
        }
        if (lote.getCantidad().compareTo(solicitud.cantidad()) < 0) {
            throw new ReglaNegocioException("La cantidad del lote es insuficiente");
        }

        // El lote conserva su identidad y se cambia de ubicación como una unidad.
        lote.setCantidad(lote.getCantidad().subtract(solicitud.cantidad()));
        lote.setEstado(lote.getCantidad().signum() == 0 ? EstadoLote.AGOTADO : EstadoLote.DISPONIBLE);
        loteRepository.save(lote);

        Lote loteDestino = loteRepository.findByProducto_IdAndCodigoLoteAndUbicacion_Id(
                lote.getProducto().getId(), lote.getCodigoLote(), destino.getId()).orElse(null);
        if (loteDestino != null) {
            loteDestino.setCantidad(loteDestino.getCantidad().add(solicitud.cantidad()));
            loteDestino.setEstado(EstadoLote.DISPONIBLE);
            loteRepository.save(loteDestino);
        } else {
            Lote nuevoLote = new Lote();
            nuevoLote.setCodigoLote(lote.getCodigoLote());
            nuevoLote.setProducto(lote.getProducto());
            nuevoLote.setUbicacion(destino);
            nuevoLote.setCantidad(solicitud.cantidad());
            nuevoLote.setFechaVencimiento(lote.getFechaVencimiento());
            nuevoLote.setFechaIngreso(lote.getFechaIngreso());
            nuevoLote.setEstado(EstadoLote.DISPONIBLE);
            loteRepository.save(nuevoLote);
        }

        MovimientoInventario movimiento = crearMovimiento(
                TipoMovimiento.TRANSFERENCIA, lote.getProducto(), lote,
                solicitud.cantidad(), origen, destino);
        return MovimientoResponseDTO.desde(movimientoRepository.save(movimiento));
    }

    @Transactional(readOnly = true)
    public List<Lote> listarLotesProximosAVencer() {
        LocalDate hoy = LocalDate.now();
        return loteRepository.buscarProximosAVencer(hoy, hoy.plusDays(DIAS_PROXIMO_VENCIMIENTO));
    }

    @Transactional(readOnly = true)
    public List<Lote> listarLotesPorProducto(Long productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new RecursoNoEncontradoException("Producto no encontrado");
        }
        return loteRepository.findByProducto_IdOrderByFechaVencimientoAsc(productoId);
    }

    private MovimientoInventario crearMovimiento(TipoMovimiento tipo, Producto producto,
                                                  Lote lote, BigDecimal cantidad,
                                                  Ubicacion origen, Ubicacion destino) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipoMovimiento(tipo);
        movimiento.setProducto(producto);
        movimiento.setLote(lote);
        movimiento.setCantidad(cantidad);
        movimiento.setUbicacionOrigen(origen);
        movimiento.setUbicacionDestino(destino);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        return movimiento;
    }
}
