package com.example.inventario.Servicios;

import com.example.inventario.Entidades.Rol;
import com.example.inventario.Entidades.Usuario;
import com.example.inventario.Repositorios.RepositorioRol;
import com.example.inventario.Repositorios.RepositorioUsuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class ImplServicioUsuario implements ServicioUsuario {

    private final RepositorioUsuario repositorio;
    private final RepositorioRol repositorioRol;
    private final PasswordEncoder encoder;

    public ImplServicioUsuario(RepositorioUsuario repositorio,
                               RepositorioRol repositorioRol,
                               PasswordEncoder encoder) {
        this.repositorio = repositorio;
        this.repositorioRol = repositorioRol;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public UsuarioResponse guardar(UsuarioRequest peticion) {

        String username = peticion.username().trim().toLowerCase();

        if (repositorio.existsByUsername(username)) {
            throw new ReglaNegocioException("El usuario '" + username + "' ya existe");
        }

        String email = (peticion.email() != null && !peticion.email().isBlank())
                ? peticion.email().trim()
                : null;
        if (email != null && repositorio.existsByEmailIgnoreCase(email)) {
            throw new ReglaNegocioException("El correo '" + email + "' ya esta registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPasswordHash(encoder.encode(peticion.password()));
        usuario.setNombreCompleto(peticion.nombreCompleto().trim());
        usuario.setEmail(email);
        usuario.setRoles(resolverRoles(peticion.rolesIds()));

        return aDto(repositorio.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioResponse cambiarEstado(Long id, boolean activo) {
        Usuario usuario = repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario " + id + " no existe"));
        usuario.setActivo(activo);
        return aDto(repositorio.save(usuario));
    }

    private Set<Rol> resolverRoles(Set<Long> ids) {
        Set<Rol> roles = new HashSet<>();
        for (Long id : ids) {
            Rol rol = repositorioRol.findById(id)
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "El rol " + id + " no existe"));
            roles.add(rol);
        }
        return roles;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return repositorio.findAll().stream().map(this::aDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        Usuario usuario = repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario " + id + " no existe"));
        return aDto(usuario);
    }

    private UsuarioResponse aDto(Usuario u) {
        return new UsuarioResponse(
                u.getIdUsuario(),
                u.getUsername(),
                u.getNombreCompleto(),
                u.getEmail(),
                u.isActivo(),
                u.getFechaCreacion(),
                u.getRoles().stream()
                 .map(Rol::getNombre)
                 .collect(Collectors.toCollection(TreeSet::new)));
    }
}