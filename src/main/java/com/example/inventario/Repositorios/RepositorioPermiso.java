package com.example.inventario.Repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.inventario.Entidades.Permiso;
import java.util.*;

public interface RepositorioPermiso extends JpaRepository<Permiso, Long> {

    Optional<Permiso> findByCodigo(String codigo);

    List<Permiso> findAllByOrderByOrdenAsc();

}
