package com.example.inventario.Controladores;

import com.example.inventario.Entidades.Categoria;
import com.example.inventario.Entidades.Permiso;
import com.example.inventario.Entidades.Proveedor;
import com.example.inventario.Entidades.Rol;
import com.example.inventario.Entidades.TipoPermiso;
import com.example.inventario.Entidades.Ubicacion;
import com.example.inventario.Entidades.UnidadMedida;
import com.example.inventario.Entidades.Usuario;
import com.example.inventario.Repositorios.CategoriaRepository;
import com.example.inventario.Repositorios.ProveedorRepository;
import com.example.inventario.Repositorios.RepositorioPermiso;
import com.example.inventario.Repositorios.RepositorioRol;
import com.example.inventario.Repositorios.RepositorioUsuario;
import com.example.inventario.Repositorios.UbicacionRepository;
import com.example.inventario.Repositorios.UnidadMedidaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Configuration
public class CargaInicial {

    @Bean
    CommandLineRunner inicializar(RepositorioPermiso repoPermiso,
                                  RepositorioRol repoRol,
                                  RepositorioUsuario repoUsuario,
                                  CategoriaRepository repoCategoria,
                                  UnidadMedidaRepository repoUnidadMedida,
                                  UbicacionRepository repoUbicacion,
                                  ProveedorRepository repoProveedor) {
        return args -> {

            if (repoPermiso.count() == 0) {
                // ---------- PERMISOS ----------
                crearPermiso(repoPermiso, "ARTICULO_VER", "Consultar stock", "Inventario", "/api/articulos", TipoPermiso.VER, true, 11);
                crearPermiso(repoPermiso, "ARTICULO_GESTIONAR", "Crear y editar articulos", "Inventario", null, TipoPermiso.EDITAR, false, 12);
                crearPermiso(repoPermiso, "STOCK_AJUSTAR", "Ajuste de inventario", "Inventario", "/api/ajustes", TipoPermiso.CREAR, true, 13);

                crearPermiso(repoPermiso, "INGRESO_VER", "Ver recepciones", "Recepciones", "/api/ingresos", TipoPermiso.VER, true, 21);
                crearPermiso(repoPermiso, "INGRESO_CREAR", "Registrar recepcion", "Recepciones", null, TipoPermiso.CREAR, false, 22);

                crearPermiso(repoPermiso, "SOLICITUD_CREAR", "Solicitar materiales", "Despachos", "/api/solicitudes", TipoPermiso.CREAR, true, 31);
                crearPermiso(repoPermiso, "SOLICITUD_VER_PROPIAS", "Mis solicitudes", "Despachos", "/api/solicitudes/mias", TipoPermiso.VER, true, 32);
                crearPermiso(repoPermiso, "SALIDA_VER", "Ver despachos", "Despachos", "/api/salidas", TipoPermiso.VER, true, 33);
                crearPermiso(repoPermiso, "SALIDA_APROBAR", "Aprobar salida", "Despachos", null, TipoPermiso.APROBAR, false, 34);
                crearPermiso(repoPermiso, "SALIDA_ATENDER", "Despachar materiales", "Despachos", null, TipoPermiso.CREAR, false, 35);

                crearPermiso(repoPermiso, "TRANSFER_VER", "Ver transferencias", "Transferencias", "/api/transferencias", TipoPermiso.VER, true, 41);
                crearPermiso(repoPermiso, "TRANSFER_CREAR", "Registrar transferencia", "Transferencias", null, TipoPermiso.CREAR, false, 42);

                crearPermiso(repoPermiso, "KARDEX_VER", "Consultar Kardex", "Kardex y reportes", "/api/kardex", TipoPermiso.VER, true, 51);
                crearPermiso(repoPermiso, "REPORTE_STOCK_MINIMO", "Articulos bajo minimo", "Kardex y reportes", "/api/reportes/minimos", TipoPermiso.VER, true, 52);
                crearPermiso(repoPermiso, "REPORTE_VALORIZADO", "Inventario valorizado", "Kardex y reportes", "/api/reportes/valorizado", TipoPermiso.VER, true, 53);
                crearPermiso(repoPermiso, "REPORTE_VENCIMIENTOS", "Lotes por vencer", "Kardex y reportes", "/api/reportes/vencimientos", TipoPermiso.VER, true, 54);

                crearPermiso(repoPermiso, "MAESTRO_GESTIONAR", "Categorias, unidades y ubicaciones", "Maestros", "/api/maestros", TipoPermiso.VER, true, 61);
                crearPermiso(repoPermiso, "PROVEEDOR_GESTIONAR", "Proveedores", "Maestros", "/api/proveedores", TipoPermiso.VER, true, 62);

                crearPermiso(repoPermiso, "USUARIO_GESTIONAR", "Usuarios", "Seguridad", "/api/usuarios", TipoPermiso.VER, true, 71);
                crearPermiso(repoPermiso, "ROL_GESTIONAR", "Roles y permisos", "Seguridad", "/api/roles", TipoPermiso.VER, true, 72);

                // Mapa para buscar permisos por codigo
                Map<String, Permiso> p = new HashMap<>();
                repoPermiso.findAll().forEach(x -> p.put(x.getCodigo(), x));

                // ---------- ROLES ----------
                crearRol(repoRol, "ADMINISTRADOR", "Acceso total y gestion de usuarios",
                        new HashSet<>(p.values()));

                crearRol(repoRol, "JEFE_ALMACEN", "Supervisa, aprueba salidas y ajusta inventario",
                        seleccionar(p, "ARTICULO_VER", "ARTICULO_GESTIONAR", "STOCK_AJUSTAR",
                                "INGRESO_VER", "INGRESO_CREAR", "SALIDA_VER", "SALIDA_APROBAR",
                                "SALIDA_ATENDER", "TRANSFER_VER", "TRANSFER_CREAR", "KARDEX_VER",
                                "REPORTE_STOCK_MINIMO", "REPORTE_VALORIZADO", "REPORTE_VENCIMIENTOS",
                                "MAESTRO_GESTIONAR", "PROVEEDOR_GESTIONAR"));

                crearRol(repoRol, "ALMACENERO", "Ejecuta movimientos fisicos de almacen",
                        seleccionar(p, "ARTICULO_VER", "INGRESO_VER", "INGRESO_CREAR",
                                "SALIDA_VER", "SALIDA_ATENDER", "TRANSFER_VER", "TRANSFER_CREAR",
                                "KARDEX_VER", "REPORTE_STOCK_MINIMO", "REPORTE_VENCIMIENTOS"));

                crearRol(repoRol, "SOLICITANTE", "Solicita materiales para produccion",
                        seleccionar(p, "ARTICULO_VER", "SOLICITUD_CREAR", "SOLICITUD_VER_PROPIAS"));

                crearRol(repoRol, "COSTOS", "Consulta Kardex y reportes valorizados",
                        seleccionar(p, "ARTICULO_VER", "INGRESO_VER", "KARDEX_VER",
                                "REPORTE_VALORIZADO", "REPORTE_STOCK_MINIMO"));

                // ---------- USUARIO ADMINISTRADOR ----------
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPasswordHash(encoder.encode("Admin2026*"));
                admin.setNombreCompleto("Administrador del sistema");
                admin.setEmail("admin@electromecanicaandina.com");
                admin.getRoles().add(repoRol.findByNombre("ADMINISTRADOR").orElseThrow());
                repoUsuario.save(admin);

                System.out.println(">>> CARGA INICIAL DE SEGURIDAD COMPLETADA");
            } else {
                System.out.println(">>> La carga inicial de seguridad ya se ejecuto antes. Se omite.");
            }

            // ---------- CATALOGOS MAESTROS INICIALES ----------
            if (repoCategoria.count() == 0) {
                crearCategoria(repoCategoria, "Materiales Electricos");
                crearCategoria(repoCategoria, "Componentes Mecanicos");
                crearCategoria(repoCategoria, "Herramientas e Insumos");
                crearCategoria(repoCategoria, "Equipos de Proteccion (EPP)");
            }

            if (repoUnidadMedida.count() == 0) {
                crearUnidad(repoUnidadMedida, "Unidad", "UND");
                crearUnidad(repoUnidadMedida, "Metro", "M");
                crearUnidad(repoUnidadMedida, "Kilogramo", "KG");
                crearUnidad(repoUnidadMedida, "Litro", "LT");
                crearUnidad(repoUnidadMedida, "Rollo", "RLL");
            }

            if (repoUbicacion.count() == 0) {
                crearUbicacion(repoUbicacion, "Almacen Principal - Estante A1", "ALM-A1", "ESTANTERIA");
                crearUbicacion(repoUbicacion, "Almacen Principal - Estante B1", "ALM-B1", "ESTANTERIA");
                crearUbicacion(repoUbicacion, "Zona de Recepcion", "REC-01", "RECEPCION");
                crearUbicacion(repoUbicacion, "Planta de Produccion", "PROD-01", "PRODUCCION");
            }

            if (repoProveedor.count() == 0) {
                crearProveedor(repoProveedor, "Suministros Industriales Andinos SAC", "20512345678", "ventas@suministrosandinos.pe");
                crearProveedor(repoProveedor, "Importadora Electromecanica Peru SAA", "20609876543", "contacto@imep.com.pe");
            }
        };
    }

