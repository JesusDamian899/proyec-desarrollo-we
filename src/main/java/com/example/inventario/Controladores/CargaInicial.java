package com.example.inventario.Controladores;

import com.example.inventario.DTOs.IngresoStockRequestDTO;
import com.example.inventario.Entidades.Articulo;
import com.example.inventario.Entidades.Categoria;
import com.example.inventario.Entidades.Permiso;
import com.example.inventario.Entidades.Proveedor;
import com.example.inventario.Entidades.Rol;
import com.example.inventario.Entidades.TipoPermiso;
import com.example.inventario.Entidades.Ubicacion;
import com.example.inventario.Entidades.UnidadMedida;
import com.example.inventario.Entidades.Usuario;
import com.example.inventario.Repositorios.ArticuloRepository;
import com.example.inventario.Repositorios.CategoriaRepository;
import com.example.inventario.Repositorios.ProveedorRepository;
import com.example.inventario.Repositorios.RepositorioPermiso;
import com.example.inventario.Repositorios.RepositorioRol;
import com.example.inventario.Repositorios.RepositorioUsuario;
import com.example.inventario.Repositorios.UbicacionRepository;
import com.example.inventario.Repositorios.UnidadMedidaRepository;
import com.example.inventario.Servicios.AlmacenService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
                                  ProveedorRepository repoProveedor,
                                  ArticuloRepository repoArticulo,
                                  AlmacenService almacenService) {
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

                System.out.println(">>> CARGA INICIAL DE PERMISOS Y ROLES COMPLETADA");
            } else {
                System.out.println(">>> Los permisos y roles ya existen en la BD.");
            }

            // Asegurar siempre que los 5 usuarios iniciales existan y esten activos con sus credenciales
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            asegurarUsuario(repoUsuario, repoRol, encoder, "admin", "Admin2026*",
                    "Administrador del Sistema", "admin@electromecanicaandina.com", "ADMINISTRADOR");
            asegurarUsuario(repoUsuario, repoRol, encoder, "jefe.almacen", "Jefe2026*",
                    "Carlos Mendoza (Jefe de Almacen)", "cmendoza@electromecanicaandina.com", "JEFE_ALMACEN");
            asegurarUsuario(repoUsuario, repoRol, encoder, "almacenero", "Almacen2026*",
                    "Luis Paredes (Operador Almacen)", "lparedes@electromecanicaandina.com", "ALMACENERO");
            asegurarUsuario(repoUsuario, repoRol, encoder, "solicitante", "Solicita2026*",
                    "Ana Torres (Supervisora Produccion)", "atorres@electromecanicaandina.com", "SOLICITANTE");
            asegurarUsuario(repoUsuario, repoRol, encoder, "costos", "Costos2026*",
                    "Jorge Ramirez (Analista de Costos)", "jramirez@electromecanicaandina.com", "COSTOS");

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
                crearProveedor(repoProveedor, "Ferreyros Industrial Corp SAC", "20100028698", "atencion@ferreyrosind.pe");
            }

            if (repoArticulo.count() == 0) {
                List<Categoria> cats = repoCategoria.findAll();
                List<UnidadMedida> ums = repoUnidadMedida.findAll();
                List<Ubicacion> ubs = repoUbicacion.findAll();
                List<Proveedor> provs = repoProveedor.findAll();

                if (!cats.isEmpty() && !ums.isEmpty() && !ubs.isEmpty() && !provs.isEmpty()) {
                    Articulo a1 = crearArticuloBase(repoArticulo, "EL-00101", "Interruptor Termomagnetico Trifasico 63A",
                            cats.get(0), ums.get(0), ubs.get(0), new BigDecimal("10.00"));
                    Articulo a2 = crearArticuloBase(repoArticulo, "EL-00102", "Cable THW-90 12 AWG Indeco (Rollo 100m)",
                            cats.get(0), ums.get(4 % ums.size()), ubs.get(0), new BigDecimal("8.00"));
                    Articulo a3 = crearArticuloBase(repoArticulo, "MC-00201", "Rodamiento Rigido de Bolas SKF 6205-2RSH",
                            cats.get(1 % cats.size()), ums.get(0), ubs.get(1 % ubs.size()), new BigDecimal("20.00"));
                    Articulo a4 = crearArticuloBase(repoArticulo, "IN-00301", "Lubricante Dielectrico Industrial WD-40",
                            cats.get(2 % cats.size()), ums.get(3 % ums.size()), ubs.get(1 % ubs.size()), new BigDecimal("15.00"));
                    Articulo a5 = crearArticuloBase(repoArticulo, "EP-00401", "Guantes Dielectricos Clase 00 Certificados",
                            cats.get(3 % cats.size()), ums.get(0), ubs.get(0), new BigDecimal("12.00"));

                    almacenService.registrarIngreso(new IngresoStockRequestDTO(
                            a1.getIdArticulo(), "LOT-2026-001", new BigDecimal("25.00"),
                            LocalDate.now().plusMonths(18), ubs.get(0).getId(), provs.get(0).getIdProveedor(), "GR-001-00451"));
                    almacenService.registrarIngreso(new IngresoStockRequestDTO(
                            a2.getIdArticulo(), "LOT-2026-002", new BigDecimal("14.00"),
                            LocalDate.now().plusMonths(24), ubs.get(0).getId(), provs.get(1 % provs.size()).getIdProveedor(), "GR-001-00452"));
                    almacenService.registrarIngreso(new IngresoStockRequestDTO(
                            a3.getIdArticulo(), "LOT-2026-003", new BigDecimal("12.00"),
                            LocalDate.now().plusDays(20), ubs.get(1 % ubs.size()).getId(), provs.get(0).getIdProveedor(), "GR-001-00453"));
                    almacenService.registrarIngreso(new IngresoStockRequestDTO(
                            a4.getIdArticulo(), "LOT-2026-004", new BigDecimal("30.00"),
                            LocalDate.now().plusDays(15), ubs.get(1 % ubs.size()).getId(), provs.get(2 % provs.size()).getIdProveedor(), "GR-001-00454"));
                    almacenService.registrarIngreso(new IngresoStockRequestDTO(
                            a5.getIdArticulo(), "LOT-2026-005", new BigDecimal("8.00"),
                            LocalDate.now().plusMonths(6), ubs.get(0).getId(), provs.get(1 % provs.size()).getIdProveedor(), "GR-001-00455"));
                }
            }
        };
    }

    private void asegurarUsuario(RepositorioUsuario repoUsuario, RepositorioRol repoRol,
                                 BCryptPasswordEncoder encoder, String username, String rawPassword,
                                 String nombreCompleto, String email, String rolNombre) {
        Usuario u = repoUsuario.buscarConRolesYPermisos(username).orElseGet(Usuario::new);
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(rawPassword));
        u.setNombreCompleto(nombreCompleto);
        if (u.getEmail() == null || u.getEmail().isBlank()) {
            u.setEmail(email);
        }
        u.setActivo(true);
        Set<Rol> rolesActualizados = new HashSet<>();
        if (u.getRoles() != null) {
            rolesActualizados.addAll(u.getRoles());
        }
        repoRol.findByNombre(rolNombre).ifPresent(rol -> {
            if (rolesActualizados.stream().noneMatch(r -> r.getNombre().equals(rolNombre))) {
                rolesActualizados.add(rol);
            }
        });
        u.setRoles(rolesActualizados);
        repoUsuario.save(u);
    }

    private Articulo crearArticuloBase(ArticuloRepository repo, String codigo, String descripcion,
                                       Categoria cat, UnidadMedida um, Ubicacion ub, BigDecimal stockMinimo) {
        Articulo a = new Articulo();
        a.setCodigoInterno(codigo);
        a.setDescripcion(descripcion);
        a.setCategoria(cat);
        a.setUnidadMedida(um);
        a.setUbicacion(ub);
        a.setStockMinimo(stockMinimo);
        a.setStockActual(BigDecimal.ZERO);
        return repo.save(a);
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