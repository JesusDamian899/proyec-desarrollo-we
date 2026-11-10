package com.example.inventario.Repositorios;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.inventario.Entidades.IngresoCabecera;

public interface IngresoCabeceraRepository extends JpaRepository<IngresoCabecera, Integer> {

    @EntityGraph(attributePaths = {
        "detalles",
        "detalles.articulo",
        "detalles.articulo.categoria",
        "detalles.articulo.unidadMedida",
        "detalles.articulo.ubicacion",
        "usuario",
        "proveedor"
    })
    List<IngresoCabecera> findAllByOrderByFechaRecepcionDesc();
}