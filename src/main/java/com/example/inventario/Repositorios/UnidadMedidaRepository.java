package com.example.inventario.Repositorios;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.inventario.Entidades.UnidadMedida;


public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida, Integer> {
    boolean existsByAbreviaturaIgnoreCase(String abreviatura);
    Optional<UnidadMedida> findByAbreviaturaIgnoreCase(String abreviatura);
}

