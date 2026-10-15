package com.example.inventario.Repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;  
import com.example.inventario.Entidades.Rol;

public interface RepositorioRol extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
    
}
