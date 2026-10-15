package com.example.inventario.Servicios;


import com.example.inventario.Entidades.UsuarioAutenticado;
import com.example.inventario.Repositorios.RepositorioUsuario;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ImplServicioAuth implements ServicioAuth {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RepositorioUsuario repositorioUsuario;

    public ImplServicioAuth(AuthenticationManager authenticationManager,
                            JwtService jwtService,
                            RepositorioUsuario repositorioUsuario) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.repositorioUsuario = repositorioUsuario;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest peticion) {

        Authentication autenticacion = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        peticion.username().trim().toLowerCase(),
                        peticion.password()));

        UsuarioAutenticado usuario = (UsuarioAutenticado) autenticacion.getPrincipal();

        repositorioUsuario.findById(usuario.getIdUsuario())
                .ifPresent(u -> u.setUltimoAcceso(LocalDateTime.now()));

        List<String> authorities = usuario.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();

        List<String> roles = authorities.stream()
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .toList();

        List<String> permisos = authorities.stream()
                .filter(a -> !a.startsWith("ROLE_"))
                .toList();

        return new LoginResponse(
                jwtService.generar(usuario),
                "Bearer",
                usuario.getIdUsuario(),
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                roles,
                permisos,
                jwtService.getExpiracionMs());
    }
}