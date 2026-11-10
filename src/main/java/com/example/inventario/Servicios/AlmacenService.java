package com.example.inventario.Servicios;

import com.example.inventario.DTOs.*;
import com.example.inventario.Entidades.*;
import com.example.inventario.Repositorios.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlmacenService {

    private static final long DIAS_PROXIMO_VENCIMIENTO = 30;

    private final ArticuloRepository articuloRepository;
    private final UbicacionRepository ubicacionRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final KardexRepository kardexRepository;
    private final IngresoCabeceraRepository ingresoCabeceraRepository;
    private final IngresoDetalleRepository ingresoDetalleRepository;
    private final SalidaProduccionCabeceraRepository salidaProduccionCabeceraRepository;
    private final TransferenciaCabeceraRepository transferenciaCabeceraRepository;
    private final ProveedorRepository proveedorRepository;
    private final RepositorioUsuario repositorioUsuario;

    public AlmacenService(ArticuloRepository articuloRepository,
                          UbicacionRepository ubicacionRepository,
                          LoteRepository loteRepository,
                          MovimientoInventarioRepository movimientoRepository,
                          KardexRepository kardexRepository,
                          IngresoCabeceraRepository ingresoCabeceraRepository,
                          IngresoDetalleRepository ingresoDetalleRepository,
                          SalidaProduccionCabeceraRepository salidaProduccionCabeceraRepository,
                          TransferenciaCabeceraRepository transferenciaCabeceraRepository,
                          ProveedorRepository proveedorRepository,
                          RepositorioUsuario repositorioUsuario) {
        this.articuloRepository = articuloRepository;
        this.ubicacionRepository = ubicacionRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.kardexRepository = kardexRepository;
        this.ingresoCabeceraRepository = ingresoCabeceraRepository;
        this.ingresoDetalleRepository = ingresoDetalleRepository;
        this.salidaProduccionCabeceraRepository = salidaProduccionCabeceraRepository;
        this.transferenciaCabeceraRepository = transferenciaCabeceraRepository;
        this.proveedorRepository = proveedorRepository;
        this.repositorioUsuario = repositorioUsuario;
    }

    @Transactional
    public MovimientoResponseDTO registrarIngreso(IngresoStockRequestDTO solicitud) {
        Articulo articulo = articuloRepository.findById(solicitud.articuloId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Articulo no encontrado"));
        Ubicacion ubicacion = ubicacionRepository.findById(solicitud.ubicacionId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Ubicacion no encontrada"));

        Proveedor proveedor = null;
        if (solicitud.proveedorId() != null) {
            proveedor = proveedorRepository.findById(solicitud.proveedorId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado"));
        } else {
            proveedor = proveedorRepository.findAll().stream().findFirst().orElse(null);
        }

        Lote lote = loteRepository.findByArticulo_IdArticuloAndCodigoLoteAndUbicacion_Id(
                        solicitud.articuloId(), solicitud.codigoLote(), solicitud.ubicacionId())
                .orElseGet(Lote::new);

        if (lote.getId() == null) {
            lote.setArticulo(articulo);
            lote.setCodigoLote(solicitud.codigoLote());
            lote.setCantidad(BigDecimal.ZERO);
            lote.setFechaIngreso(LocalDate.now());
        }
        lote.setUbicacion(ubicacion);
        lote.setFechaVencimiento(solicitud.fechaVencimiento());
        lote.setCantidad(lote.getCantidad().add(solicitud.cantidad()));
        lote.setEstado(EstadoLote.DISPONIBLE);
        lote = loteRepository.save(lote);

        actualizarStockActual(articulo, solicitud.cantidad());

        MovimientoInventario movimiento = crearMovimiento(
                TipoMovimiento.INGRESO, articulo, lote, solicitud.cantidad(), null, ubicacion);
        movimiento = movimientoRepository.save(movimiento);

        String docRef = (solicitud.documentoReferencia() != null && !solicitud.documentoReferencia().isBlank())
                ? solicitud.documentoReferencia().trim()
                : "ING-" + solicitud.codigoLote();

        Usuario usuarioActual = obtenerUsuarioActual();
        if (usuarioActual != null) {
            IngresoCabecera cabecera = new IngresoCabecera();
            cabecera.setProveedor(proveedor);
            cabecera.setDocumentoReferencia(docRef);
            cabecera.setFechaRecepcion(LocalDateTime.now());
            cabecera.setUsuario(usuarioActual);
            cabecera.setEstado("COMPLETADO");

            IngresoDetalle detalle = new IngresoDetalle();
            detalle.setIngresoCabecera(cabecera);
            detalle.setArticulo(articulo);
            detalle.setCantidadRecibida(solicitud.cantidad());
            detalle.setNumeroLote(solicitud.codigoLote());
            detalle.setFechaVencimiento(solicitud.fechaVencimiento().atStartOfDay());
            cabecera.getDetalles().add(detalle);
            ingresoCabeceraRepository.save(cabecera);

            registrarEnKardex(articulo, "INGRESO", docRef, solicitud.cantidad(), articulo.getStockActual(), usuarioActual);
        }

        return MovimientoResponseDTO.desde(movimiento);
    }

    @Transactional
    public List<MovimientoResponseDTO> registrarSalidaFIFO(SalidaStockRequestDTO solicitud) {
        Articulo articulo = articuloRepository.findById(solicitud.articuloId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Articulo no encontrado"));
        List<Lote> lotes = new java.util.ArrayList<>(loteRepository.buscarDisponiblesPorArticuloFIFO(solicitud.articuloId()));
        BigDecimal disponible = lotes.stream()
                .map(Lote::getCantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Si el artículo tenía stockActual inicial mayor a la suma de lotes registrados, sincronizar un lote base
        BigDecimal stockArticulo = articulo.getStockActual() == null ? BigDecimal.ZERO : articulo.getStockActual();
        if (disponible.compareTo(solicitud.cantidadRequerida()) < 0 && stockArticulo.compareTo(disponible) > 0) {
            BigDecimal diferencia = stockArticulo.subtract(disponible);
            Lote loteBase = new Lote();
            loteBase.setArticulo(articulo);
            loteBase.setCodigoLote("LOTE-BASE-" + articulo.getCodigoInterno());
            loteBase.setUbicacion(articulo.getUbicacion());
            loteBase.setCantidad(diferencia);
            loteBase.setFechaIngreso(LocalDate.now());
            loteBase.setFechaVencimiento(LocalDate.now().plusYears(1));
            loteBase.setEstado(EstadoLote.DISPONIBLE);
            loteBase = loteRepository.save(loteBase);
            lotes.add(loteBase);
            disponible = disponible.add(diferencia);
        }

        if (disponible.compareTo(solicitud.cantidadRequerida()) < 0) {
            throw new ReglaNegocioException("Stock insuficiente para el articulo solicitado");
        }

        Usuario usuarioDespacha = obtenerUsuarioActual();
        Usuario usuarioSolicitante = usuarioDespacha;
        if (solicitud.usuarioSolicitanteId() != null) {
            usuarioSolicitante = repositorioUsuario.findById(solicitud.usuarioSolicitanteId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Usuario solicitante no encontrado"));
        }

        String ordenProd = (solicitud.ordenProduccion() != null && !solicitud.ordenProduccion().isBlank())
                ? solicitud.ordenProduccion().trim()
                : "OP-" + articulo.getCodigoInterno() + "-" + (System.currentTimeMillis() % 100000);

        SalidaProduccionCabecera salidaCabecera = null;
        if (usuarioDespacha != null) {
            salidaCabecera = new SalidaProduccionCabecera();
            salidaCabecera.setOrdenProduccion(ordenProd);
            salidaCabecera.setFechaSalida(LocalDateTime.now());
            salidaCabecera.setUsuarioDespacha(usuarioDespacha);
            salidaCabecera.setUsuarioSolicitante(usuarioSolicitante);
            salidaCabecera.setEstado("COMPLETADO");
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
                    TipoMovimiento.SALIDA, articulo, lote, descontar, origen, null);
            movimientos.add(MovimientoResponseDTO.desde(movimientoRepository.save(movimiento)));

            if (salidaCabecera != null) {
                SalidaProduccionDetalle detalleSalida = new SalidaProduccionDetalle();
                detalleSalida.setSalidaCabecera(salidaCabecera);
                detalleSalida.setArticulo(articulo);
                detalleSalida.setCantidadEntregada(descontar);
                IngresoDetalle loteIngreso = ingresoDetalleRepository
                        .findFirstByArticulo_IdArticuloAndNumeroLoteOrderByIngresoCabecera_FechaRecepcionAsc(
                                articulo.getIdArticulo(), lote.getCodigoLote())
                        .orElse(null);
                detalleSalida.setLoteIngreso(loteIngreso);
                salidaCabecera.getDetalles().add(detalleSalida);
            }

            pendiente = pendiente.subtract(descontar);
        }
        actualizarStockActual(articulo, solicitud.cantidadRequerida().negate());

        if (salidaCabecera != null) {
            salidaProduccionCabeceraRepository.save(salidaCabecera);
            registrarEnKardex(articulo, "SALIDA", ordenProd, solicitud.cantidadRequerida(),
                    articulo.getStockActual(), usuarioDespacha);
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

        Lote loteDestino = loteRepository.findByArticulo_IdArticuloAndCodigoLoteAndUbicacion_Id(
                lote.getArticulo().getIdArticulo(), lote.getCodigoLote(), destino.getId()).orElse(null);
        if (loteDestino != null) {
            loteDestino.setCantidad(loteDestino.getCantidad().add(solicitud.cantidad()));
            loteDestino.setEstado(EstadoLote.DISPONIBLE);
            loteRepository.save(loteDestino);
        } else {
            Lote nuevoLote = new Lote();
            nuevoLote.setCodigoLote(lote.getCodigoLote());
            nuevoLote.setArticulo(lote.getArticulo());
            nuevoLote.setUbicacion(destino);
            nuevoLote.setCantidad(solicitud.cantidad());
            nuevoLote.setFechaVencimiento(lote.getFechaVencimiento());
            nuevoLote.setFechaIngreso(lote.getFechaIngreso());
            nuevoLote.setEstado(EstadoLote.DISPONIBLE);
            loteRepository.save(nuevoLote);
        }

        MovimientoInventario movimiento = crearMovimiento(
                TipoMovimiento.TRANSFERENCIA, lote.getArticulo(), lote,
                solicitud.cantidad(), origen, destino);
        movimiento = movimientoRepository.save(movimiento);

        String motivoTexto = (solicitud.motivo() != null && !solicitud.motivo().isBlank())
                ? solicitud.motivo().trim()
                : "Transferencia de " + origen.getCodigo() + " a " + destino.getCodigo();

        Usuario usuarioActual = obtenerUsuarioActual();
        if (usuarioActual != null) {
            TransferenciaCabecera transferenciaCabecera = new TransferenciaCabecera();
            transferenciaCabecera.setFechaTransferencia(LocalDateTime.now());
            transferenciaCabecera.setUsuario(usuarioActual);
            transferenciaCabecera.setMotivo(motivoTexto);

            TransferenciaDetalle detalle = new TransferenciaDetalle();
            detalle.setTransferenciaCabecera(transferenciaCabecera);
            detalle.setArticulo(lote.getArticulo());
            detalle.setUbicacionOrigen(origen);
            detalle.setUbicacionDestino(destino);
            detalle.setCantidadMovida(solicitud.cantidad());
            transferenciaCabecera.getDetalles().add(detalle);
            transferenciaCabeceraRepository.save(transferenciaCabecera);

            registrarEnKardex(lote.getArticulo(), "TRANSFERENCIA", "TRF-" + lote.getCodigoLote(),
                    solicitud.cantidad(), lote.getArticulo().getStockActual(), usuarioActual);
        }

        return MovimientoResponseDTO.desde(movimiento);
    }

    @Transactional(readOnly = true)
    public List<LoteResponseDTO> listarTodosLosLotes() {
        return loteRepository.findAllByOrderByFechaVencimientoAsc()
                .stream()
                .map(LoteResponseDTO::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LoteResponseDTO> listarLotesProximosAVencer() {
        LocalDate hoy = LocalDate.now();
        return loteRepository.buscarProximosAVencer(hoy, hoy.plusDays(DIAS_PROXIMO_VENCIMIENTO))
                .stream()
                .map(LoteResponseDTO::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LoteResponseDTO> listarLotesPorArticulo(Integer articuloId) {
        if (!articuloRepository.existsById(articuloId)) {
            throw new RecursoNoEncontradoException("Articulo no encontrado");
        }
        return loteRepository.findByArticulo_IdArticuloOrderByFechaVencimientoAsc(articuloId)
                .stream()
                .map(LoteResponseDTO::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponseDTO> listarMovimientos() {
        return movimientoRepository.findAllByOrderByFechaMovimientoDesc()
                .stream()
                .map(MovimientoResponseDTO::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponseDTO> listarMovimientosPorTipo(TipoMovimiento tipo) {
        return movimientoRepository.findByTipoMovimientoOrderByFechaMovimientoDesc(tipo)
                .stream()
                .map(MovimientoResponseDTO::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IngresoCabecera> listarDocumentosIngreso() {
        return ingresoCabeceraRepository.findAllByOrderByFechaRecepcionDesc();
    }

    @Transactional(readOnly = true)
    public List<SalidaProduccionCabecera> listarDocumentosSalida() {
        return salidaProduccionCabeceraRepository.findAllByOrderByFechaSalidaDesc();
    }

    @Transactional(readOnly = true)
    public List<TransferenciaCabecera> listarDocumentosTransferencia() {
        return transferenciaCabeceraRepository.findAllByOrderByFechaTransferenciaDesc();
    }

    private MovimientoInventario crearMovimiento(TipoMovimiento tipo, Articulo articulo,
                                                  Lote lote, BigDecimal cantidad,
                                                  Ubicacion origen, Ubicacion destino) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipoMovimiento(tipo);
        movimiento.setArticulo(articulo);
        movimiento.setLote(lote);
        movimiento.setCantidad(cantidad);
        movimiento.setUbicacionOrigen(origen);
        movimiento.setUbicacionDestino(destino);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        return movimiento;
    }

    private void actualizarStockActual(Articulo articulo, BigDecimal variacion) {
        BigDecimal stockActual = articulo.getStockActual() == null
                ? BigDecimal.ZERO
                : articulo.getStockActual();
        articulo.setStockActual(stockActual.add(variacion));
        articuloRepository.save(articulo);
    }

    private void registrarEnKardex(Articulo articulo, String tipoMovimiento, String referencia,
                                   BigDecimal cantidad, BigDecimal saldoResultante, Usuario usuario) {
        Kardex kardex = new Kardex();
        kardex.setArticulo(articulo);
        kardex.setFechaMovimiento(LocalDateTime.now());
        kardex.setTipoMovimiento(tipoMovimiento);
        kardex.setReferenciaDocumento(referencia);
        kardex.setCantidad(cantidad);
        kardex.setSaldoResultante(saldoResultante);
        kardex.setUsuario(usuario);
        kardexRepository.save(kardex);
    }

    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            if (auth.getPrincipal() instanceof UsuarioAutenticado ua) {
                return repositorioUsuario.findById(ua.getIdUsuario()).orElse(null);
            }
            if (auth.getName() != null) {
                var porUsername = repositorioUsuario.findByUsername(auth.getName());
                if (porUsername.isPresent()) {
                    return porUsername.get();
                }
            }
        }
        return repositorioUsuario.findByUsername("admin")
                .or(() -> repositorioUsuario.findAll().stream().findFirst())
                .orElse(null);
    }
}