    private void crearPermiso(RepositorioPermiso repo, String codigo, String nombre,
                              String modulo, String ruta, TipoPermiso tipo,
                              boolean menu, int orden) {
        Permiso permiso = new Permiso();
        permiso.setCodigo(codigo);
        permiso.setNombre(nombre);
        permiso.setModulo(modulo);
        permiso.setRuta(ruta);
        permiso.setTipo(tipo);
        permiso.setMostrarEnMenu(menu);
        permiso.setOrden(orden);
        repo.save(permiso);
    }

    private void crearRol(RepositorioRol repo, String nombre, String descripcion,
                          Set<Permiso> permisos) {
        Rol rol = new Rol();
        rol.setNombre(nombre);
        rol.setDescripcion(descripcion);
        rol.setPermisos(permisos);
        repo.save(rol);
    }

    private Set<Permiso> seleccionar(Map<String, Permiso> mapa, String... codigos) {
        Set<Permiso> set = new HashSet<>();
        for (String codigo : codigos) {
            set.add(mapa.get(codigo));
        }
        return set;
    }

    private void crearCategoria(CategoriaRepository repo, String nombre) {
        Categoria c = new Categoria();
        c.setNombre(nombre);
        repo.save(c);
    }

    private void crearUnidad(UnidadMedidaRepository repo, String nombre, String abreviatura) {
        UnidadMedida u = new UnidadMedida();
        u.setNombre(nombre);
        u.setAbreviatura(abreviatura);
        repo.save(u);
    }

    private void crearUbicacion(UbicacionRepository repo, String nombre, String codigo, String tipo) {
        Ubicacion ub = new Ubicacion();
        ub.setNombre(nombre);
        ub.setCodigo(codigo);
        ub.setTipo(tipo);
        repo.save(ub);
    }

    private void crearProveedor(ProveedorRepository repo, String razonSocial, String ruc, String contacto) {
        Proveedor p = new Proveedor();
        p.setRazonSocial(razonSocial);
        p.setRuc(ruc);
        p.setContacto(contacto);
        repo.save(p);
    }
}