package com.example.inventario.Servicios;

import com.example.inventario.Entidades.Articulo;
import com.example.inventario.Entidades.Kardex;
import com.example.inventario.Entidades.Usuario;
import com.example.inventario.Excepciones.RecursoNoEncontradoException;
import com.example.inventario.Repositorios.ArticuloRepository;
import com.example.inventario.Repositorios.KardexRepository;
import com.example.inventario.Repositorios.RepositorioUsuario;
import com.example.inventario.Seguridad.UsuarioActualHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class KardexService {

    private final KardexRepository kardexRepository;
    private final ArticuloRepository articuloRepository;
    private final RepositorioUsuario repositorioUsuario;
    private final UsuarioActualHelper usuarioActualHelper;

    public KardexService(KardexRepository kardexRepository,
                         ArticuloRepository articuloRepository,
                         RepositorioUsuario repositorioUsuario,
                         UsuarioActualHelper usuarioActualHelper) {
        this.kardexRepository = kardexRepository;
        this.articuloRepository = articuloRepository;
        this.repositorioUsuario = repositorioUsuario;
        this.usuarioActualHelper = usuarioActualHelper;
    }

    @Transactional(readOnly = true)
    public List<Kardex> listarTodo() {
        return kardexRepository.findAllByOrderByFechaMovimientoDesc();
    }

    @Transactional(readOnly = true)
    public Optional<Kardex> obtenerPorId(Integer id) {
        return kardexRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Kardex> listarPorArticulo(Integer idArticulo) {
        return kardexRepository.findByArticulo_IdArticuloOrderByFechaMovimientoDesc(idArticulo);
    }

    @Transactional(readOnly = true)
    public List<Kardex> listarPorTipoMovimiento(String tipo) {
        return kardexRepository.findByTipoMovimientoIgnoreCaseOrderByFechaMovimientoDesc(tipo);
    }

    @Transactional(readOnly = true)
    public List<Kardex> listarPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return kardexRepository.findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(inicio, fin);
    }

    @Transactional
    public Kardex registrarMovimiento(Kardex kardex) {
        if (kardex.getFechaMovimiento() == null) {
            kardex.setFechaMovimiento(LocalDateTime.now());
        }
        if (kardex.getArticulo() != null && kardex.getArticulo().getIdArticulo() != null) {
            Articulo articulo = articuloRepository.findById(kardex.getArticulo().getIdArticulo())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Articulo no encontrado con ID: " + kardex.getArticulo().getIdArticulo()));
            kardex.setArticulo(articulo);
            if (kardex.getSaldoResultante() == null) {
                kardex.setSaldoResultante(articulo.getStockActual() != null ? articulo.getStockActual() : BigDecimal.ZERO);
            }
        }
        if (kardex.getSaldoResultante() == null) {
            kardex.setSaldoResultante(kardex.getCantidad() != null ? kardex.getCantidad() : BigDecimal.ZERO);
        }
        if (kardex.getReferenciaDocumento() == null || kardex.getReferenciaDocumento().isBlank()) {
            kardex.setReferenciaDocumento("MOV-MANUAL");
        }
        if (kardex.getUsuario() != null && kardex.getUsuario().getIdUsuario() != null) {
            Usuario u = repositorioUsuario.findById(kardex.getUsuario().getIdUsuario())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Usuario no encontrado con ID: " + kardex.getUsuario().getIdUsuario()));
            kardex.setUsuario(u);
        } else {
            kardex.setUsuario(usuarioActualHelper.obtenerUsuarioActual());
        }
        return kardexRepository.save(kardex);
    }
}