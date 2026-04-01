package com.exodia.inventario.interfaz.rest.catalogo;

import com.exodia.inventario.aplicacion.comando.BodegaService;
import com.exodia.inventario.interfaz.dto.peticion.ActualizarBodegaRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearBodegaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ApiResponse;
import com.exodia.inventario.interfaz.dto.respuesta.BodegaResponse;
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
@RequestMapping("/api/v1/bodegas")
@RequiredArgsConstructor
@Tag(name = "Bodegas", description = "CRUD de bodegas")
public class BodegaController {

    private final BodegaService bodegaService;

    @PostMapping
    @Operation(summary = "Crear bodega", description = "Crea una nueva bodega para la empresa")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Bodega creada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    public ResponseEntity<ApiResponse<BodegaResponse>> crear(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @Valid @RequestBody CrearBodegaRequest request) {
        BodegaResponse response = bodegaService.crear(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exitoso(response, "Bodega creada exitosamente"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener bodega por ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bodega encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bodega no encontrada")
    })
    public ResponseEntity<ApiResponse<BodegaResponse>> obtenerPorId(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        BodegaResponse response = bodegaService.obtenerPorId(empresaId, id);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @GetMapping
    @Operation(summary = "Listar bodegas por empresa")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de bodegas")
    })
    public ResponseEntity<ApiResponse<List<BodegaResponse>>> listarPorEmpresa(
            @RequestHeader("X-Empresa-Id") Long empresaId) {
        List<BodegaResponse> response = bodegaService.listarPorEmpresa(empresaId);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar bodega")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bodega actualizada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bodega no encontrada")
    })
    public ResponseEntity<ApiResponse<BodegaResponse>> actualizar(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id,
            @Valid @RequestBody ActualizarBodegaRequest request) {
        BodegaResponse response = bodegaService.actualizar(empresaId, id, request);
        return ResponseEntity.ok(ApiResponse.exitoso(response, "Bodega actualizada exitosamente"));
    }

    @DeleteMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar bodega", description = "Soft delete de la bodega")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Bodega desactivada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bodega no encontrada")
    })
    public ResponseEntity<Void> desactivar(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        bodegaService.desactivar(empresaId, id);
        return ResponseEntity.noContent().build();
    }
}
