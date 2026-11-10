package com.example.inventario;

import com.example.inventario.DTOs.*;
import com.example.inventario.Entidades.*;
import com.example.inventario.Servicios.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InventarioApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServicioAuth servicioAuth;

    @Autowired
    private ServicioPermiso servicioPermiso;

    @Autowired
    private ServicioRol servicioRol;

    @Autowired
    private ServicioUsuario servicioUsuario;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private UnidadMedidaService unidadMedidaService;

    @Autowired
    private UbicacionService ubicacionService;

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private ArticuloService articuloService;

    @Autowired
    private AlmacenService almacenService;

    @Autowired
    private KardexService kardexService;

    @Test
    void contextLoadsAndFullFlowWorks() throws Exception {
        // 1. Verificar CargaInicial (Seguridad y Maestros)
        assertEquals(20, servicioPermiso.listar().size());
        assertEquals(5, servicioRol.listar().size());
        assertFalse(servicioUsuario.listar().isEmpty());
        assertFalse(categoriaService.listarTodas().isEmpty());
        assertFalse(unidadMedidaService.listarTodas().isEmpty());
        assertTrue(ubicacionService.listarTodas().size() >= 2);
        assertFalse(proveedorService.listartodos().isEmpty());

        // 2. Verificar Login JWT
        LoginResponse login = servicioAuth.login(new LoginRequest("admin", "Admin2026*"));
        assertNotNull(login.token());
        assertTrue(login.roles().contains("ADMINISTRADOR"));
        assertEquals(20, login.permisos().size());
        String bearer = "Bearer " + login.token();

        // 3. Crear Articulo con stock inicial 0
        Categoria cat = categoriaService.listarTodas().get(0);
        UnidadMedida um = unidadMedidaService.listarTodas().get(0);
        List<Ubicacion> ubicaciones = ubicacionService.listarTodas();
        Ubicacion ubOrigen = ubicaciones.get(0);
        Ubicacion ubDestino = ubicaciones.get(1);

        Articulo art = new Articulo();
        art.setCodigoInterno("ART-TEST-001");
        art.setDescripcion("Contactor Trifasico 32A");
        art.setCategoria(cat);
        art.setUnidadMedida(um);
        art.setUbicacion(ubOrigen);
        art.setStockMinimo(new BigDecimal("15.00"));
        art.setStockActual(BigDecimal.ZERO);

        Articulo guardado = articuloService.guardar(art);
        assertNotNull(guardado.getIdArticulo());

        // 4. Registrar dos Ingresos en lotes distintos
        MovimientoResponseDTO ing1 = almacenService.registrarIngreso(new IngresoStockRequestDTO(
                guardado.getIdArticulo(),
                "LOTE-A",
                new BigDecimal("10.00"),
                LocalDate.now().plusDays(15),
                ubOrigen.getId(),
                null,
                "GR-001-0001"));
        assertEquals(TipoMovimiento.INGRESO, ing1.tipoMovimiento());

        MovimientoResponseDTO ing2 = almacenService.registrarIngreso(new IngresoStockRequestDTO(
                guardado.getIdArticulo(),
                "LOTE-B",
                new BigDecimal("20.00"),
                LocalDate.now().plusDays(25),
                ubOrigen.getId(),
                null,
                "GR-001-0002"));
        assertEquals(TipoMovimiento.INGRESO, ing2.tipoMovimiento());

        Articulo trasIngresos = articuloService.obtenerPorId(guardado.getIdArticulo()).orElseThrow();
        assertEquals(0, new BigDecimal("30.00").compareTo(trasIngresos.getStockActual()));

        // 5. Registrar Salida FIFO (requiere 15: debe agotar LOTE-A de 10 y tomar 5 de LOTE-B)
        List<MovimientoResponseDTO> salidas = almacenService.registrarSalidaFIFO(
                new SalidaStockRequestDTO(guardado.getIdArticulo(), new BigDecimal("15.00"), "OP-2026-01", null));
        assertEquals(2, salidas.size());
        assertEquals("LOTE-A", salidas.get(0).codigoLote());
        assertEquals(0, new BigDecimal("10.00").compareTo(salidas.get(0).cantidad()));
        assertEquals("LOTE-B", salidas.get(1).codigoLote());
        assertEquals(0, new BigDecimal("5.00").compareTo(salidas.get(1).cantidad()));

        Articulo trasSalida = articuloService.obtenerPorId(guardado.getIdArticulo()).orElseThrow();
        assertEquals(0, new BigDecimal("15.00").compareTo(trasSalida.getStockActual()));

        // Como stockActual (15) <= stockMinimo (15), debe aparecer en bajo stock
        assertTrue(articuloService.listarArticulosBajoStock().stream()
                .anyMatch(a -> a.getIdArticulo().equals(guardado.getIdArticulo())));

        // 6. Registrar Transferencia del LOTE-B restante (5 unidades de ubOrigen a ubDestino)
        Long loteBId = salidas.get(1).loteId();
        MovimientoResponseDTO trf = almacenService.registrarTransferencia(
                new TransferenciaStockRequestDTO(loteBId, ubOrigen.getId(), ubDestino.getId(),
                        new BigDecimal("5.00"), "Reubicacion de estante"));
        assertEquals(TipoMovimiento.TRANSFERENCIA, trf.tipoMovimiento());

        // 7. Verificar Lotes próximos a vencer, Documentos Cabecera-Detalle y Kardex
        assertFalse(almacenService.listarLotesProximosAVencer().isEmpty());
        assertFalse(almacenService.listarDocumentosIngreso().isEmpty());
        assertFalse(almacenService.listarDocumentosSalida().isEmpty());
        assertFalse(almacenService.listarDocumentosTransferencia().isEmpty());

        List<Kardex> historialKardex = kardexService.listarPorArticulo(guardado.getIdArticulo());
        assertEquals(4, historialKardex.size()); // 2 ingresos + 1 salida + 1 transferencia

        // 8. Verificar serialización JSON y autorización HTTP con MockMvc (open-in-view=false)
        mockMvc.perform(get("/api/kardex").header("Authorization", bearer).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/kardex/articulo/" + guardado.getIdArticulo()).header("Authorization", bearer).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/articulos").header("Authorization", bearer).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/articulos/bajo-stock").header("Authorization", bearer).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/categorias").header("Authorization", bearer).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/unidades-medida").header("Authorization", bearer).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/ubicaciones").header("Authorization", bearer).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/proveedores").header("Authorization", bearer).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/almacen/ingresos").header("Authorization", bearer).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/almacen/ingresos/documentos").header("Authorization", bearer).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/almacen/salidas").header("Authorization", bearer).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/almacen/salidas/documentos").header("Authorization", bearer).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/almacen/transferencias").header("Authorization", bearer).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/almacen/transferencias/documentos").header("Authorization", bearer).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/almacen/movimientos").header("Authorization", bearer).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/almacen/lotes").header("Authorization", bearer).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/almacen/reportes/vencimientos").header("Authorization", bearer).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/almacen/reportes/minimos").header("Authorization", bearer).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/usuarios").header("Authorization", bearer).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/roles").header("Authorization", bearer).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/permisos").header("Authorization", bearer).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 9. Verificar denegación sin token (401/403)
        mockMvc.perform(get("/api/usuarios").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}


