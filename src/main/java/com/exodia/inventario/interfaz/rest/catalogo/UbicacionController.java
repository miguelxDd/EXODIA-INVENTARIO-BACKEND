package com.exodia.inventario.interfaz.rest.catalogo;

import com.exodia.inventario.aplicacion.comando.UbicacionService;
import com.exodia.inventario.interfaz.dto.peticion.ActualizarUbicacionRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearUbicacionRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ApiResponse;
import com.exodia.inventario.interfaz.dto.respuesta.UbicacionResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ubicaciones")
@RequiredArgsConstructor
@Tag(name = "Ubicaciones", description = "CRUD de ubicaciones")
public class UbicacionController {

    private final UbicacionService ubicacionService;

    @PostMapping
    @Operation(summary = "Crear ubicacion", description = "Crea una nueva ubicacion en una bodega")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Ubicacion creada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bodega no encontrada")
    })
    public ResponseEntity<ApiResponse<UbicacionResponse>> crear(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @Valid @RequestBody CrearUbicacionRequest request) {
        UbicacionResponse response = ubicacionService.crear(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exitoso(response, "Ubicacion creada exitosamente"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ubicacion por ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ubicacion encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ubicacion no encontrada")
    })
    public ResponseEntity<ApiResponse<UbicacionResponse>> obtenerPorId(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        UbicacionResponse response = ubicacionService.obtenerPorId(empresaId, id);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @GetMapping
    @Operation(summary = "Listar ubicaciones por bodega")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de ubicaciones")
    })
    public ResponseEntity<ApiResponse<List<UbicacionResponse>>> listarPorBodega(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @RequestParam Long bodegaId) {
        List<UbicacionResponse> response = ubicacionService.listarPorBodega(empresaId, bodegaId);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar ubicacion")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ubicacion actualizada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ubicacion no encontrada")
    })
    public ResponseEntity<ApiResponse<UbicacionResponse>> actualizar(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUbicacionRequest request) {
        UbicacionResponse response = ubicacionService.actualizar(empresaId, id, request);
        return ResponseEntity.ok(ApiResponse.exitoso(response, "Ubicacion actualizada exitosamente"));
    }

    @DeleteMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar ubicacion", description = "Soft delete de la ubicacion")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Ubicacion desactivada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ubicacion no encontrada")
    })
    public ResponseEntity<Void> desactivar(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        ubicacionService.desactivar(empresaId, id);
        return ResponseEntity.noContent().build();
    }
}
