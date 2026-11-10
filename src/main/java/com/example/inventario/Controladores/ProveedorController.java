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

import com.example.inventario.Entidades.Proveedor;
import com.example.inventario.Servicios.ProveedorService;

@RestController
@RequestMapping("/api/proveedores")
@CrossOrigin(origins = "*")
public class ProveedorController {

    @Autowired 
    private ProveedorService proveedorService;

    @GetMapping
    public ResponseEntity<List<Proveedor>> listar(){
        return ResponseEntity.ok(proveedorService.listartodos());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Proveedor> ObtenerPorId(@PathVariable Integer id) { // obtener x ID LEER
        return proveedorService.obtenerPorId(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ruc/{ruc}")
    public ResponseEntity<Proveedor> obtenerPorRuc(@PathVariable String ruc) {
        return proveedorService.obtenerPorRuc(ruc).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Proveedor proveedor) { // crear
        try {
            Proveedor nuevo = proveedorService.guardar(proveedor);
            return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody Proveedor proveedor) {
        try {
            Proveedor actualizado = proveedorService.actualizar(id, proveedor);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        try {
            proveedorService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}

