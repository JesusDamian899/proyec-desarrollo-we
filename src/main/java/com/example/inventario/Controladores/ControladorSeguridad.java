package com.example.inventario.Controladores;

import com.example.inventario.Servicios.*;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ControladorSeguridad {

    private final ServicioPermiso servicioPermiso;
    private final ServicioRol servicioRol;
    private final ServicioUsuario servicioUsuario;

    public ControladorSeguridad(ServicioPermiso servicioPermiso,
                                ServicioRol servicioRol,
                                ServicioUsuario servicioUsuario) {
        this.servicioPermiso = servicioPermiso;
        this.servicioRol = servicioRol;
        this.servicioUsuario = servicioUsuario;
    }

    @GetMapping("/permisos")
    public List<PermisoResponse> listarPermisos() {
        return servicioPermiso.listar();
    }

    @GetMapping("/permisos/{id}")
    public PermisoResponse buscarPermiso(@PathVariable Long id) {
        return servicioPermiso.buscarPorId(id);
    }

    @GetMapping("/roles")
    public List<RolResponse> listarRoles() {
        return servicioRol.listar();
    }

    @GetMapping("/roles/{id}")
    public RolResponse buscarRol(@PathVariable Long id) {
        return servicioRol.buscarPorId(id);
    }

    @GetMapping("/usuarios")
    public List<UsuarioResponse> listarUsuarios() {
        return servicioUsuario.listar();
    }

    @GetMapping("/usuarios/{id}")
    public UsuarioResponse buscarUsuario(@PathVariable Long id) {
        return servicioUsuario.buscarPorId(id);
    }

    @PostMapping("/usuarios")
@ResponseStatus(HttpStatus.CREATED)
public UsuarioResponse guardarUsuario(@Valid @RequestBody UsuarioRequest peticion) {
    return servicioUsuario.guardar(peticion);
    }
}