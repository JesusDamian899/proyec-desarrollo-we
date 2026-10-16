package com.example.inventario.Servicios;

import com.example.inventario.Entidades.Kardex;
import com.example.inventario.Repositorios.KardexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class KardexService {

    @Autowired
    private KardexRepository kardexRepository;

    // Listar todo el historial del Kardex 
    public List<Kardex> listarTodo() {
        return kardexRepository.findAllByOrderByFechaMovimientoDesc();
    }

    // obtener un registro específico por ID
    public Optional<Kardex> obtenerPorId(Integer id) {
        return kardexRepository.findById(id);
    }

    // 
    public List<Kardex> listarPorArticulo(Integer idArticulo) {
        return kardexRepository.findByArticulo_IdArticuloOrderByFechaMovimientoDesc(idArticulo);
    }

    
    public List<Kardex> listarPorTipoMovimiento(String tipo) {
        return kardexRepository.findByTipoMovimientoIgnoreCaseOrderByFechaMovimientoDesc(tipo);
    }

    // Filtrar historial 
    public List<Kardex> listarPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return kardexRepository.findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(inicio, fin);
    }

    // registrar un nuevo movimiento en el Kardex
    public Kardex registrarMovimiento(Kardex kardex) {
        if (kardex.getFechaMovimiento() == null) {
            kardex.setFechaMovimiento(LocalDateTime.now());
        }
        return kardexRepository.save(kardex);
    }
}