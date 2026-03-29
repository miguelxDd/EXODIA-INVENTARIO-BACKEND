package com.exodia.inventario.interfaz.rest.catalogo;

import com.exodia.inventario.aplicacion.comando.ConversionUnidadService;
import com.exodia.inventario.interfaz.dto.peticion.ActualizarConversionUnidadRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearConversionUnidadRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ApiResponse;
import com.exodia.inventario.interfaz.dto.respuesta.ConversionUnidadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/conversiones-unidad")
@RequiredArgsConstructor
@Tag(name = "Conversiones de Unidad", description = "CRUD de conversiones de unidad")
public class ConversionUnidadController {

    private final ConversionUnidadService conversionUnidadService;

    @PostMapping
    @Operation(summary = "Crear conversion de unidad")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Conversion creada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Unidad no encontrada")
    })
    public ResponseEntity<ApiResponse<ConversionUnidadResponse>> crear(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @Valid @RequestBody CrearConversionUnidadRequest request) {
        ConversionUnidadResponse response = conversionUnidadService.crear(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exitoso(response, "Conversion de unidad creada exitosamente"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener conversion por ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conversion encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversion no encontrada")
    })
    public ResponseEntity<ApiResponse<ConversionUnidadResponse>> obtenerPorId(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        ConversionUnidadResponse response = conversionUnidadService.obtenerPorId(empresaId, id);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @GetMapping
    @Operation(summary = "Listar conversiones por empresa")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de conversiones")
    })
    public ResponseEntity<ApiResponse<List<ConversionUnidadResponse>>> listarPorEmpresa(
            @RequestHeader("X-Empresa-Id") Long empresaId) {
        List<ConversionUnidadResponse> response = conversionUnidadService.listarPorEmpresa(empresaId);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar conversion de unidad")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conversion actualizada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversion no encontrada")
    })
    public ResponseEntity<ApiResponse<ConversionUnidadResponse>> actualizar(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id,
            @Valid @RequestBody ActualizarConversionUnidadRequest request) {
        ConversionUnidadResponse response = conversionUnidadService.actualizar(empresaId, id, request);
        return ResponseEntity.ok(ApiResponse.exitoso(response, "Conversion actualizada exitosamente"));
    }

    @DeleteMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar conversion", description = "Soft delete de la conversion de unidad")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Conversion desactivada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversion no encontrada")
    })
    public ResponseEntity<Void> desactivar(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        conversionUnidadService.desactivar(empresaId, id);
        return ResponseEntity.noContent().build();
    }
}
