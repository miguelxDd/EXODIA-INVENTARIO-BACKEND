package com.exodia.inventario.interfaz.rest.reportes;

import com.exodia.inventario.aplicacion.consulta.EtiquetaInventarioService;
import com.exodia.inventario.interfaz.dto.respuesta.ApiResponse;
import com.exodia.inventario.interfaz.dto.respuesta.EtiquetaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/etiquetas")
@RequiredArgsConstructor
@Tag(name = "Etiquetas", description = "Reimpresion operativa de etiquetas de inventario")
public class EtiquetaController {

    private final EtiquetaInventarioService etiquetaInventarioService;

    @GetMapping("/contenedores/{contenedorId}")
    @Operation(summary = "Generar etiqueta de contenedor")
    public ResponseEntity<ApiResponse<EtiquetaResponse>> etiquetaContenedor(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long contenedorId) {
        EtiquetaResponse response = etiquetaInventarioService
                .generarEtiquetaContenedor(empresaId, contenedorId);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @GetMapping(value = "/contenedores/{contenedorId}/zpl", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Exportar etiqueta de contenedor en ZPL")
    public ResponseEntity<String> etiquetaContenedorZpl(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long contenedorId) {
        EtiquetaResponse response = etiquetaInventarioService
                .generarEtiquetaContenedor(empresaId, contenedorId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=contenedor-" + contenedorId + ".zpl")
                .contentType(MediaType.TEXT_PLAIN)
                .body(response.zpl());
    }

    @GetMapping(value = "/contenedores/{contenedorId}/svg", produces = "image/svg+xml")
    @Operation(summary = "Exportar vista previa SVG de etiqueta de contenedor")
    public ResponseEntity<String> etiquetaContenedorSvg(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long contenedorId) {
        EtiquetaResponse response = etiquetaInventarioService
                .generarEtiquetaContenedor(empresaId, contenedorId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("image/svg+xml"))
                .body(response.svgVistaPrevia());
    }

    @GetMapping("/ubicaciones/{ubicacionId}")
    @Operation(summary = "Generar etiqueta de ubicacion")
    public ResponseEntity<ApiResponse<EtiquetaResponse>> etiquetaUbicacion(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long ubicacionId) {
        EtiquetaResponse response = etiquetaInventarioService
                .generarEtiquetaUbicacion(empresaId, ubicacionId);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @GetMapping(value = "/ubicaciones/{ubicacionId}/zpl", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Exportar etiqueta de ubicacion en ZPL")
    public ResponseEntity<String> etiquetaUbicacionZpl(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long ubicacionId) {
        EtiquetaResponse response = etiquetaInventarioService
                .generarEtiquetaUbicacion(empresaId, ubicacionId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=ubicacion-" + ubicacionId + ".zpl")
                .contentType(MediaType.TEXT_PLAIN)
                .body(response.zpl());
    }

    @GetMapping(value = "/ubicaciones/{ubicacionId}/svg", produces = "image/svg+xml")
    @Operation(summary = "Exportar vista previa SVG de etiqueta de ubicacion")
    public ResponseEntity<String> etiquetaUbicacionSvg(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long ubicacionId) {
        EtiquetaResponse response = etiquetaInventarioService
                .generarEtiquetaUbicacion(empresaId, ubicacionId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("image/svg+xml"))
                .body(response.svgVistaPrevia());
    }
}
