package com.example.inventario.Servicios;

import com.example.inventario.Servicios.RecursoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(RecursoNoEncontradoException e) {
        return armar(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> credenciales(BadCredentialsException e) {
        return armar(HttpStatus.UNAUTHORIZED, "Usuario o contrasena incorrectos");
    }

    private ResponseEntity<Map<String, Object>> armar(HttpStatus estado, String mensaje) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("fecha", LocalDateTime.now());
        cuerpo.put("estado", estado.value());
        cuerpo.put("mensaje", mensaje);
        return new ResponseEntity<>(cuerpo, estado);
    }

    @ExceptionHandler(ReglaNegocioException.class)
public ResponseEntity<Map<String, Object>> reglaNegocio(ReglaNegocioException e) {
    return armar(HttpStatus.CONFLICT, e.getMessage());
}
}