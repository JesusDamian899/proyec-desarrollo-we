package com.example.inventario.Repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;  
import com.example.inventario.Entidades.Usuario;

public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {

     Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("SELECT DISTINCT u FROM Usuario u "
         + "LEFT JOIN FETCH u.roles r "
         + "LEFT JOIN FETCH r.permisos "
         + "WHERE u.username = :username")
    Optional<Usuario> buscarConRolesYPermisos(@Param("username") String username);
    
}
