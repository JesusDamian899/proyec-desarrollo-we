package com.example.inventario.Servicios;

import com.example.inventario.Servicios.RecursoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> argumentoInvalido(IllegalArgumentException e) {
        return armar(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException e) {
        String detalle = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Error de validacion en la peticion");
        return armar(HttpStatus.BAD_REQUEST, detalle);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> cuerpoInvalido(HttpMessageNotReadableException e) {
        return armar(HttpStatus.BAD_REQUEST, "Cuerpo de la peticion invalido o ausente");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> metodoNoSoportado(HttpRequestMethodNotSupportedException e) {
        return armar(HttpStatus.METHOD_NOT_ALLOWED, "Metodo HTTP " + e.getMethod() + " no permitido para esta ruta");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> general(Exception e) {
        return armar(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage() != null ? e.getMessage() : "Error interno del servidor");
    }
}

