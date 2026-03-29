package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Peticion para crear configuracion de maximo/minimo")
public record CrearMaximoMinimoRequest(
    @Schema(description = "ID del producto") @NotNull Long productoId,
    @Schema(description = "ID de la bodega") @NotNull Long bodegaId,
    @Schema(description = "ID de la unidad") @NotNull Long unidadId,
    @Schema(description = "Stock minimo") @NotNull @Positive BigDecimal stockMinimo,
    @Schema(description = "Stock maximo") @NotNull @Positive BigDecimal stockMaximo,
    @Schema(description = "Punto de reorden") BigDecimal puntoReorden
) {}
