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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.inventario.Entidades.Articulo;
import com.example.inventario.Servicios.ArticuloService;

@RestController
@RequestMapping("/api/articulos")
@CrossOrigin(origins = "*")
public class ArticuloController {
    @Autowired
    private ArticuloService articuloService;

    //lista todo
    @GetMapping
    public ResponseEntity<List<Articulo>> listar() {
        return ResponseEntity.ok(articuloService.listarTodos());
    }

    //busca por ID
    @GetMapping("/{id}")
    public ResponseEntity<Articulo> obtenerPorId(@PathVariable Integer id) {
        return articuloService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //BUSCA POR CODIGO INTERNO
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<Articulo> obtenerPorCodigo(@PathVariable String codigo) {
        return articuloService.obtenerPorCodigoInterno(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //BUSCA POR DESCRIPCION
    @GetMapping("/buscar")
    public ResponseEntity<List<Articulo>> buscarPorDescripcion(@RequestParam("texto") String texto) {
        return ResponseEntity.ok(articuloService.buscarPorDescripcion(texto));
    } 

    //alerta de bajo stock
    @GetMapping("/bajo-stock")
    public ResponseEntity<List<Articulo>> listarBajoStock() {
        return ResponseEntity.ok(articuloService.listarArticulosBajoStock());
    }

    // crear nuevo articulo
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Articulo articulo) {
        try {
            Articulo nuevo = articuloService.guardar(articulo);
            return new ResponseEntity<>(nuevo, HttpStatus.CREATED); // 201 CREATED
        } catch (IllegalArgumentException e) {
            // Si el código interno ya existe, devuelve 400 BAD REQUEST con el mensaje del Service
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //actualizar articulo
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody Articulo articulo) {
        try {
            Articulo actualizado = articuloService.actualizar(id, articulo);
            return ResponseEntity.ok(actualizado); // 200 OK
        } catch (IllegalArgumentException e) {
            // Si intenta ponerle un código interno que ya usa otro artículo -> 400 BAD REQUEST
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            // Si el ID del artículo no existe -> 404 NOT FOUND
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    //eliminar articulo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        try {
            articuloService.eliminar(id);
            return ResponseEntity.noContent().build(); // 204 NO CONTENT
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}

