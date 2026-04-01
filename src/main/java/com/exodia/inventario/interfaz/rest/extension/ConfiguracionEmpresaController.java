package com.exodia.inventario.interfaz.rest.extension;

import com.exodia.inventario.aplicacion.comando.ConfiguracionEmpresaService;
import com.exodia.inventario.interfaz.dto.peticion.ActualizarConfiguracionEmpresaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ApiResponse;
import com.exodia.inventario.interfaz.dto.respuesta.ConfiguracionEmpresaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/configuracion-empresa")
@RequiredArgsConstructor
@Tag(name = "Configuracion Empresa", description = "Parametros globales de negocio por empresa")
public class ConfiguracionEmpresaController {

    private final ConfiguracionEmpresaService configuracionEmpresaService;

    @GetMapping
    @Operation(summary = "Obtener configuracion", description = "Obtiene la configuracion de la empresa. Si no existe, crea una con valores por defecto.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Configuracion obtenida")
    })
    public ResponseEntity<ApiResponse<ConfiguracionEmpresaResponse>> obtener(
            @RequestHeader("X-Empresa-Id") Long empresaId) {
        ConfiguracionEmpresaResponse response = configuracionEmpresaService.obtenerOCrear(empresaId);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @PatchMapping
    @Operation(summary = "Actualizar configuracion", description = "Actualiza parcialmente la configuracion de la empresa")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Configuracion actualizada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos")
    })
    public ResponseEntity<ApiResponse<ConfiguracionEmpresaResponse>> actualizar(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @Valid @RequestBody ActualizarConfiguracionEmpresaRequest request) {
        ConfiguracionEmpresaResponse response = configuracionEmpresaService.actualizar(empresaId, request);
        return ResponseEntity.ok(ApiResponse.exitoso(response, "Configuracion actualizada"));
    }
}
