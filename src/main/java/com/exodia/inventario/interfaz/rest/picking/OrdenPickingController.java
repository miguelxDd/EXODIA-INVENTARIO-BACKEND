package com.exodia.inventario.interfaz.rest.picking;

import com.exodia.inventario.aplicacion.comando.PickingService;
import com.exodia.inventario.interfaz.dto.peticion.CrearOrdenPickingRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ApiResponse;
import com.exodia.inventario.interfaz.dto.respuesta.OrdenPickingResponse;
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
@RequestMapping("/api/v1/picking")
@RequiredArgsConstructor
@Tag(name = "Picking", description = "Ordenes de picking de inventario")
public class OrdenPickingController {

    private final PickingService pickingService;

    @PostMapping
    @Operation(summary = "Crear orden de picking", description = "Crea una orden de picking en estado PENDIENTE")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Orden creada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Entidad no encontrada")
    })
    public ResponseEntity<ApiResponse<OrdenPickingResponse>> crear(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @Valid @RequestBody CrearOrdenPickingRequest request) {
        OrdenPickingResponse response = pickingService.crear(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exitoso(response, "Orden de picking creada exitosamente"));
    }

    @PatchMapping("/{id}/ejecutar")
    @Operation(summary = "Ejecutar picking", description = "Ejecuta el picking segun la politica configurada (FEFO/FIFO/MANUAL)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Picking ejecutado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Estado invalido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Stock insuficiente")
    })
    public ResponseEntity<ApiResponse<OrdenPickingResponse>> ejecutar(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        OrdenPickingResponse response = pickingService.ejecutar(empresaId, id);
        return ResponseEntity.ok(ApiResponse.exitoso(response, "Picking ejecutado exitosamente"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener orden de picking por ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Orden encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    public ResponseEntity<ApiResponse<OrdenPickingResponse>> obtenerPorId(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        OrdenPickingResponse response = pickingService.obtenerPorId(empresaId, id);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @GetMapping
    @Operation(summary = "Listar ordenes de picking", description = "Lista ordenes de picking paginadas por empresa")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de ordenes")
    })
    public ResponseEntity<ApiResponse<PaginaResponse<OrdenPickingResponse>>> listarPorEmpresa(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "" + TAMANIO_PAGINA_DEFAULT) int tamanio) {
        Pageable pageable = PageRequest.of(pagina, Math.min(tamanio, MAX_RESULTADOS_PAGINA));
        Page<OrdenPickingResponse> page = pickingService.listarPorEmpresa(empresaId, pageable);
        return ResponseEntity.ok(ApiResponse.exitoso(PaginaResponse.de(page)));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar orden de picking", description = "Cancela una orden en estado PENDIENTE")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Orden cancelada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "No se puede cancelar")
    })
    public ResponseEntity<ApiResponse<OrdenPickingResponse>> cancelar(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        OrdenPickingResponse response = pickingService.cancelar(empresaId, id);
        return ResponseEntity.ok(ApiResponse.exitoso(response, "Orden de picking cancelada"));
    }
}
