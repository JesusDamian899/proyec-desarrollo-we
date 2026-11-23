package com.example.inventario.Controladores;

import com.example.inventario.Entidades.Articulo;
import com.example.inventario.Servicios.ArticuloService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articulos")
@CrossOrigin(origins = "*")
public class ArticuloController {

    private final ArticuloService articuloService;

    public ArticuloController(ArticuloService articuloService) {
        this.articuloService = articuloService;
    }

    @GetMapping
    public ResponseEntity<List<Articulo>> listar() {
        return ResponseEntity.ok(articuloService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Articulo> obtenerPorId(@PathVariable Integer id) {
        return articuloService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<Articulo> obtenerPorCodigo(@PathVariable String codigo) {
        return articuloService.obtenerPorCodigoInterno(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Articulo>> buscarPorDescripcion(@RequestParam("texto") String texto) {
        return ResponseEntity.ok(articuloService.buscarPorDescripcion(texto));
    }

    @GetMapping("/bajo-stock")
    public ResponseEntity<List<Articulo>> listarBajoStock() {
        return ResponseEntity.ok(articuloService.listarArticulosBajoStock());
    }

    @PostMapping
    public ResponseEntity<Articulo> crear(@RequestBody Articulo articulo) {
        return new ResponseEntity<>(articuloService.guardar(articulo), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Articulo> actualizar(@PathVariable Integer id, @RequestBody Articulo articulo) {
        return ResponseEntity.ok(articuloService.actualizar(id, articulo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        articuloService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
