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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integracion: picking FEFO.
 * Cubre: recepcion de stock con lotes -> picking que selecciona FEFO
 *        -> verificar deduccion -> verificar orden FEFO via kardex.
 */
@Tag("integration")
@AutoConfigureMockMvc
class PickingIntegrationTest extends BaseIntegrationTest {

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
                .filter(e -> "PIK-TEST".equals(e.getCodigo()))
                .findFirst()
                .orElseGet(() -> empresaRepository.save(
                        Empresa.builder().codigo("PIK-TEST").nombre("Empresa Picking").build()));
        empresaId = empresa.getId();

        Bodega bodega = bodegaRepository.findByEmpresaIdAndActivoTrue(empresaId).stream()
                .filter(b -> "BOD-PK".equals(b.getCodigo()))
                .findFirst()
                .orElseGet(() -> bodegaRepository.save(
                        Bodega.builder().empresa(empresa).codigo("BOD-PK").nombre("Bodega Picking").build()));
        bodegaId = bodega.getId();

        Ubicacion ubicacion = ubicacionRepository.findByBodegaIdAndActivoTrue(bodegaId).stream()
                .filter(u -> "UB-PK".equals(u.getCodigo()))
                .findFirst()
                .orElseGet(() -> ubicacionRepository.save(
                        Ubicacion.builder().bodega(bodega).codigo("UB-PK").nombre("Ubicacion Picking").build()));
        ubicacionId = ubicacion.getId();

