package com.exodia.inventario.interfaz.rest.reserva;

import com.exodia.inventario.aplicacion.comando.ReservaService;
import com.exodia.inventario.interfaz.dto.peticion.CrearReservaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ApiResponse;
import com.exodia.inventario.interfaz.dto.respuesta.ReservaResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "Reservas de stock sobre contenedores")
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping
    @Operation(summary = "Crear reserva", description = "Crea una reserva de stock sobre un contenedor")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Reserva creada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Stock insuficiente o datos invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Contenedor no encontrado")
    })
    public ResponseEntity<ApiResponse<ReservaResponse>> crear(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @Valid @RequestBody CrearReservaRequest request) {
        ReservaResponse response = reservaService.crear(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exitoso(response, "Reserva creada exitosamente"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener reserva por ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reserva encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    public ResponseEntity<ApiResponse<ReservaResponse>> obtenerPorId(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        ReservaResponse response = reservaService.obtenerPorId(empresaId, id);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @GetMapping("/contenedor/{contenedorId}")
    @Operation(summary = "Listar reservas por contenedor", description = "Reservas activas (PENDIENTE/PARCIAL) de un contenedor")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de reservas")
    })
    public ResponseEntity<ApiResponse<List<ReservaResponse>>> listarPorContenedor(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long contenedorId) {
        List<ReservaResponse> response = reservaService.listarPorContenedor(empresaId, contenedorId);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar reserva", description = "Cancela una reserva activa")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reserva cancelada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "No se puede cancelar")
    })
    public ResponseEntity<ApiResponse<ReservaResponse>> cancelar(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        ReservaResponse response = reservaService.cancelar(empresaId, id);
        return ResponseEntity.ok(ApiResponse.exitoso(response, "Reserva cancelada"));
    }
}
