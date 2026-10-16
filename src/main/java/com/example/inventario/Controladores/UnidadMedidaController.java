package com.example.inventario.Controladores;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.inventario.Entidades.UnidadMedida;
import com.example.inventario.Servicios.UnidadMedidaService;

@RestController
@RequestMapping("/api/unidades-medida")
@CrossOrigin(origins = "*")
public class UnidadMedidaController {

    @Autowired
    private UnidadMedidaService unidadMedidaService;

    
    @GetMapping
    public ResponseEntity<List<UnidadMedida>> listar() {
        return ResponseEntity.ok(unidadMedidaService.listarTodas());
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<UnidadMedida> obtenerPorId(@PathVariable Integer id) {
        return unidadMedidaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

 
    @PostMapping
    public ResponseEntity<UnidadMedida> crear(@RequestBody UnidadMedida unidadMedida) {
        UnidadMedida nueva = unidadMedidaService.guardar(unidadMedida);
        return new ResponseEntity<>(nueva, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody UnidadMedida unidadMedida) {
        try {
            UnidadMedida actualizada = unidadMedidaService.actualizar(id, unidadMedida);
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        try {
            unidadMedidaService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}