package com.exodia.inventario.interfaz.rest.transferencia;

import com.exodia.inventario.aplicacion.comando.TransferenciaService;
import com.exodia.inventario.interfaz.dto.peticion.CrearTransferenciaRequest;
import com.exodia.inventario.interfaz.dto.peticion.RecibirTransferenciaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ApiResponse;
import com.exodia.inventario.interfaz.dto.respuesta.PaginaResponse;
import com.exodia.inventario.interfaz.dto.respuesta.TransferenciaResponse;
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
@RequestMapping("/api/v1/transferencias")
@RequiredArgsConstructor
@Tag(name = "Transferencias", description = "Transferencias de inventario entre bodegas")
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    @PostMapping
    @Operation(summary = "Crear transferencia", description = "Crea una transferencia en estado BORRADOR")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Transferencia creada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Entidad no encontrada")
    })
    public ResponseEntity<ApiResponse<TransferenciaResponse>> crear(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @Valid @RequestBody CrearTransferenciaRequest request) {
        TransferenciaResponse response = transferenciaService.crear(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exitoso(response, "Transferencia creada exitosamente"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener transferencia por ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transferencia encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Transferencia no encontrada")
    })
    public ResponseEntity<ApiResponse<TransferenciaResponse>> obtenerPorId(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        TransferenciaResponse response = transferenciaService.obtenerPorId(empresaId, id);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @GetMapping
    @Operation(summary = "Listar transferencias", description = "Lista transferencias paginadas por empresa")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de transferencias")
    })
    public ResponseEntity<ApiResponse<PaginaResponse<TransferenciaResponse>>> listarPorEmpresa(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "" + TAMANIO_PAGINA_DEFAULT) int tamanio) {
        Pageable pageable = PageRequest.of(pagina, Math.min(tamanio, MAX_RESULTADOS_PAGINA));
        Page<TransferenciaResponse> page = transferenciaService.listarPorEmpresa(empresaId, pageable);
        return ResponseEntity.ok(ApiResponse.exitoso(PaginaResponse.de(page)));
    }

    @PatchMapping("/{id}/confirmar")
    @Operation(summary = "Confirmar transferencia", description = "Transicion BORRADOR -> CONFIRMADO")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transferencia confirmada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Transicion de estado invalida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Transferencia no encontrada")
    })
    public ResponseEntity<ApiResponse<TransferenciaResponse>> confirmar(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        TransferenciaResponse response = transferenciaService.confirmar(empresaId, id);
        return ResponseEntity.ok(ApiResponse.exitoso(response, "Transferencia confirmada"));
    }

    @PatchMapping("/{id}/despachar")
    @Operation(summary = "Despachar transferencia", description = "Transicion CONFIRMADO -> DESPACHADO -> EN_TRANSITO. Resuelve contenedores FEFO, crea operaciones de salida")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transferencia despachada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Transicion invalida o sin stock"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Stock insuficiente")
    })
    public ResponseEntity<ApiResponse<TransferenciaResponse>> despachar(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        TransferenciaResponse response = transferenciaService.despachar(empresaId, id);
        return ResponseEntity.ok(ApiResponse.exitoso(response, "Transferencia despachada"));
    }

    @PatchMapping("/{id}/recibir")
    @Operation(summary = "Recibir transferencia", description = "Transicion EN_TRANSITO -> RECIBIDO_PARCIAL o RECIBIDO_COMPLETO")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transferencia recibida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Transicion invalida o datos incorrectos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Contenedor no encontrado en transferencia")
    })
    public ResponseEntity<ApiResponse<TransferenciaResponse>> recibir(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id,
            @Valid @RequestBody RecibirTransferenciaRequest request) {
        TransferenciaResponse response = transferenciaService.recibir(empresaId, id, request);
        return ResponseEntity.ok(ApiResponse.exitoso(response, "Transferencia recibida"));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar transferencia", description = "Transicion BORRADOR/CONFIRMADO -> CANCELADO")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transferencia cancelada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "No se puede cancelar en este estado")
    })
    public ResponseEntity<ApiResponse<TransferenciaResponse>> cancelar(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        TransferenciaResponse response = transferenciaService.cancelar(empresaId, id);
        return ResponseEntity.ok(ApiResponse.exitoso(response, "Transferencia cancelada"));
    }
}
