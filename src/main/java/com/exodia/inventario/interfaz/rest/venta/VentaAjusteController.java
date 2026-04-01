package com.exodia.inventario.interfaz.rest.venta;

import com.exodia.inventario.aplicacion.comando.VentaAjusteService;
import com.exodia.inventario.interfaz.dto.peticion.CrearAjusteVentaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.AjusteResponse;
import com.exodia.inventario.interfaz.dto.respuesta.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ventas-ajustes")
@RequiredArgsConstructor
@Tag(name = "Ventas Ajustes", description = "Salida de inventario por ventas facturadas")
public class VentaAjusteController {

    private final VentaAjusteService ventaAjusteService;

    @PostMapping
    @Operation(summary = "Ajustar inventario por venta facturada",
            description = "Descuenta inventario siguiendo la politica FEFO/FIFO/MANUAL configurada")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "Ajuste por venta creado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Datos invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Entidad no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422", description = "Stock insuficiente")
    })
    public ResponseEntity<ApiResponse<AjusteResponse>> crear(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @Valid @RequestBody CrearAjusteVentaRequest request) {
        AjusteResponse response = ventaAjusteService.crear(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exitoso(response, "Ajuste por venta facturada creado exitosamente"));
    }
}
