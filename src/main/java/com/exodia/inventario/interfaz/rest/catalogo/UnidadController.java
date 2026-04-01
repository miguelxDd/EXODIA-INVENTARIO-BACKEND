package com.exodia.inventario.interfaz.rest.catalogo;

import com.exodia.inventario.aplicacion.comando.UnidadService;
import com.exodia.inventario.interfaz.dto.peticion.ActualizarUnidadRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearUnidadRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ApiResponse;
import com.exodia.inventario.interfaz.dto.respuesta.UnidadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/unidades")
@RequiredArgsConstructor
@Tag(name = "Unidades", description = "CRUD de unidades de medida")
public class UnidadController {

    private final UnidadService unidadService;

    @PostMapping
    @Operation(summary = "Crear unidad de medida")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Unidad creada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    public ResponseEntity<ApiResponse<UnidadResponse>> crear(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @Valid @RequestBody CrearUnidadRequest request) {
        UnidadResponse response = unidadService.crear(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exitoso(response, "Unidad creada exitosamente"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener unidad por ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Unidad encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Unidad no encontrada")
    })
    public ResponseEntity<ApiResponse<UnidadResponse>> obtenerPorId(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        UnidadResponse response = unidadService.obtenerPorId(empresaId, id);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @GetMapping
    @Operation(summary = "Listar unidades por empresa")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de unidades")
    })
    public ResponseEntity<ApiResponse<List<UnidadResponse>>> listarPorEmpresa(
            @RequestHeader("X-Empresa-Id") Long empresaId) {
        List<UnidadResponse> response = unidadService.listarPorEmpresa(empresaId);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar unidad")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Unidad actualizada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Unidad no encontrada")
    })
    public ResponseEntity<ApiResponse<UnidadResponse>> actualizar(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUnidadRequest request) {
        UnidadResponse response = unidadService.actualizar(empresaId, id, request);
        return ResponseEntity.ok(ApiResponse.exitoso(response, "Unidad actualizada exitosamente"));
    }

    @DeleteMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar unidad", description = "Soft delete de la unidad")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Unidad desactivada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Unidad no encontrada")
    })
    public ResponseEntity<Void> desactivar(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        unidadService.desactivar(empresaId, id);
        return ResponseEntity.noContent().build();
    }
}
