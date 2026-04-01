package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Respuesta de foto de costo")
public record FotoCostoResponse(
    @Schema(description = "ID") Long id,
    @Schema(description = "ID del producto") Long productoId,
    @Schema(description = "ID de la bodega") Long bodegaId,
    @Schema(description = "ID de la unidad") Long unidadId,
    @Schema(description = "Cantidad en stock") BigDecimal cantidadStock,
    @Schema(description = "Costo unitario") BigDecimal costoUnitario,
    @Schema(description = "Costo total") BigDecimal costoTotal,
    @Schema(description = "Metodo de costo") String metodoCosto,
    @Schema(description = "Fecha de la foto") OffsetDateTime fechaFoto
) {}
