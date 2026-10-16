package com.example.inventario.Repositorios;

import com.example.inventario.Entidades.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
