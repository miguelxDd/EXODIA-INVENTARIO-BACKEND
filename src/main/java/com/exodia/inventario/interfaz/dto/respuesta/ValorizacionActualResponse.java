package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Fila del reporte de valorizacion actual")
public record ValorizacionActualResponse(
    @Schema(description = "ID del producto") Long productoId,
    @Schema(description = "ID de la bodega") Long bodegaId,
    @Schema(description = "ID de la unidad") Long unidadId,
    @Schema(description = "Cantidad actual en stock") BigDecimal cantidadStock,
    @Schema(description = "Costo unitario promedio ponderado") BigDecimal costoUnitario,
    @Schema(description = "Costo total valorizado") BigDecimal costoTotal
) {}
