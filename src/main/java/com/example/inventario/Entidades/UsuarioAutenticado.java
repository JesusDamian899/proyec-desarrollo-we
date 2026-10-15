package com.example.inventario.Entidades;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class UsuarioAutenticado extends User {

    private final Long idUsuario;
    private final String nombreCompleto;

    public UsuarioAutenticado(Usuario usuario,
                              Collection<? extends GrantedAuthority> authorities) {
        super(usuario.getUsername(),
              usuario.getPasswordHash(),
              usuario.isActivo(),
              true,
              true,
              true,
              authorities);
        this.idUsuario = usuario.getIdUsuario();
        this.nombreCompleto = usuario.getNombreCompleto();
    }

    public Long getIdUsuario() { return idUsuario; }
    public String getNombreCompleto() { return nombreCompleto; }
}
