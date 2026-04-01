package com.exodia.inventario.interfaz.rest.reportes;

import com.exodia.inventario.aplicacion.consulta.ReporteInventarioService;
import com.exodia.inventario.interfaz.dto.respuesta.ApiResponse;
import com.exodia.inventario.interfaz.dto.respuesta.AuxiliarInventarioResponse;
import com.exodia.inventario.interfaz.dto.respuesta.ValorizacionActualResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Reportes livianos de inventario")
public class ReporteInventarioController {

    private static final MediaType TEXT_CSV = MediaType.parseMediaType("text/csv");

    private final ReporteInventarioService reporteInventarioService;

    @GetMapping("/auxiliar-inventario")
    @Operation(summary = "Auxiliar de inventario",
            description = "Reconstruye un kardex valorizado por producto en orden cronologico")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Auxiliar generado")
    })
    public ResponseEntity<ApiResponse<AuxiliarInventarioResponse>> auxiliarInventario(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @RequestParam Long productoId,
            @RequestParam(required = false) Long bodegaId,
            @RequestParam(required = false) OffsetDateTime fechaDesde,
            @RequestParam(required = false) OffsetDateTime fechaHasta) {
        AuxiliarInventarioResponse response = reporteInventarioService.generarAuxiliarInventario(
                empresaId, productoId, bodegaId, fechaDesde, fechaHasta);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @GetMapping(value = "/auxiliar-inventario/exportar-csv", produces = "text/csv")
    @Operation(summary = "Exportar auxiliar de inventario CSV")
    public ResponseEntity<String> exportarAuxiliarInventarioCsv(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @RequestParam Long productoId,
            @RequestParam(required = false) Long bodegaId,
            @RequestParam(required = false) OffsetDateTime fechaDesde,
            @RequestParam(required = false) OffsetDateTime fechaHasta) {
        String csv = reporteInventarioService.exportarAuxiliarInventarioCsv(
                empresaId, productoId, bodegaId, fechaDesde, fechaHasta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=auxiliar-inventario.csv")
                .contentType(TEXT_CSV)
                .body(csv);
    }

    @GetMapping("/valorizacion-actual")
    @Operation(summary = "Valorizacion actual",
            description = "Valorizacion operativa actual por producto y bodega sin persistir una foto")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Valorizacion actual")
    })
    public ResponseEntity<ApiResponse<List<ValorizacionActualResponse>>> valorizacionActual(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @RequestParam(required = false) Long bodegaId,
            @RequestParam(required = false) Long productoId) {
        List<ValorizacionActualResponse> response = reporteInventarioService
                .obtenerValorizacionActual(empresaId, bodegaId, productoId);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @GetMapping(value = "/valorizacion-actual/exportar-csv", produces = "text/csv")
    @Operation(summary = "Exportar valorizacion actual CSV")
    public ResponseEntity<String> exportarValorizacionActualCsv(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @RequestParam(required = false) Long bodegaId,
            @RequestParam(required = false) Long productoId) {
        String csv = reporteInventarioService.exportarValorizacionActualCsv(
                empresaId, bodegaId, productoId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=valorizacion-actual.csv")
                .contentType(TEXT_CSV)
                .body(csv);
    }
}
