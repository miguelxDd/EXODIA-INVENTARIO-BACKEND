package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Linea de una transferencia")
public record TransferenciaLineaResponse(
    @Schema(description = "ID de la linea") Long id,
    @Schema(description = "ID del producto") Long productoId,
    @Schema(description = "ID de la unidad") Long unidadId,
    @Schema(description = "Cantidad solicitada") BigDecimal cantidadSolicitada,
    @Schema(description = "Cantidad despachada") BigDecimal cantidadDespachada,
    @Schema(description = "Cantidad recibida") BigDecimal cantidadRecibida
) {}
