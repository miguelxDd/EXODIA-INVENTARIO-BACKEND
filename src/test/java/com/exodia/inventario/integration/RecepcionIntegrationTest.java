package com.exodia.inventario.integration;

import com.exodia.inventario.domain.modelo.catalogo.*;
import com.exodia.inventario.repositorio.catalogo.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@AutoConfigureMockMvc
class RecepcionIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
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
}
