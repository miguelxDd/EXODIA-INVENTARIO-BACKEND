package com.exodia.inventario.interfaz.rest.conteo;

import com.exodia.inventario.aplicacion.comando.ConteoFisicoService;
import com.exodia.inventario.interfaz.dto.peticion.CrearConteoFisicoRequest;
import com.exodia.inventario.interfaz.dto.peticion.RegistrarConteoLineaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ApiResponse;
import com.exodia.inventario.interfaz.dto.respuesta.ConteoFisicoResponse;
import com.exodia.inventario.interfaz.dto.respuesta.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.exodia.inventario.util.InventarioConstantes.MAX_RESULTADOS_PAGINA;
import static com.exodia.inventario.util.InventarioConstantes.TAMANIO_PAGINA_DEFAULT;

@RestController
@RequestMapping("/api/v1/conteos")
@RequiredArgsConstructor
@Tag(name = "Conteo Fisico", description = "Conteos fisicos de inventario")
public class ConteoFisicoController {

    private final ConteoFisicoService conteoFisicoService;

    @PostMapping
    @Operation(summary = "Crear conteo fisico", description = "Crea un conteo fisico en estado EN_PROGRESO")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Conteo creado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Entidad no encontrada")
    })
    public ResponseEntity<ApiResponse<ConteoFisicoResponse>> crear(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @Valid @RequestBody CrearConteoFisicoRequest request) {
        ConteoFisicoResponse response = conteoFisicoService.crear(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exitoso(response, "Conteo fisico creado exitosamente"));
    }

    @PostMapping("/{id}/lineas")
    @Operation(summary = "Registrar linea de conteo", description = "Registra la cantidad contada para un contenedor")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Linea registrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o contenedor ya registrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conteo o contenedor no encontrado")
    })
    public ResponseEntity<ApiResponse<ConteoFisicoResponse>> registrarLinea(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id,
            @Valid @RequestBody RegistrarConteoLineaRequest request) {
        ConteoFisicoResponse response = conteoFisicoService.registrarLinea(empresaId, id, request);
        return ResponseEntity.ok(ApiResponse.exitoso(response, "Linea de conteo registrada"));
    }

    @PatchMapping("/{id}/aplicar")
    @Operation(summary = "Aplicar conteo fisico", description = "Genera ajustes automaticos a partir de las diferencias")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conteo aplicado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Estado invalido o sin lineas")
    })
    public ResponseEntity<ApiResponse<ConteoFisicoResponse>> aplicar(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        ConteoFisicoResponse response = conteoFisicoService.aplicar(empresaId, id);
        return ResponseEntity.ok(ApiResponse.exitoso(response, "Conteo fisico aplicado exitosamente"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener conteo fisico por ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conteo encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conteo no encontrado")
    })
    public ResponseEntity<ApiResponse<ConteoFisicoResponse>> obtenerPorId(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        ConteoFisicoResponse response = conteoFisicoService.obtenerPorId(empresaId, id);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @GetMapping
    @Operation(summary = "Listar conteos fisicos", description = "Lista conteos fisicos paginados por empresa")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de conteos")
    })
    public ResponseEntity<ApiResponse<PaginaResponse<ConteoFisicoResponse>>> listarPorEmpresa(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "" + TAMANIO_PAGINA_DEFAULT) int tamanio) {
        Pageable pageable = PageRequest.of(pagina, Math.min(tamanio, MAX_RESULTADOS_PAGINA));
        Page<ConteoFisicoResponse> page = conteoFisicoService.listarPorEmpresa(empresaId, pageable);
        return ResponseEntity.ok(ApiResponse.exitoso(PaginaResponse.de(page)));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar conteo fisico", description = "Cancela un conteo en estado EN_PROGRESO")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conteo cancelado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "No se puede cancelar")
    })
    public ResponseEntity<ApiResponse<ConteoFisicoResponse>> cancelar(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        ConteoFisicoResponse response = conteoFisicoService.cancelar(empresaId, id);
        return ResponseEntity.ok(ApiResponse.exitoso(response, "Conteo fisico cancelado"));
    }
}
