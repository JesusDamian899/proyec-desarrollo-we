package com.example.inventario.Servicios;

import com.example.inventario.DTOs.LoginRequest;
import com.example.inventario.DTOs.LoginResponse;

public interface ServicioAuth {
    LoginResponse login(LoginRequest peticion);
}