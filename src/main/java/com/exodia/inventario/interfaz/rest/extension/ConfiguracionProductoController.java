package com.exodia.inventario.interfaz.rest.extension;

import com.exodia.inventario.aplicacion.comando.ConfiguracionProductoService;
import com.exodia.inventario.interfaz.dto.peticion.ActualizarConfiguracionProductoRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearConfiguracionProductoRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ApiResponse;
import com.exodia.inventario.interfaz.dto.respuesta.ConfiguracionProductoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/configuracion-producto")
@RequiredArgsConstructor
@Tag(name = "Configuracion Producto", description = "Parametros por producto por empresa")
public class ConfiguracionProductoController {

    private final ConfiguracionProductoService configuracionProductoService;

    @PostMapping
    @Operation(summary = "Crear configuracion de producto", description = "Crea configuracion para un producto en la empresa")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Configuracion creada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o ya existe"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Entidad no encontrada")
    })
    public ResponseEntity<ApiResponse<ConfiguracionProductoResponse>> crear(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @Valid @RequestBody CrearConfiguracionProductoRequest request) {
        ConfiguracionProductoResponse response = configuracionProductoService.crear(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exitoso(response, "Configuracion de producto creada"));
    }

    @GetMapping("/{productoId}")
    @Operation(summary = "Obtener configuracion por producto", description = "Obtiene o crea configuracion con defaults para un producto")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Configuracion obtenida")
    })
    public ResponseEntity<ApiResponse<ConfiguracionProductoResponse>> obtenerPorProducto(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long productoId) {
        ConfiguracionProductoResponse response = configuracionProductoService.obtenerOCrear(empresaId, productoId);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @GetMapping
    @Operation(summary = "Listar por empresa", description = "Lista configuraciones de producto activas de la empresa")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de configuraciones")
    })
    public ResponseEntity<ApiResponse<List<ConfiguracionProductoResponse>>> listarPorEmpresa(
            @RequestHeader("X-Empresa-Id") Long empresaId) {
        List<ConfiguracionProductoResponse> response = configuracionProductoService.listarPorEmpresa(empresaId);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @PatchMapping("/{productoId}")
    @Operation(summary = "Actualizar configuracion de producto")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Configuracion actualizada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No encontrada")
    })
    public ResponseEntity<ApiResponse<ConfiguracionProductoResponse>> actualizar(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long productoId,
            @Valid @RequestBody ActualizarConfiguracionProductoRequest request) {
        ConfiguracionProductoResponse response = configuracionProductoService.actualizar(
                empresaId, productoId, request);
        return ResponseEntity.ok(ApiResponse.exitoso(response, "Configuracion actualizada"));
    }
}
