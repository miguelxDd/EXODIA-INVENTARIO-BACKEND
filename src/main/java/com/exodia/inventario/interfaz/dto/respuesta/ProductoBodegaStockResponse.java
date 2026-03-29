package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Stock agrupado por producto y bodega")
public record ProductoBodegaStockResponse(
    @Schema(description = "ID del producto") Long productoId,
    @Schema(description = "ID de la bodega") Long bodegaId,
    @Schema(description = "ID de la unidad") Long unidadId,
    @Schema(description = "Stock total") BigDecimal stockCantidad
) {}
