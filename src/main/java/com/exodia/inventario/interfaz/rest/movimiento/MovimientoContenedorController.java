package com.exodia.inventario.interfaz.rest.movimiento;

import com.exodia.inventario.aplicacion.comando.MovimientoContenedorService;
import com.exodia.inventario.interfaz.dto.peticion.MoverContenedorRequest;
import com.exodia.inventario.interfaz.dto.peticion.OperacionContenedorRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ApiResponse;
import com.exodia.inventario.interfaz.dto.respuesta.MovimientoContenedorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/movimientos/contenedores")
@RequiredArgsConstructor
@Tag(name = "Movimientos", description = "Movimientos internos de contenedores")
public class MovimientoContenedorController {

    private final MovimientoContenedorService movimientoContenedorService;

    @PostMapping("/{contenedorId}/mover")
    @Operation(summary = "Mover contenedor", description = "Mueve un contenedor a otra ubicacion dentro de la misma bodega")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Contenedor movido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Movimiento invalido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Contenedor o ubicacion no encontrados")
    })
    public ResponseEntity<ApiResponse<MovimientoContenedorResponse>> mover(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long contenedorId,
            @Valid @RequestBody MoverContenedorRequest request) {
        MovimientoContenedorResponse response = movimientoContenedorService.mover(empresaId, contenedorId, request);
        return ResponseEntity.ok(ApiResponse.exitoso(response, "Contenedor movido exitosamente"));
    }

    @PostMapping("/{contenedorId}/enviar-standby")
    @Operation(summary = "Enviar a standby", description = "Mueve un contenedor a la ubicacion standby configurada en su bodega")
    public ResponseEntity<ApiResponse<MovimientoContenedorResponse>> enviarAStandby(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long contenedorId,
            @Valid @RequestBody(required = false) OperacionContenedorRequest request) {
        OperacionContenedorRequest payload = request != null ? request : new OperacionContenedorRequest(null);
        MovimientoContenedorResponse response = movimientoContenedorService.enviarAStandby(
                empresaId, contenedorId, payload);
        return ResponseEntity.ok(ApiResponse.exitoso(response, "Contenedor enviado a standby"));
    }

    @PostMapping("/{contenedorId}/sacar-standby")
    @Operation(summary = "Sacar de standby", description = "Saca un contenedor desde standby hacia una ubicacion operativa")
    public ResponseEntity<ApiResponse<MovimientoContenedorResponse>> sacarDeStandby(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long contenedorId,
            @Valid @RequestBody MoverContenedorRequest request) {
        MovimientoContenedorResponse response = movimientoContenedorService.sacarDeStandby(
                empresaId, contenedorId, request);
        return ResponseEntity.ok(ApiResponse.exitoso(response, "Contenedor retirado de standby"));
    }
}
