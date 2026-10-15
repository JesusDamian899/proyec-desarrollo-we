package com.example.inventario.Servicios;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UsuarioRequest(
    @NotBlank(message = "El usuario es obligatorio")
    @Size(min = 4, max = 50, message = "Entre 4 y 50 caracteres")
    String username,

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, message = "Minimo 8 caracteres")
    String password,

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 100)
    String nombreCompleto,

    @Email(message = "Formato de correo invalido")
    String email,

    @NotEmpty(message = "Debe asignar al menos un rol")
    Set<Long> rolesIds
) {}