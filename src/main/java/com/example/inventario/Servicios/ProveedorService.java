package com.example.inventario.Servicios;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.inventario.Entidades.Proveedor;
import com.example.inventario.Repositorios.ProveedorRepository;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Transactional(readOnly = true)
    public List<Proveedor> listartodos() {
        return proveedorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Proveedor> obtenerPorId(Integer id) {
        return proveedorRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Proveedor> obtenerPorRuc(String ruc) {
        return proveedorRepository.findByRuc(ruc);
    }

    @Transactional
    public Proveedor guardar(Proveedor proveedor) {
        if (proveedor.getRuc() != null && proveedorRepository.existsByRuc(proveedor.getRuc().trim())) {
            throw new IllegalArgumentException("Ya existe un proveedor registrado con el RUC: " + proveedor.getRuc());
        }
        if (proveedor.getRuc() != null) {
            proveedor.setRuc(proveedor.getRuc().trim());
        }
        return proveedorRepository.save(proveedor);
    }

    @Transactional
    public Proveedor actualizar(Integer id, Proveedor datosActualizados) {
        return proveedorRepository.findById(id).map(proveedorExistente -> {
            if (datosActualizados.getRuc() != null && !datosActualizados.getRuc().isBlank()) {
                String nuevoRuc = datosActualizados.getRuc().trim();
                if (!proveedorExistente.getRuc().equals(nuevoRuc) && proveedorRepository.existsByRuc(nuevoRuc)) {
                    throw new IllegalArgumentException("El RUC " + nuevoRuc + " ya pertenece a otro proveedor.");
                }
                proveedorExistente.setRuc(nuevoRuc);
            }
            if (datosActualizados.getRazonSocial() != null) {
                proveedorExistente.setRazonSocial(datosActualizados.getRazonSocial());
            }
            proveedorExistente.setContacto(datosActualizados.getContacto());
            return proveedorRepository.save(proveedorExistente);
        }).orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado con ID " + id));
    }

    @Transactional
    public void eliminar(Integer id) {
        if (!proveedorRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Proveedor no encontrado con ID " + id);
        }
        proveedorRepository.deleteById(id);
    }
}


