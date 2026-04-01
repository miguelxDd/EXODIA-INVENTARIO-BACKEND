package com.exodia.inventario.integration;

import com.exodia.inventario.domain.modelo.catalogo.*;
import com.exodia.inventario.repositorio.catalogo.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integracion: conteo fisico con aplicacion de diferencias.
 * Cubre: recepcion -> crear conteo -> registrar linea con diferencia -> aplicar -> verificar ajuste.
 */
@Tag("integration")
@AutoConfigureMockMvc
class ConteoFisicoIntegrationTest extends BaseIntegrationTest {

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
                .filter(e -> "CNT-TEST".equals(e.getCodigo()))
                .findFirst()
                .orElseGet(() -> empresaRepository.save(
                        Empresa.builder().codigo("CNT-TEST").nombre("Empresa Conteo").build()));
        empresaId = empresa.getId();

        Bodega bodega = bodegaRepository.findByEmpresaIdAndActivoTrue(empresaId).stream()
                .filter(b -> "BOD-CN".equals(b.getCodigo()))
                .findFirst()
                .orElseGet(() -> bodegaRepository.save(
                        Bodega.builder().empresa(empresa).codigo("BOD-CN").nombre("Bodega Conteo").build()));
        bodegaId = bodega.getId();

        Ubicacion ubicacion = ubicacionRepository.findByBodegaIdAndActivoTrue(bodegaId).stream()
                .filter(u -> "UB-CN".equals(u.getCodigo()))
                .findFirst()
                .orElseGet(() -> ubicacionRepository.save(
                        Ubicacion.builder().bodega(bodega).codigo("UB-CN").nombre("Ubicacion Conteo").build()));
        ubicacionId = ubicacion.getId();

        Unidad unidad = unidadRepository.findByEmpresaIdAndActivoTrue(empresaId).stream()
                .filter(u -> "UND-CN".equals(u.getCodigo()))
                .findFirst()
                .orElseGet(() -> unidadRepository.save(
                        Unidad.builder().empresa(empresa).codigo("UND-CN").nombre("Unidad").abreviatura("UC").build()));
        unidadId = unidad.getId();
    }

    @Test
    void deberiaAplicarConteoConDiferenciaNegativa() throws Exception {
        // 1. Recepcionar 100 unidades
        String recepcionJson = String.format("""
                {
                    "bodegaId": %d,
                    "tipoRecepcion": "MANUAL",
                    "lineas": [{
                        "productoId": 500,
                        "unidadId": %d,
                        "ubicacionId": %d,
                        "cantidad": 100,
                        "precioUnitario": 20.00
                    }]
                }
                """, bodegaId, unidadId, ubicacionId);

        MvcResult recepcionResult = mockMvc.perform(post("/api/v1/recepciones")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recepcionJson))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode recepcionNode = objectMapper.readTree(recepcionResult.getResponse().getContentAsString());
        Long contenedorId = recepcionNode.at("/datos/lineas").get(0).get("contenedorId").asLong();

        // 2. Crear conteo fisico
        String conteoJson = String.format("""
                {
                    "bodegaId": %d,
                    "comentarios": "Conteo trimestral"
                }
                """, bodegaId);

        MvcResult conteoResult = mockMvc.perform(post("/api/v1/conteos")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conteoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.datos.estado").value("EN_PROGRESO"))
                .andReturn();

        Long conteoId = objectMapper.readTree(conteoResult.getResponse().getContentAsString())
                .at("/datos/id").asLong();

        // 3. Registrar linea: contamos 85 (faltaron 15)
        String lineaJson = String.format("""
                {
                    "contenedorId": %d,
                    "cantidadContada": 85
                }
                """, contenedorId);

        mockMvc.perform(post("/api/v1/conteos/{id}/lineas", conteoId)
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(lineaJson))
                .andExpect(status().isOk());

        // 4. Aplicar conteo (genera ajuste automatico: -15)
        mockMvc.perform(patch("/api/v1/conteos/{id}/aplicar", conteoId)
                        .header("X-Empresa-Id", empresaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.estado").value("APLICADO"));

        // 5. Verificar stock ajustado = 85
        mockMvc.perform(get("/api/v1/inventario/stock/contenedor/{contenedorId}", contenedorId)
                        .header("X-Empresa-Id", empresaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos").value(closeTo(85.0, 0.01)));
    }

    @Test
    void deberiaAplicarConteoConDiferenciaPositiva() throws Exception {
        // 1. Recepcionar 20 unidades
        String recepcionJson = String.format("""
                {
                    "bodegaId": %d,
                    "tipoRecepcion": "MANUAL",
                    "lineas": [{
                        "productoId": 501,
                        "unidadId": %d,
                        "ubicacionId": %d,
                        "cantidad": 20,
                        "precioUnitario": 15.00
                    }]
                }
                """, bodegaId, unidadId, ubicacionId);

        MvcResult recepcionResult = mockMvc.perform(post("/api/v1/recepciones")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recepcionJson))
                .andExpect(status().isCreated())
                .andReturn();

        Long contenedorId = objectMapper.readTree(recepcionResult.getResponse().getContentAsString())
                .at("/datos/lineas").get(0).get("contenedorId").asLong();

        // 2. Crear y registrar conteo: contamos 25 (sobraron 5)
        String conteoJson = String.format("""
                {
                    "bodegaId": %d,
                    "comentarios": "Conteo sorpresa positiva"
                }
                """, bodegaId);

        MvcResult conteoResult = mockMvc.perform(post("/api/v1/conteos")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conteoJson))
                .andExpect(status().isCreated())
                .andReturn();

        Long conteoId = objectMapper.readTree(conteoResult.getResponse().getContentAsString())
                .at("/datos/id").asLong();

        String lineaJson = String.format("""
                {
                    "contenedorId": %d,
                    "cantidadContada": 25
                }
                """, contenedorId);

        mockMvc.perform(post("/api/v1/conteos/{id}/lineas", conteoId)
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(lineaJson))
                .andExpect(status().isOk());

        // 3. Aplicar
        mockMvc.perform(patch("/api/v1/conteos/{id}/aplicar", conteoId)
                        .header("X-Empresa-Id", empresaId))
                .andExpect(status().isOk());

        // 4. Verificar stock = 25
        mockMvc.perform(get("/api/v1/inventario/stock/contenedor/{contenedorId}", contenedorId)
                        .header("X-Empresa-Id", empresaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos").value(closeTo(25.0, 0.01)));
    }
}
