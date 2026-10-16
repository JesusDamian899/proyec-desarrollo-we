package com.example.inventario.Repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.inventario.Entidades.UnidadMedida;


public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida, Integer> {
}
