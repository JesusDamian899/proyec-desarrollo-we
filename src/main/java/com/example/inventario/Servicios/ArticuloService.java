package com.example.inventario.Servicios;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.example.inventario.Entidades.UsuarioAutenticado;
import com.example.inventario.Repositorios.ArticuloRepository;
import com.example.inventario.Repositorios.CategoriaRepository;
import com.example.inventario.Repositorios.KardexRepository;
import com.example.inventario.Repositorios.LoteRepository;
import com.example.inventario.Repositorios.MovimientoInventarioRepository;
import com.example.inventario.Repositorios.RepositorioUsuario;
import com.example.inventario.Repositorios.UbicacionRepository;
import com.example.inventario.Repositorios.UnidadMedidaRepository;

@Service
public class ArticuloService {

    @Autowired
    private ArticuloRepository articuloRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UnidadMedidaRepository unidadMedidaRepository;

    @Autowired
    private UbicacionRepository ubicacionRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    @Autowired
    private KardexRepository kardexRepository;

    @Autowired
    private RepositorioUsuario repositorioUsuario;

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
            throw new IllegalArgumentException("El código interno del artículo es obligatorio.");
        }
        articulo.setCodigoInterno(articulo.getCodigoInterno().trim());

        // Validar que el código no exista previamente
        if (articuloRepository.existsByCodigoInterno(articulo.getCodigoInterno())) {
            throw new IllegalArgumentException("Ya existe un artículo registrado con el código interno: " + articulo.getCodigoInterno());
        }

        resolverRelaciones(articulo);

        if (articulo.getStockActual() == null) {
            articulo.setStockActual(BigDecimal.ZERO);
        }

        if (articulo.getStockMinimo() == null) {
            articulo.setStockMinimo(BigDecimal.ZERO);
        }

        Articulo guardado = articuloRepository.save(articulo);

        // Si se registra con stock inicial mayor a 0, crear su Lote inicial, Movimiento y Kardex para mantener consistencia FIFO
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

            Usuario usuario = obtenerUsuarioActual();
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

    // Actualizar datos del artículo
    @Transactional
    public Articulo actualizar(Integer id, Articulo datosActualizados) {
        return articuloRepository.findById(id).map(articuloExistente -> {

            if (datosActualizados.getCodigoInterno() != null && !datosActualizados.getCodigoInterno().isBlank()) {
                String nuevoCodigo = datosActualizados.getCodigoInterno().trim();
                if (!articuloExistente.getCodigoInterno().equalsIgnoreCase(nuevoCodigo) &&
                    articuloRepository.existsByCodigoInterno(nuevoCodigo)) {
                    throw new IllegalArgumentException("El código interno " + nuevoCodigo + " ya pertenece a otro artículo.");
                }
                articuloExistente.setCodigoInterno(nuevoCodigo);
            }

            if (datosActualizados.getDescripcion() != null) {
                articuloExistente.setDescripcion(datosActualizados.getDescripcion());
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
        }).orElseThrow(() -> new RecursoNoEncontradoException("Artículo no encontrado con ID: " + id));
    }

    // Eliminar un artículo por su ID
    @Transactional
    public void eliminar(Integer id) {
        if (!articuloRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Artículo no encontrado con ID: " + id);
        }
        articuloRepository.deleteById(id);
    }

    // Buscar artículos
    @Transactional(readOnly = true)
    public List<Articulo> buscarPorDescripcion(String texto) {
        return articuloRepository.findByDescripcionContainingIgnoreCase(texto);
    }

    // Obtener lista de artículos con alerta de bajo stock
    @Transactional(readOnly = true)
    public List<Articulo> listarArticulosBajoStock() {
        return articuloRepository.findArticulosBajoStock();
    }

    private void resolverRelaciones(Articulo articulo) {
        if (articulo.getCategoria() != null && articulo.getCategoria().getIdCategoria() != null) {
            Categoria cat = categoriaRepository.findById(articulo.getCategoria().getIdCategoria())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + articulo.getCategoria().getIdCategoria()));
            articulo.setCategoria(cat);
        }
        if (articulo.getUnidadMedida() != null && articulo.getUnidadMedida().getIdUnidadMedida() != null) {
            UnidadMedida um = unidadMedidaRepository.findById(articulo.getUnidadMedida().getIdUnidadMedida())
                    .orElseThrow(() -> new IllegalArgumentException("Unidad de medida no encontrada con ID: " + articulo.getUnidadMedida().getIdUnidadMedida()));
            articulo.setUnidadMedida(um);
        }
        if (articulo.getUbicacion() != null && articulo.getUbicacion().getId() != null) {
            Ubicacion ub = ubicacionRepository.findById(articulo.getUbicacion().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Ubicación no encontrada con ID: " + articulo.getUbicacion().getId()));
            articulo.setUbicacion(ub);
        }
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