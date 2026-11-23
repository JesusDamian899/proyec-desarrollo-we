package com.example.inventario.Controladores;

import com.example.inventario.Entidades.UnidadMedida;
import com.example.inventario.Servicios.UnidadMedidaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unidades-medida")
@CrossOrigin(origins = "*")
public class UnidadMedidaController {

    private final UnidadMedidaService unidadMedidaService;

    public UnidadMedidaController(UnidadMedidaService unidadMedidaService) {
        this.unidadMedidaService = unidadMedidaService;
    }

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
        return new ResponseEntity<>(unidadMedidaService.guardar(unidadMedida), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnidadMedida> actualizar(@PathVariable Integer id, @RequestBody UnidadMedida unidadMedida) {
        return ResponseEntity.ok(unidadMedidaService.actualizar(id, unidadMedida));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        unidadMedidaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}