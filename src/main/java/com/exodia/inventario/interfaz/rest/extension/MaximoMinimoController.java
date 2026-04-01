package com.exodia.inventario.interfaz.rest.extension;

import com.exodia.inventario.aplicacion.comando.MaximoMinimoService;
import com.exodia.inventario.interfaz.dto.peticion.ActualizarMaximoMinimoRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearMaximoMinimoRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ApiResponse;
import com.exodia.inventario.interfaz.dto.respuesta.MaximoMinimoResponse;
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
@RequestMapping("/api/v1/maximos-minimos")
@RequiredArgsConstructor
@Tag(name = "Maximos y Minimos", description = "Configuracion de stock maximo y minimo por producto-bodega")
public class MaximoMinimoController {

    private final MaximoMinimoService maximoMinimoService;

    @PostMapping
    @Operation(summary = "Crear configuracion", description = "Crea regla de maximo/minimo para producto-bodega")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Configuracion creada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o ya existe"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Entidad no encontrada")
    })
    public ResponseEntity<ApiResponse<MaximoMinimoResponse>> crear(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @Valid @RequestBody CrearMaximoMinimoRequest request) {
        MaximoMinimoResponse response = maximoMinimoService.crear(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exitoso(response, "Configuracion de max/min creada"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener configuracion por ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Configuracion encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No encontrada")
    })
    public ResponseEntity<ApiResponse<MaximoMinimoResponse>> obtenerPorId(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        MaximoMinimoResponse response = maximoMinimoService.obtenerPorId(empresaId, id);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @GetMapping
    @Operation(summary = "Listar por bodega", description = "Lista configuraciones de max/min por bodega")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de configuraciones")
    })
    public ResponseEntity<ApiResponse<List<MaximoMinimoResponse>>> listarPorBodega(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @RequestParam Long bodegaId) {
        List<MaximoMinimoResponse> response = maximoMinimoService.listarPorBodega(empresaId, bodegaId);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar configuracion")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Configuracion actualizada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No encontrada")
    })
    public ResponseEntity<ApiResponse<MaximoMinimoResponse>> actualizar(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id,
            @Valid @RequestBody ActualizarMaximoMinimoRequest request) {
        MaximoMinimoResponse response = maximoMinimoService.actualizar(empresaId, id, request);
        return ResponseEntity.ok(ApiResponse.exitoso(response, "Configuracion actualizada"));
    }

    @DeleteMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar configuracion")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Configuracion desactivada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No encontrada")
    })
    public ResponseEntity<Void> desactivar(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        maximoMinimoService.desactivar(empresaId, id);
        return ResponseEntity.noContent().build();
    }
}
