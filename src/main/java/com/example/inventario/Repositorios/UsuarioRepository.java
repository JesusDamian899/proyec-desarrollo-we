package com.example.inventario.Repositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.inventario.Entidades.Usuario;


public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    // Fundamental para el Login (H.U.1): Buscar al usuario por su username
    Optional<Usuario> findByUsername(String username);
}