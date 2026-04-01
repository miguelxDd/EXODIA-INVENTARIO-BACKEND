package com.exodia.inventario.integration;

import com.exodia.inventario.domain.modelo.catalogo.*;
import com.exodia.inventario.repositorio.catalogo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@AutoConfigureMockMvc
class RecepcionIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private BodegaRepository bodegaRepository;
    @Autowired private UbicacionRepository ubicacionRepository;
    @Autowired private UnidadRepository unidadRepository;

    private Long empresaId;
    private Long bodegaId;
    private Long ubicacionId;
    private Long unidadId;

    @BeforeEach
    void setUp() {
        Empresa empresa = empresaRepository.findAll().stream()
                .filter(e -> "REC-TEST".equals(e.getCodigo()))
                .findFirst()
                .orElseGet(() -> empresaRepository.save(
                        Empresa.builder().codigo("REC-TEST").nombre("Empresa Recepcion").build()));
        empresaId = empresa.getId();

        Bodega bodega = bodegaRepository.findByEmpresaIdAndActivoTrue(empresaId).stream()
                .filter(b -> "BOD-REC".equals(b.getCodigo()))
                .findFirst()
                .orElseGet(() -> bodegaRepository.save(
                        Bodega.builder().empresa(empresa).codigo("BOD-REC").nombre("Bodega Recepcion").build()));
        bodegaId = bodega.getId();

        Ubicacion ubicacion = ubicacionRepository.findByBodegaIdAndActivoTrue(bodegaId).stream()
                .filter(u -> "UB-REC".equals(u.getCodigo()))
                .findFirst()
                .orElseGet(() -> ubicacionRepository.save(
                        Ubicacion.builder().bodega(bodega).codigo("UB-REC").nombre("Ubicacion Recepcion").build()));
        ubicacionId = ubicacion.getId();

        Unidad unidad = unidadRepository.findByEmpresaIdAndActivoTrue(empresaId).stream()
                .filter(u -> "UND-REC".equals(u.getCodigo()))
                .findFirst()
                .orElseGet(() -> unidadRepository.save(
                        Unidad.builder().empresa(empresa).codigo("UND-REC").nombre("Unidad").abreviatura("UR").build()));
        unidadId = unidad.getId();
    }

    @Test
    void deberiaCrearRecepcionCompleta() throws Exception {
        String recepcionJson = String.format("""
                {
                    "bodegaId": %d,
                    "tipoRecepcion": "MANUAL",
                    "lineas": [{
                        "productoId": 100,
                        "unidadId": %d,
                        "ubicacionId": %d,
                        "cantidad": 25,
                        "precioUnitario": 10.00
                    }]
                }
                """, bodegaId, unidadId, ubicacionId);

        mockMvc.perform(post("/api/v1/recepciones")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recepcionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exito").value(true))
                .andExpect(jsonPath("$.datos.numeroRecepcion").isNotEmpty())
                .andExpect(jsonPath("$.datos.estado").value("CONFIRMADO"));
    }

    @Test
    void deberiaCrearRecepcionYConsultarStock() throws Exception {
        String recepcionJson = String.format("""
                {
                    "bodegaId": %d,
                    "tipoRecepcion": "MANUAL",
                    "lineas": [{
                        "productoId": 101,
                        "unidadId": %d,
                        "ubicacionId": %d,
                        "cantidad": 50,
                        "precioUnitario": 5.00
                    }]
                }
                """, bodegaId, unidadId, ubicacionId);

        mockMvc.perform(post("/api/v1/recepciones")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recepcionJson))
                .andExpect(status().isCreated());

        // Consultar stock consolidado
        mockMvc.perform(get("/api/v1/inventario/stock/consolidado")
                        .header("X-Empresa-Id", empresaId)
                        .param("pagina", "0")
                        .param("tamanio", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exito").value(true));

        // Consultar stock por producto y bodega
        mockMvc.perform(get("/api/v1/inventario/stock/producto-bodega")
                        .header("X-Empresa-Id", empresaId)
                        .param("productoId", "101")
                        .param("bodegaId", bodegaId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void deberiaCrearRecepcionYConsultarKardex() throws Exception {
        String recepcionJson = String.format("""
                {
                    "bodegaId": %d,
                    "tipoRecepcion": "MANUAL",
                    "lineas": [{
                        "productoId": 102,
                        "unidadId": %d,
                        "ubicacionId": %d,
                        "cantidad": 10,
                        "precioUnitario": 20.00
                    }]
                }
                """, bodegaId, unidadId, ubicacionId);

        mockMvc.perform(post("/api/v1/recepciones")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recepcionJson))
                .andExpect(status().isCreated());

        // Consultar kardex
        mockMvc.perform(get("/api/v1/inventario/kardex")
                        .header("X-Empresa-Id", empresaId)
                        .param("pagina", "0")
                        .param("tamanio", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exito").value(true));
    }

    @Test
    void deberiaFallarConBodegaInexistente() throws Exception {
        String recepcionJson = String.format("""
                {
                    "bodegaId": 999999,
                    "tipoRecepcion": "MANUAL",
                    "lineas": [{
                        "productoId": 100,
                        "unidadId": %d,
                        "ubicacionId": %d,
                        "cantidad": 10,
                        "precioUnitario": 5.00
                    }]
                }
                """, unidadId, ubicacionId);

        mockMvc.perform(post("/api/v1/recepciones")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recepcionJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void deberiaListarContenedoresProximosAVencerSegunConfiguracionEmpresa() throws Exception {
        mockMvc.perform(patch("/api/v1/configuracion-empresa")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "diasAlertaVencimiento": 7
                                }
                                """))
                .andExpect(status().isOk());

        LocalDate fechaCercana = LocalDate.now().plusDays(5);
        LocalDate fechaLejana = LocalDate.now().plusDays(20);

        recepcionarConVencimiento(150, 10, 12.00, "LOT-CERCA", fechaCercana);
        recepcionarConVencimiento(151, 10, 14.00, "LOT-LEJOS", fechaLejana);

        mockMvc.perform(get("/api/v1/inventario/stock/proximos-a-vencer")
                        .header("X-Empresa-Id", empresaId)
                        .param("bodegaId", bodegaId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos[?(@.numeroLote=='LOT-CERCA')]", hasSize(1)))
                .andExpect(jsonPath("$.datos[?(@.numeroLote=='LOT-LEJOS')]", hasSize(0)));
    }

    private MvcResult recepcionarConVencimiento(int productoId,
                                                int cantidad,
                                                double precioUnitario,
                                                String numeroLote,
                                                LocalDate fechaVencimiento) throws Exception {
        String recepcionJson = String.format("""
                {
                    "bodegaId": %d,
                    "tipoRecepcion": "MANUAL",
                    "lineas": [{
                        "productoId": %d,
                        "unidadId": %d,
                        "ubicacionId": %d,
                        "cantidad": %d,
                        "precioUnitario": %.2f,
                        "numeroLote": "%s",
                        "fechaVencimiento": "%s"
                    }]
                }
                """, bodegaId, productoId, unidadId, ubicacionId, cantidad,
                precioUnitario, numeroLote, fechaVencimiento);

        return mockMvc.perform(post("/api/v1/recepciones")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recepcionJson))
                .andExpect(status().isCreated())
                .andReturn();
    }
}
