package com.example.inventario.Controladores;

import com.example.inventario.DTOs.PermisoResponse;
import com.example.inventario.DTOs.RolResponse;
import com.example.inventario.DTOs.UsuarioRequest;
import com.example.inventario.DTOs.UsuarioResponse;
import com.example.inventario.Servicios.ServicioPermiso;
import com.example.inventario.Servicios.ServicioRol;
import com.example.inventario.Servicios.ServicioUsuario;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
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

    @PatchMapping("/usuarios/{id}/estado")
    public UsuarioResponse cambiarEstadoUsuario(@PathVariable Long id,
                                                @RequestParam("activo") boolean activo) {
        return servicioUsuario.cambiarEstado(id, activo);
    }

    @DeleteMapping("/usuarios/{id}")
    public UsuarioResponse desactivarUsuario(@PathVariable Long id) {
        return servicioUsuario.cambiarEstado(id, false);
    }
}