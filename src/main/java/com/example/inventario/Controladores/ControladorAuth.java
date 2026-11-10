package com.example.inventario.Controladores;

import com.example.inventario.Entidades.UsuarioAutenticado;
import com.example.inventario.Servicios.LoginRequest;
import com.example.inventario.Servicios.LoginResponse;
import com.example.inventario.Servicios.ServicioAuth;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class ControladorAuth {

    private final ServicioAuth servicioAuth;

    public ControladorAuth(ServicioAuth servicioAuth) {
        this.servicioAuth = servicioAuth;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest peticion) {
        return ResponseEntity.ok(servicioAuth.login(peticion));
    }

    @GetMapping("/perfil")
    public ResponseEntity<Map<String, Object>> perfil(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("idUsuario", usuario.getIdUsuario());
        cuerpo.put("username", usuario.getUsername());
        cuerpo.put("nombreCompleto", usuario.getNombreCompleto());
        cuerpo.put("authorities", usuario.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).sorted().toList());

        return ResponseEntity.ok(cuerpo);
    }
}