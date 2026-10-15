package com.example.inventario.Servicios;


import com.example.inventario.Entidades.Permiso;
import com.example.inventario.Entidades.Rol;
import com.example.inventario.Entidades.Usuario;
import com.example.inventario.Entidades.UsuarioAutenticado;
import com.example.inventario.Repositorios.RepositorioUsuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DetalleUsuarioService implements UserDetailsService {

    private final RepositorioUsuario repositorioUsuario;

    public DetalleUsuarioService(RepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {

        Usuario usuario = repositorioUsuario
                .buscarConRolesYPermisos(username.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();

        for (Rol rol : usuario.getRoles()) {
            if (!rol.isActivo()) continue;

            authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.getNombre()));

            for (Permiso permiso : rol.getPermisos()) {
                authorities.add(new SimpleGrantedAuthority(permiso.getCodigo()));
            }
        }

        return new UsuarioAutenticado(usuario, authorities);
    }
}
