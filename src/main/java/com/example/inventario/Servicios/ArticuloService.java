package com.example.inventario.Servicios;

import com.example.inventario.Entidades.Articulo;
import com.example.inventario.Entidades.Categoria;
import com.example.inventario.Entidades.EstadoLote;
import com.example.inventario.Entidades.Kardex;
import com.example.inventario.Entidades.Lote;
import com.example.inventario.Entidades.MovimientoInventario;
import com.example.inventario.Entidades.TipoMovimiento;
import com.example.inventario.Entidades.Ubicacion;
import com.example.inventario.Entidades.UnidadMedida;
import com.example.inventario.Entidades.Usuario;
import com.example.inventario.Excepciones.RecursoNoEncontradoException;
import com.example.inventario.Excepciones.ReglaNegocioException;
import com.example.inventario.Repositorios.ArticuloRepository;
import com.example.inventario.Repositorios.CategoriaRepository;
import com.example.inventario.Repositorios.KardexRepository;
import com.example.inventario.Repositorios.LoteRepository;
import com.example.inventario.Repositorios.MovimientoInventarioRepository;
import com.example.inventario.Repositorios.UbicacionRepository;
import com.example.inventario.Repositorios.UnidadMedidaRepository;
import com.example.inventario.Seguridad.UsuarioActualHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ArticuloService {

    private final ArticuloRepository articuloRepository;
    private final CategoriaRepository categoriaRepository;
    private final UnidadMedidaRepository unidadMedidaRepository;
    private final UbicacionRepository ubicacionRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final KardexRepository kardexRepository;
    private final UsuarioActualHelper usuarioActualHelper;

    public ArticuloService(ArticuloRepository articuloRepository,
                           CategoriaRepository categoriaRepository,
                           UnidadMedidaRepository unidadMedidaRepository,
                           UbicacionRepository ubicacionRepository,
                           LoteRepository loteRepository,
                           MovimientoInventarioRepository movimientoRepository,
                           KardexRepository kardexRepository,
                           UsuarioActualHelper usuarioActualHelper) {
        this.articuloRepository = articuloRepository;
        this.categoriaRepository = categoriaRepository;
        this.unidadMedidaRepository = unidadMedidaRepository;
        this.ubicacionRepository = ubicacionRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.kardexRepository = kardexRepository;
        this.usuarioActualHelper = usuarioActualHelper;
    }

    @Transactional(readOnly = true)
    public List<Articulo> listarTodos() {
        return articuloRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Articulo> obtenerPorId(Integer id) {
        return articuloRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Articulo> obtenerPorCodigoInterno(String codigoInterno) {
        return articuloRepository.findByCodigoInterno(codigoInterno);
    }

    @Transactional
    public Articulo guardar(Articulo articulo) {
        if (articulo.getCodigoInterno() == null || articulo.getCodigoInterno().isBlank()) {
            throw new IllegalArgumentException("El codigo interno del articulo es obligatorio.");
        }
        if (articulo.getDescripcion() == null || articulo.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripcion del articulo es obligatoria.");
        }
        articulo.setCodigoInterno(articulo.getCodigoInterno().trim());
        articulo.setDescripcion(articulo.getDescripcion().trim());

        if (articuloRepository.existsByCodigoInterno(articulo.getCodigoInterno())) {
            throw new ReglaNegocioException(
                    "Ya existe un articulo registrado con el codigo interno: " + articulo.getCodigoInterno());
        }

        resolverRelaciones(articulo);

        if (articulo.getStockActual() == null) {
            articulo.setStockActual(BigDecimal.ZERO);
        }
        if (articulo.getStockMinimo() == null) {
            articulo.setStockMinimo(BigDecimal.ZERO);
        }

        Articulo guardado = articuloRepository.save(articulo);

        if (guardado.getStockActual().compareTo(BigDecimal.ZERO) > 0 && guardado.getUbicacion() != null) {
            String codigoLoteInicial = "LOTE-INI-" + guardado.getCodigoInterno();
            Lote loteInicial = new Lote();
            loteInicial.setArticulo(guardado);
            loteInicial.setCodigoLote(codigoLoteInicial);
            loteInicial.setUbicacion(guardado.getUbicacion());
            loteInicial.setCantidad(guardado.getStockActual());
            loteInicial.setFechaIngreso(LocalDate.now());
            loteInicial.setFechaVencimiento(LocalDate.now().plusYears(1));
            loteInicial.setEstado(EstadoLote.DISPONIBLE);
            loteInicial = loteRepository.save(loteInicial);

            MovimientoInventario mov = new MovimientoInventario();
            mov.setTipoMovimiento(TipoMovimiento.INGRESO);
            mov.setArticulo(guardado);
            mov.setLote(loteInicial);
            mov.setCantidad(guardado.getStockActual());
            mov.setUbicacionDestino(guardado.getUbicacion());
            mov.setFechaMovimiento(LocalDateTime.now());
            movimientoRepository.save(mov);

            Usuario usuario = usuarioActualHelper.obtenerUsuarioActual();
            if (usuario != null) {
                Kardex kardex = new Kardex();
                kardex.setArticulo(guardado);
                kardex.setFechaMovimiento(LocalDateTime.now());
                kardex.setTipoMovimiento("INGRESO");
                kardex.setReferenciaDocumento("INV-INICIAL-" + guardado.getCodigoInterno());
                kardex.setCantidad(guardado.getStockActual());
                kardex.setSaldoResultante(guardado.getStockActual());
                kardex.setUsuario(usuario);
                kardexRepository.save(kardex);
            }
        }

        return guardado;
    }

    @Transactional
    public Articulo actualizar(Integer id, Articulo datosActualizados) {
        return articuloRepository.findById(id).map(articuloExistente -> {
            if (datosActualizados.getCodigoInterno() != null && !datosActualizados.getCodigoInterno().isBlank()) {
                String nuevoCodigo = datosActualizados.getCodigoInterno().trim();
                if (!articuloExistente.getCodigoInterno().equalsIgnoreCase(nuevoCodigo)
                        && articuloRepository.existsByCodigoInterno(nuevoCodigo)) {
                    throw new ReglaNegocioException("El codigo interno " + nuevoCodigo + " ya pertenece a otro articulo.");
                }
                articuloExistente.setCodigoInterno(nuevoCodigo);
            }
            if (datosActualizados.getDescripcion() != null && !datosActualizados.getDescripcion().isBlank()) {
                articuloExistente.setDescripcion(datosActualizados.getDescripcion().trim());
            }
            if (datosActualizados.getCategoria() != null) {
                articuloExistente.setCategoria(datosActualizados.getCategoria());
            }
            if (datosActualizados.getUnidadMedida() != null) {
                articuloExistente.setUnidadMedida(datosActualizados.getUnidadMedida());
            }
            if (datosActualizados.getUbicacion() != null) {
                articuloExistente.setUbicacion(datosActualizados.getUbicacion());
            }
            if (datosActualizados.getStockMinimo() != null) {
                articuloExistente.setStockMinimo(datosActualizados.getStockMinimo());
            }

            resolverRelaciones(articuloExistente);
            return articuloRepository.save(articuloExistente);
        }).orElseThrow(() -> new RecursoNoEncontradoException("Articulo no encontrado con ID: " + id));
    }

    @Transactional
    public void eliminar(Integer id) {
        if (!articuloRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Articulo no encontrado con ID: " + id);
        }
        articuloRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Articulo> buscarPorDescripcion(String texto) {
        return articuloRepository.findByDescripcionContainingIgnoreCase(texto);
    }

    @Transactional(readOnly = true)
    public List<Articulo> listarArticulosBajoStock() {
        return articuloRepository.findArticulosBajoStock();
    }

    private void resolverRelaciones(Articulo articulo) {
        if (articulo.getCategoria() != null && articulo.getCategoria().getIdCategoria() != null) {
            Categoria cat = categoriaRepository.findById(articulo.getCategoria().getIdCategoria())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Categoria no encontrada con ID: " + articulo.getCategoria().getIdCategoria()));
            articulo.setCategoria(cat);
        }
        if (articulo.getUnidadMedida() != null && articulo.getUnidadMedida().getIdUnidadMedida() != null) {
            UnidadMedida um = unidadMedidaRepository.findById(articulo.getUnidadMedida().getIdUnidadMedida())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unidad de medida no encontrada con ID: " + articulo.getUnidadMedida().getIdUnidadMedida()));
            articulo.setUnidadMedida(um);
        }
        if (articulo.getUbicacion() != null && articulo.getUbicacion().getId() != null) {
            Ubicacion ub = ubicacionRepository.findById(articulo.getUbicacion().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Ubicacion no encontrada con ID: " + articulo.getUbicacion().getId()));
            articulo.setUbicacion(ub);
        }
    }
}