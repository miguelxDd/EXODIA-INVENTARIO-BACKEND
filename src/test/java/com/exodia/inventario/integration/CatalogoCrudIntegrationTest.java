package com.exodia.inventario.integration;

import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@AutoConfigureMockMvc
class CatalogoCrudIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EmpresaRepository empresaRepository;

    private Long empresaId;

    @BeforeEach
    void setUp() {
        Empresa empresa = empresaRepository.findAll().stream()
                .filter(e -> "CRUD-TEST".equals(e.getCodigo()))
                .findFirst()
                .orElseGet(() -> empresaRepository.save(
                        Empresa.builder().codigo("CRUD-TEST").nombre("Empresa CRUD Test").build()));
        empresaId = empresa.getId();
    }

    @Test
    void deberiaCrearBodegaYObtenerla() throws Exception {
        String bodegaJson = """
                {"codigo":"BOD-INT","nombre":"Bodega Integracion","direccion":"Dir","ciudad":"Ciudad","pais":"Pais"}
                """;

        MvcResult createResult = mockMvc.perform(post("/api/v1/bodegas")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodegaJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exito").value(true))
                .andExpect(jsonPath("$.datos.codigo").value("BOD-INT"))
                .andReturn();

        Integer bodegaId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("datos").path("id").asInt();

        mockMvc.perform(get("/api/v1/bodegas/" + bodegaId)
                        .header("X-Empresa-Id", empresaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.codigo").value("BOD-INT"))
                .andExpect(jsonPath("$.datos.nombre").value("Bodega Integracion"));
    }

    @Test
    void deberiaCrearUnidadYListarPorEmpresa() throws Exception {
        String unidadJson = """
                {"codigo":"KG-INT","nombre":"Kilogramo","abreviatura":"kg"}
                """;

        mockMvc.perform(post("/api/v1/unidades")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(unidadJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.datos.codigo").value("KG-INT"));

        mockMvc.perform(get("/api/v1/unidades")
                        .header("X-Empresa-Id", empresaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exito").value(true));
    }

    @Test
    void deberiaCrearUbicacionEnBodega() throws Exception {
        // Crear bodega
        String bodegaJson = """
                {"codigo":"BOD-UB","nombre":"Bodega Ubicacion"}
                """;

        MvcResult bodegaResult = mockMvc.perform(post("/api/v1/bodegas")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodegaJson))
                .andExpect(status().isCreated())
                .andReturn();

        Integer bodegaId = objectMapper.readTree(bodegaResult.getResponse().getContentAsString())
                .path("datos").path("id").asInt();

        // Crear ubicacion
        String ubicacionJson = String.format("""
                {"bodegaId":%d,"codigo":"UB-INT","nombre":"Ubicacion Int","tipoUbicacion":"GENERAL"}
                """, bodegaId);

        mockMvc.perform(post("/api/v1/ubicaciones")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ubicacionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.datos.codigo").value("UB-INT"));
    }

    @Test
    void deberiaRetornar404SiEmpresaNoExiste() throws Exception {
        String bodegaJson = """
                {"codigo":"FAIL","nombre":"Falla"}
                """;

        mockMvc.perform(post("/api/v1/bodegas")
                        .header("X-Empresa-Id", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodegaJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exito").value(false));
    }

    @Test
    void deberiaDesactivarBodegaYNoEncontrarla() throws Exception {
        String bodegaJson = """
                {"codigo":"BOD-DEL","nombre":"Bodega a Desactivar"}
                """;

        MvcResult createResult = mockMvc.perform(post("/api/v1/bodegas")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodegaJson))
                .andExpect(status().isCreated())
                .andReturn();

        Integer bodegaId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("datos").path("id").asInt();

        // Desactivar
        mockMvc.perform(delete("/api/v1/bodegas/" + bodegaId + "/desactivar")
                        .header("X-Empresa-Id", empresaId))
                .andExpect(status().isNoContent());

        // Ya no se puede obtener
        mockMvc.perform(get("/api/v1/bodegas/" + bodegaId)
                        .header("X-Empresa-Id", empresaId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deberiaActualizarBodega() throws Exception {
        String bodegaJson = """
                {"codigo":"BOD-UPD","nombre":"Original"}
                """;

        MvcResult createResult = mockMvc.perform(post("/api/v1/bodegas")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodegaJson))
                .andExpect(status().isCreated())
                .andReturn();

        Integer bodegaId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("datos").path("id").asInt();

        String updateJson = """
                {"nombre":"Actualizada"}
                """;

        mockMvc.perform(patch("/api/v1/bodegas/" + bodegaId)
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.nombre").value("Actualizada"));
    }
}
