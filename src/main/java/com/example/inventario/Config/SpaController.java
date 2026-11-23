package com.example.inventario.Config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping({"/login", "/resumen", "/articulos", "/catalogos", "/proveedores", "/seguridad", "/almacen"})
    public String forwardAngularRoutes() {
        return "forward:/index.html";
    }
}
