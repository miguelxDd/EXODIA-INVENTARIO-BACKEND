package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Linea de una orden de picking")
public record PickingLineaResponse(
    @Schema(description = "ID") Long id,
    @Schema(description = "ID del producto") Long productoId,
    @Schema(description = "ID de la unidad") Long unidadId,
    @Schema(description = "Cantidad solicitada") BigDecimal cantidadSolicitada,
    @Schema(description = "Cantidad pickeada") BigDecimal cantidadPickeada,
    @Schema(description = "ID del contenedor asignado") Long contenedorId,
    @Schema(description = "ID de la operacion") Long operacionId
) {}
