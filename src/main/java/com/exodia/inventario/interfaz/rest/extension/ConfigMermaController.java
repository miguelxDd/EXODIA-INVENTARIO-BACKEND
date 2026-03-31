package com.exodia.inventario.interfaz.rest.extension;

import com.exodia.inventario.aplicacion.comando.ConfigMermaService;
import com.exodia.inventario.interfaz.dto.peticion.ActualizarConfigMermaRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearConfigMermaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ApiResponse;
import com.exodia.inventario.interfaz.dto.respuesta.ConfigMermaResponse;
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
@RequestMapping("/api/v1/config-merma")
@RequiredArgsConstructor
@Tag(name = "Configuracion Merma", description = "Configuracion de merma por empresa/producto/bodega")
public class ConfigMermaController {

    private final ConfigMermaService configMermaService;

    @PostMapping
    @Operation(summary = "Crear configuracion de merma", description = "Crea regla de merma para empresa/producto/bodega")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Configuracion creada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o ya existe"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Entidad no encontrada")
    })
    public ResponseEntity<ApiResponse<ConfigMermaResponse>> crear(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @Valid @RequestBody CrearConfigMermaRequest request) {
        ConfigMermaResponse response = configMermaService.crear(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exitoso(response, "Configuracion de merma creada"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener configuracion por ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Configuracion encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No encontrada")
    })
    public ResponseEntity<ApiResponse<ConfigMermaResponse>> obtenerPorId(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        ConfigMermaResponse response = configMermaService.obtenerPorId(empresaId, id);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @GetMapping
    @Operation(summary = "Listar por empresa", description = "Lista configuraciones de merma activas de la empresa")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de configuraciones")
    })
    public ResponseEntity<ApiResponse<List<ConfigMermaResponse>>> listarPorEmpresa(
            @RequestHeader("X-Empresa-Id") Long empresaId) {
        List<ConfigMermaResponse> response = configMermaService.listarPorEmpresa(empresaId);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar configuracion")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Configuracion actualizada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No encontrada")
    })
    public ResponseEntity<ApiResponse<ConfigMermaResponse>> actualizar(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id,
            @Valid @RequestBody ActualizarConfigMermaRequest request) {
        ConfigMermaResponse response = configMermaService.actualizar(empresaId, id, request);
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
        configMermaService.desactivar(empresaId, id);
        return ResponseEntity.noContent().build();
    }
}
