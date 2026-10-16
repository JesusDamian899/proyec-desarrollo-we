package com.example.inventario.Controladores;

import com.example.inventario.Entidades.Kardex;
import com.example.inventario.Servicios.KardexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/kardex")
@CrossOrigin(origins = "*")
public class KardexController {

    @Autowired
    private KardexService kardexService;

   
    @GetMapping
    public ResponseEntity<List<Kardex>> listarTodo() {
        return ResponseEntity.ok(kardexService.listarTodo());
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<Kardex> obtenerPorId(@PathVariable Integer id) {
        return kardexService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

   
    @GetMapping("/articulo/{idArticulo}")
    public ResponseEntity<List<Kardex>> listarPorArticulo(@PathVariable Integer idArticulo) {
        return ResponseEntity.ok(kardexService.listarPorArticulo(idArticulo));
    }


    @GetMapping("/tipo")
    public ResponseEntity<List<Kardex>> listarPorTipo(@RequestParam("movimiento") String movimiento) {
        return ResponseEntity.ok(kardexService.listarPorTipoMovimiento(movimiento));
    }


    @GetMapping("/fechas")
    public ResponseEntity<List<Kardex>> listarPorFechas(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam("fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(kardexService.listarPorFechas(inicio, fin));
    }

    @PostMapping
    public ResponseEntity<Kardex> registrar(@RequestBody Kardex kardex) {
        Kardex nuevo = kardexService.registrarMovimiento(kardex);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }
}