        Unidad unidad = unidadRepository.findByEmpresaIdAndActivoTrue(empresaId).stream()
                .filter(u -> "UND-PK".equals(u.getCodigo()))
                .findFirst()
                .orElseGet(() -> unidadRepository.save(
                        Unidad.builder().empresa(empresa).codigo("UND-PK").nombre("Unidad").abreviatura("UP").build()));
        unidadId = unidad.getId();
    }

    @Test
    void deberiaEjecutarPickingFEFOYDeducirStockEnOrdenCorrecto() throws Exception {
        // 1. Recepcionar lote A (vence 2026-06-01) - 30 unidades
        String recepcionLoteA = String.format("""
                {
                    "bodegaId": %d,
                    "tipoRecepcion": "MANUAL",
                    "lineas": [{
                        "productoId": 300,
                        "unidadId": %d,
                        "ubicacionId": %d,
                        "cantidad": 30,
                        "precioUnitario": 10.00,
                        "numeroLote": "LOTE-A",
                        "fechaVencimiento": "2026-06-01"
                    }]
                }
                """, bodegaId, unidadId, ubicacionId);

        MvcResult recA = mockMvc.perform(post("/api/v1/recepciones")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recepcionLoteA))
                .andExpect(status().isCreated())
                .andReturn();

        String barcodeA = objectMapper.readTree(recA.getResponse().getContentAsString())
                .at("/datos/lineas").get(0).get("codigoBarras").asText();

        // 2. Recepcionar lote B (vence 2026-12-01) - 50 unidades
        String recepcionLoteB = String.format("""
                {
                    "bodegaId": %d,
                    "tipoRecepcion": "MANUAL",
                    "lineas": [{
                        "productoId": 300,
                        "unidadId": %d,
                        "ubicacionId": %d,
                        "cantidad": 50,
                        "precioUnitario": 12.00,
                        "numeroLote": "LOTE-B",
                        "fechaVencimiento": "2026-12-01"
                    }]
                }
                """, bodegaId, unidadId, ubicacionId);

        MvcResult recB = mockMvc.perform(post("/api/v1/recepciones")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recepcionLoteB))
                .andExpect(status().isCreated())
                .andReturn();

        String barcodeB = objectMapper.readTree(recB.getResponse().getContentAsString())
                .at("/datos/lineas").get(0).get("codigoBarras").asText();

        // 3. Crear picking de 40 unidades (FEFO: deberia tomar 30 de LOTE-A + 10 de LOTE-B)
        String pickingJson = String.format("""
                {
                    "bodegaId": %d,
                    "tipoPicking": "GENERAL",
                    "lineas": [{
                        "productoId": 300,
                        "unidadId": %d,
                        "cantidadSolicitada": 40
                    }]
                }
                """, bodegaId, unidadId);

        MvcResult createResult = mockMvc.perform(post("/api/v1/picking")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pickingJson))
                .andExpect(status().isCreated())
                .andReturn();

        Long pickingId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .at("/datos/id").asLong();

        // 4. Ejecutar picking
        mockMvc.perform(patch("/api/v1/picking/{id}/ejecutar", pickingId)
                        .header("X-Empresa-Id", empresaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.estado").value("COMPLETADO"));

        // 5. Verificar stock total restante: 80 - 40 = 40
        mockMvc.perform(get("/api/v1/inventario/stock/producto-bodega")
                        .header("X-Empresa-Id", empresaId)
                        .param("productoId", "300")
                        .param("bodegaId", bodegaId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos").value(closeTo(40.0, 0.01)));

        // 6. Verificar orden FEFO via kardex: las operaciones PICKING deben haber tomado
        //    primero del contenedor con LOTE-A (vence antes), despues de LOTE-B
        MvcResult kardexResult = mockMvc.perform(get("/api/v1/inventario/kardex")
                        .header("X-Empresa-Id", empresaId)
                        .param("productoId", "300")
                        .param("bodegaId", bodegaId.toString())
                        .param("pagina", "0")
                        .param("tamanio", "50"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode kardex = objectMapper.readTree(kardexResult.getResponse().getContentAsString());
        JsonNode operaciones = kardex.at("/datos/contenido");

        // Recopilar operaciones PICKING en orden
        List<JsonNode> pickingOps = new ArrayList<>();
        for (JsonNode op : operaciones) {
            if ("PICKING".equals(op.get("tipoOperacionCodigo").asText())) {
                pickingOps.add(op);
            }
        }

        assertTrue(pickingOps.size() >= 2,
                "Debe haber al menos 2 operaciones PICKING (una por cada contenedor FEFO)");

        // Verificar que LOTE-A fue agotado completamente (30 unidades)
        // y LOTE-B solo parcialmente (10 unidades)
        boolean loteAPickeado = false;
        boolean loteBPickeado = false;
        for (JsonNode op : pickingOps) {
            String barcode = op.get("codigoBarras").asText();
            if (barcodeA.equals(barcode)) {
                loteAPickeado = true;
                // LOTE-A debe tener picking de -30 (todo el lote)
                assertEquals(-30.0, op.get("cantidad").asDouble(), 0.01,
                        "LOTE-A (primero en vencer) debe pickearse completamente: 30 unidades");
            } else if (barcodeB.equals(barcode)) {
                loteBPickeado = true;
                // LOTE-B debe tener picking de -10 (parcial)
                assertEquals(-10.0, op.get("cantidad").asDouble(), 0.01,
                        "LOTE-B (segundo en vencer) solo debe pickearse parcialmente: 10 unidades");
            }
        }

        assertTrue(loteAPickeado, "LOTE-A (vence primero) debe haber sido pickeado (FEFO)");
        assertTrue(loteBPickeado, "LOTE-B debe haber sido pickeado parcialmente para completar 40");

        // 7. Verificar stock individual: LOTE-A debe quedar en 0, LOTE-B en 40
        mockMvc.perform(get("/api/v1/inventario/stock/barcode/{codigoBarras}", barcodeA)
                        .header("X-Empresa-Id", empresaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos").value(closeTo(0.0, 0.01)));

        mockMvc.perform(get("/api/v1/inventario/stock/barcode/{codigoBarras}", barcodeB)
                        .header("X-Empresa-Id", empresaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos").value(closeTo(40.0, 0.01)));
    }

    @Test
    void deberiaFallarPickingSinStockSuficiente() throws Exception {
        String pickingJson = String.format("""
                {
                    "bodegaId": %d,
                    "tipoPicking": "GENERAL",
                    "lineas": [{
                        "productoId": 998,
                        "unidadId": %d,
                        "cantidadSolicitada": 1000
                    }]
                }
                """, bodegaId, unidadId);

        MvcResult createResult = mockMvc.perform(post("/api/v1/picking")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pickingJson))
                .andExpect(status().isCreated())
                .andReturn();

        Long pickingId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .at("/datos/id").asLong();

        mockMvc.perform(patch("/api/v1/picking/{id}/ejecutar", pickingId)
                        .header("X-Empresa-Id", empresaId))
                .andExpect(status().is4xxClientError());
    }
}
