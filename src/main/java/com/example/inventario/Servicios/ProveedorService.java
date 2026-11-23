package com.example.inventario.Servicios;

import com.example.inventario.Entidades.Proveedor;
import com.example.inventario.Excepciones.RecursoNoEncontradoException;
import com.example.inventario.Excepciones.ReglaNegocioException;
import com.example.inventario.Repositorios.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class ProveedorService {

    private static final Pattern RUC_PATTERN = Pattern.compile("^\\d{11}$");

    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    @Transactional(readOnly = true)
    public List<Proveedor> listarTodos() {
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
        String ruc = validarRuc(proveedor.getRuc());
        if (proveedor.getRazonSocial() == null || proveedor.getRazonSocial().isBlank()) {
            throw new IllegalArgumentException("La razon social del proveedor es obligatoria.");
        }
        if (proveedorRepository.existsByRuc(ruc)) {
            throw new ReglaNegocioException("Ya existe un proveedor registrado con el RUC: " + ruc);
        }
        proveedor.setRuc(ruc);
        proveedor.setRazonSocial(proveedor.getRazonSocial().trim());
        if (proveedor.getContacto() != null) {
            proveedor.setContacto(proveedor.getContacto().trim());
        }
        return proveedorRepository.save(proveedor);
    }

    @Transactional
    public Proveedor actualizar(Integer id, Proveedor datosActualizados) {
        return proveedorRepository.findById(id).map(proveedorExistente -> {
            if (datosActualizados.getRuc() != null && !datosActualizados.getRuc().isBlank()) {
                String nuevoRuc = validarRuc(datosActualizados.getRuc());
                if (!proveedorExistente.getRuc().equals(nuevoRuc) && proveedorRepository.existsByRuc(nuevoRuc)) {
                    throw new ReglaNegocioException("El RUC " + nuevoRuc + " ya pertenece a otro proveedor.");
                }
                proveedorExistente.setRuc(nuevoRuc);
            }
            if (datosActualizados.getRazonSocial() != null && !datosActualizados.getRazonSocial().isBlank()) {
                proveedorExistente.setRazonSocial(datosActualizados.getRazonSocial().trim());
            }
            if (datosActualizados.getContacto() != null) {
                proveedorExistente.setContacto(datosActualizados.getContacto().trim());
            }
            return proveedorRepository.save(proveedorExistente);
        }).orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado con ID: " + id));
    }

    @Transactional
    public void eliminar(Integer id) {
        if (!proveedorRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Proveedor no encontrado con ID: " + id);
        }
        proveedorRepository.deleteById(id);
    }

    private String validarRuc(String rucRaw) {
        if (rucRaw == null || rucRaw.isBlank()) {
            throw new IllegalArgumentException("El RUC del proveedor es obligatorio.");
        }
        String ruc = rucRaw.trim();
        if (!RUC_PATTERN.matcher(ruc).matches()) {
            throw new IllegalArgumentException("El RUC del proveedor debe contener exactamente 11 digitos numericos.");
        }
        return ruc;
    }
}
