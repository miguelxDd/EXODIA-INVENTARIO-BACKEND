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
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integracion: flujo completo de transferencia entre bodegas.
 * Cubre: recepcion de stock -> crear transferencia -> confirmar -> despachar
 *        -> recibir parcial (verificar stock destino intermedio)
 *        -> recibir resto (verificar stock destino final y trazabilidad).
 */
@Tag("integration")
@AutoConfigureMockMvc
class TransferenciaIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private BodegaRepository bodegaRepository;
    @Autowired private UbicacionRepository ubicacionRepository;
    @Autowired private UnidadRepository unidadRepository;

    private Long empresaId;
    private Long bodegaOrigenId;
    private Long bodegaDestinoId;
    private Long ubicacionOrigenId;
    private Long ubicacionDestinoId;
    private Long unidadId;

    @BeforeEach
    void setUp() {
        Empresa empresa = empresaRepository.findAll().stream()
                .filter(e -> "TRF-TEST".equals(e.getCodigo()))
                .findFirst()
                .orElseGet(() -> empresaRepository.save(
                        Empresa.builder().codigo("TRF-TEST").nombre("Empresa Transferencia").build()));
        empresaId = empresa.getId();

        Bodega bodegaOrigen = bodegaRepository.findByEmpresaIdAndActivoTrue(empresaId).stream()
                .filter(b -> "BOD-OR".equals(b.getCodigo()))
                .findFirst()
                .orElseGet(() -> bodegaRepository.save(
                        Bodega.builder().empresa(empresa).codigo("BOD-OR").nombre("Bodega Origen").build()));
        bodegaOrigenId = bodegaOrigen.getId();

        Bodega bodegaDestino = bodegaRepository.findByEmpresaIdAndActivoTrue(empresaId).stream()
                .filter(b -> "BOD-DS".equals(b.getCodigo()))
                .findFirst()
                .orElseGet(() -> bodegaRepository.save(
                        Bodega.builder().empresa(empresa).codigo("BOD-DS").nombre("Bodega Destino").build()));
        bodegaDestinoId = bodegaDestino.getId();

        Ubicacion ubicacionOrigen = ubicacionRepository.findByBodegaIdAndActivoTrue(bodegaOrigenId).stream()
                .filter(u -> "UB-OR".equals(u.getCodigo()))
                .findFirst()
                .orElseGet(() -> ubicacionRepository.save(
                        Ubicacion.builder().bodega(bodegaOrigen).codigo("UB-OR").nombre("Ubicacion Origen").build()));
        ubicacionOrigenId = ubicacionOrigen.getId();

        Ubicacion ubicacionDestino = ubicacionRepository.findByBodegaIdAndActivoTrue(bodegaDestinoId).stream()
                .filter(u -> "UB-DS".equals(u.getCodigo()))
                .findFirst()
                .orElseGet(() -> ubicacionRepository.save(
                        Ubicacion.builder().bodega(bodegaDestino).codigo("UB-DS").nombre("Ubicacion Destino").build()));
        ubicacionDestinoId = ubicacionDestino.getId();

        Unidad unidad = unidadRepository.findByEmpresaIdAndActivoTrue(empresaId).stream()
                .filter(u -> "UND-TR".equals(u.getCodigo()))
                .findFirst()
                .orElseGet(() -> unidadRepository.save(
                        Unidad.builder().empresa(empresa).codigo("UND-TR").nombre("Unidad").abreviatura("UT").build()));
        unidadId = unidad.getId();
    }

    @Test
    void deberiaCompletarFlujoTransferenciaConRecepcionParcial() throws Exception {
        // 1. Recepcionar stock en bodega origen (100 unidades)
        String recepcionJson = String.format("""
                {
                    "bodegaId": %d,
                    "tipoRecepcion": "MANUAL",
                    "lineas": [{
                        "productoId": 200,
                        "unidadId": %d,
                        "ubicacionId": %d,
                        "cantidad": 100,
                        "precioUnitario": 15.00
                    }]
                }
                """, bodegaOrigenId, unidadId, ubicacionOrigenId);

        mockMvc.perform(post("/api/v1/recepciones")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recepcionJson))
                .andExpect(status().isCreated());

        // 2. Crear transferencia POR_PRODUCTO de 40 unidades
        String transferenciaJson = String.format("""
                {
                    "bodegaOrigenId": %d,
                    "bodegaDestinoId": %d,
                    "tipoTransferencia": "POR_PRODUCTO",
                    "lineas": [{
                        "productoId": 200,
                        "unidadId": %d,
                        "cantidadSolicitada": 40
                    }]
                }
                """, bodegaOrigenId, bodegaDestinoId, unidadId);

        MvcResult createResult = mockMvc.perform(post("/api/v1/transferencias")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferenciaJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.datos.estadoCodigo").value("BORRADOR"))
                .andReturn();

        Long transferenciaId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .at("/datos/id").asLong();

        // 3. Confirmar
        mockMvc.perform(patch("/api/v1/transferencias/{id}/confirmar", transferenciaId)
                        .header("X-Empresa-Id", empresaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.estadoCodigo").value("CONFIRMADO"));

        // 4. Despachar (resuelve contenedores FEFO, crea operaciones salida)
        MvcResult despachoResult = mockMvc.perform(patch("/api/v1/transferencias/{id}/despachar", transferenciaId)
                        .header("X-Empresa-Id", empresaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.estadoCodigo").value("EN_TRANSITO"))
                .andReturn();

        Long contenedorId = objectMapper.readTree(despachoResult.getResponse().getContentAsString())
                .at("/datos/contenedores").get(0).get("contenedorId").asLong();

        // 5. Recibir parcial (25 de 40)
        String recepcionParcialJson = String.format("""
                {
                    "ubicacionDestinoId": %d,
                    "contenedores": [{
                        "contenedorId": %d,
                        "cantidadRecibida": 25
                    }]
                }
                """, ubicacionDestinoId, contenedorId);

        MvcResult parcialResult = mockMvc.perform(patch("/api/v1/transferencias/{id}/recibir", transferenciaId)
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recepcionParcialJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.estadoCodigo").value("RECIBIDO_PARCIAL"))
                .andReturn();

        // Verificar que el contenedor NO fue marcado como recibido (parcial: 25 de 40)
        JsonNode parcialNode = objectMapper.readTree(parcialResult.getResponse().getContentAsString());
        JsonNode contenedorParcial = parcialNode.at("/datos/contenedores").get(0);
        assertFalse(contenedorParcial.get("recibido").asBoolean(),
                "Contenedor no debe estar marcado como recibido tras recepcion parcial");

        // Verificar stock intermedio en bodega destino: debe ser 25
        mockMvc.perform(get("/api/v1/inventario/stock/producto-bodega")
                        .header("X-Empresa-Id", empresaId)
                        .param("productoId", "200")
                        .param("bodegaId", bodegaDestinoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos").value(closeTo(25.0, 0.01)));

        // 6. Recibir resto (sin cantidadRecibida = usa pendiente por defecto = 15)
        String recepcionRestoJson = String.format("""
                {
                    "ubicacionDestinoId": %d,
                    "contenedores": [{
                        "contenedorId": %d
                    }]
                }
                """, ubicacionDestinoId, contenedorId);

        MvcResult completoResult = mockMvc.perform(patch("/api/v1/transferencias/{id}/recibir", transferenciaId)
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recepcionRestoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.estadoCodigo").value("RECIBIDO_COMPLETO"))
                .andReturn();

        // Verificar que ahora SÍ fue marcado como recibido (25+15 = 40 = total)
        JsonNode completoNode = objectMapper.readTree(completoResult.getResponse().getContentAsString());
        JsonNode contenedorCompleto = completoNode.at("/datos/contenedores").get(0);
        assertTrue(contenedorCompleto.get("recibido").asBoolean(),
                "Contenedor debe estar marcado como recibido tras recepcion completa");

        // 7. Verificar stock final en bodega origen (100 - 40 = 60)
        mockMvc.perform(get("/api/v1/inventario/stock/producto-bodega")
                        .header("X-Empresa-Id", empresaId)
                        .param("productoId", "200")
                        .param("bodegaId", bodegaOrigenId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos").value(closeTo(60.0, 0.01)));

        // 8. Verificar stock final en bodega destino (25 + 15 = 40)
        mockMvc.perform(get("/api/v1/inventario/stock/producto-bodega")
                        .header("X-Empresa-Id", empresaId)
                        .param("productoId", "200")
                        .param("bodegaId", bodegaDestinoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos").value(closeTo(40.0, 0.01)));

        // 9. Verificar trazabilidad: kardex debe tener ENTRADA_TRANSFERENCIA en bodega destino
        MvcResult kardexResult = mockMvc.perform(get("/api/v1/inventario/kardex")
                        .header("X-Empresa-Id", empresaId)
                        .param("productoId", "200")
                        .param("bodegaId", bodegaDestinoId.toString())
                        .param("pagina", "0")
                        .param("tamanio", "50"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode kardex = objectMapper.readTree(kardexResult.getResponse().getContentAsString());
        JsonNode operaciones = kardex.at("/datos/contenido");
        assertTrue(operaciones.isArray() && operaciones.size() >= 2,
                "Debe haber al menos 2 operaciones ENTRADA_TRANSFERENCIA (recepciones parciales)");

        // Verificar que ambas operaciones son ENTRADA_TRANSFERENCIA
        int entradasTransferencia = 0;
        for (JsonNode op : operaciones) {
            if ("ENTRADA_TRANSFERENCIA".equals(op.get("tipoOperacionCodigo").asText())) {
                entradasTransferencia++;
            }
        }
        assertEquals(2, entradasTransferencia,
                "Debe haber exactamente 2 operaciones ENTRADA_TRANSFERENCIA (25 + 15)");
    }

    @Test
    void deberiaFallarDespachoSinStockSuficiente() throws Exception {
        String transferenciaJson = String.format("""
                {
                    "bodegaOrigenId": %d,
                    "bodegaDestinoId": %d,
                    "tipoTransferencia": "POR_PRODUCTO",
                    "lineas": [{
                        "productoId": 999,
                        "unidadId": %d,
                        "cantidadSolicitada": 100
                    }]
                }
                """, bodegaOrigenId, bodegaDestinoId, unidadId);

        MvcResult createResult = mockMvc.perform(post("/api/v1/transferencias")
                        .header("X-Empresa-Id", empresaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferenciaJson))
                .andExpect(status().isCreated())
                .andReturn();

        Long transferenciaId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .at("/datos/id").asLong();

        mockMvc.perform(patch("/api/v1/transferencias/{id}/confirmar", transferenciaId)
                        .header("X-Empresa-Id", empresaId))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/transferencias/{id}/despachar", transferenciaId)
                        .header("X-Empresa-Id", empresaId))
                .andExpect(status().is4xxClientError());
    }
}
