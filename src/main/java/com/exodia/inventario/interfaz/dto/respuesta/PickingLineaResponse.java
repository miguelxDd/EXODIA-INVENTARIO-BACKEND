package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Linea de una orden de picking")
public record PickingLineaResponse(
    @Schema(description = "ID") Long id,
    @Schema(description = "ID del producto") Long productoId,
    @Schema(description = "ID de la unidad") Long unidadId,
    @Schema(description = "Cantidad solicitada") BigDecimal cantidadSolicitada,
    @Schema(description = "Cantidad pickeada") BigDecimal cantidadPickeada,
    @Schema(description = "ID del contenedor solicitado manualmente") Long contenedorSolicitadoId,
    @Schema(description = "ID del contenedor asignado") Long contenedorId,
    @Schema(description = "ID de la operacion") Long operacionId,
    @Schema(description = "Asignaciones reales de la linea") List<PickingLineaAsignacionResponse> asignaciones
) {}
