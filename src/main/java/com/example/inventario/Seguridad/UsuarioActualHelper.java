package com.example.inventario.Seguridad;

import com.example.inventario.Entidades.Usuario;
import com.example.inventario.Repositorios.RepositorioUsuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UsuarioActualHelper {

    private final RepositorioUsuario repositorioUsuario;

    public UsuarioActualHelper(RepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
    }

    public Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            if (auth.getPrincipal() instanceof UsuarioAutenticado ua) {
                return repositorioUsuario.findById(ua.getIdUsuario()).orElse(null);
            }
            if (auth.getName() != null) {
                var porUsername = repositorioUsuario.findByUsername(auth.getName());
                if (porUsername.isPresent()) {
                    return porUsername.get();
                }
            }
        }
        return repositorioUsuario.findByUsername("admin")
                .or(() -> repositorioUsuario.findAll().stream().findFirst())
                .orElse(null);
    }
}
