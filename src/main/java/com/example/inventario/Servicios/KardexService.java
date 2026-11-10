package com.example.inventario.Servicios;

import com.example.inventario.Entidades.Articulo;
import com.example.inventario.Entidades.Kardex;
import com.example.inventario.Entidades.Usuario;
import com.example.inventario.Entidades.UsuarioAutenticado;
import com.example.inventario.Repositorios.ArticuloRepository;
import com.example.inventario.Repositorios.KardexRepository;
import com.example.inventario.Repositorios.RepositorioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class KardexService {

    @Autowired
    private KardexRepository kardexRepository;

    @Autowired
    private ArticuloRepository articuloRepository;

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    // Listar todo el historial del Kardex
    @Transactional(readOnly = true)
    public List<Kardex> listarTodo() {
        return kardexRepository.findAllByOrderByFechaMovimientoDesc();
    }

    // Obtener un registro específico por ID
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

    // Filtrar historial
    @Transactional(readOnly = true)
    public List<Kardex> listarPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return kardexRepository.findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(inicio, fin);
    }

    // Registrar un nuevo movimiento en el Kardex
    @Transactional
    public Kardex registrarMovimiento(Kardex kardex) {
        if (kardex.getFechaMovimiento() == null) {
            kardex.setFechaMovimiento(LocalDateTime.now());
        }
        if (kardex.getArticulo() != null && kardex.getArticulo().getIdArticulo() != null) {
            Articulo articulo = articuloRepository.findById(kardex.getArticulo().getIdArticulo())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Artículo no encontrado con ID: " + kardex.getArticulo().getIdArticulo()));
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
            kardex.setUsuario(obtenerUsuarioActual());
        }
        return kardexRepository.save(kardex);
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