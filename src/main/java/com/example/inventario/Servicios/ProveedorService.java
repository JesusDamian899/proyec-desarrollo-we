package com.example.inventario.Servicios;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.inventario.Entidades.Proveedor;
import com.example.inventario.Repositorios.ProveedorRepository;
@Service 
public class ProveedorService {
    
    @Autowired 
    private ProveedorRepository proveedorRepository;

    public List<Proveedor> listartodos(){
        return proveedorRepository.findAll();
    }
    public Optional<Proveedor> obtenerPorId(Integer id) {
        return proveedorRepository.findById(id);
    }
    public Proveedor guardar (Proveedor proveedor) {
        return proveedorRepository.save(proveedor);
    }

    public Proveedor actualizar(Integer id, Proveedor datosActualizados) {
        return proveedorRepository.findById(id).map(proveedorExistente -> {
            proveedorExistente.setRazonSocial(datosActualizados.getRazonSocial());
            proveedorExistente.setRuc(datosActualizados.getRuc());
            proveedorExistente.setContacto(datosActualizados.getContacto());
            return proveedorRepository.save(proveedorExistente);
        }).orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID " + id));
    }
    public void eliminar(Integer id){
        proveedorRepository.deleteById(id);
    }
}

