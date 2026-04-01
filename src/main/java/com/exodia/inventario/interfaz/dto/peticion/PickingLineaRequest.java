package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Linea de una orden de picking")
public record PickingLineaRequest(
    @Schema(description = "ID del producto") @NotNull Long productoId,
    @Schema(description = "ID de la unidad") @NotNull Long unidadId,
    @Schema(description = "Cantidad solicitada") @NotNull @Positive BigDecimal cantidadSolicitada,
    @Schema(description = "ID del contenedor (requerido cuando politicaSalida = MANUAL)") Long contenedorId
) {}
