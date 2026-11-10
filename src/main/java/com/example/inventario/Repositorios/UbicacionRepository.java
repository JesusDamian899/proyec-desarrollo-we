package com.example.inventario.Repositorios;

import com.example.inventario.Entidades.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {
    boolean existsByCodigoIgnoreCase(String codigo);
    Optional<Ubicacion> findByCodigoIgnoreCase(String codigo);
}

