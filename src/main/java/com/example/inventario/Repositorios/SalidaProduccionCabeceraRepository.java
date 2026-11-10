package com.example.inventario.Repositorios;

import com.example.inventario.Entidades.SalidaProduccionCabecera;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalidaProduccionCabeceraRepository extends JpaRepository<SalidaProduccionCabecera, Integer> {

    @EntityGraph(attributePaths = {
        "detalles",
        "detalles.articulo",
        "detalles.articulo.categoria",
        "detalles.articulo.unidadMedida",
        "detalles.articulo.ubicacion",
        "detalles.loteIngreso",
        "detalles.loteIngreso.articulo",
        "detalles.loteIngreso.articulo.categoria",
        "detalles.loteIngreso.articulo.unidadMedida",
        "detalles.loteIngreso.articulo.ubicacion",
        "usuarioDespacha",
        "usuarioSolicitante"
    })
    List<SalidaProduccionCabecera> findAllByOrderByFechaSalidaDesc();
}


