package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Contenedor asignado a una transferencia")
public record TransferenciaContenedorResponse(
    @Schema(description = "ID") Long id,
    @Schema(description = "ID del contenedor") Long contenedorId,
    @Schema(description = "Codigo de barras") String codigoBarras,
    @Schema(description = "Cantidad asignada") BigDecimal cantidad,
    @Schema(description = "Fue recibido") Boolean recibido
) {}
