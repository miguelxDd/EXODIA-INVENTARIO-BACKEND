package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Asignacion de contenedor dentro de una linea de picking")
public record PickingLineaAsignacionResponse(
    @Schema(description = "ID de la asignacion") Long id,
    @Schema(description = "ID del contenedor") Long contenedorId,
    @Schema(description = "Codigo de barras del contenedor") String codigoBarras,
    @Schema(description = "Cantidad pickeada en esta asignacion") BigDecimal cantidadPickeada,
    @Schema(description = "ID de la operacion generada") Long operacionId
) {}
