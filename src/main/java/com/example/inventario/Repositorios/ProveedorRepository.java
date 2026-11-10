package com.example.inventario.Repositorios;

import com.example.inventario.Entidades.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {
    // Para buscar un proveedor por su RUC y evitar duplicados
    Optional<Proveedor> findByRuc(String ruc);
    boolean existsByRuc(String ruc);
}