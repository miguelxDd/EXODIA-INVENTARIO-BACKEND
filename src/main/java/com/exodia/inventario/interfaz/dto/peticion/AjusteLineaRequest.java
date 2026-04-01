package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Linea de ajuste de inventario")
public record AjusteLineaRequest(
    @NotNull @Schema(description = "ID del contenedor a ajustar") Long contenedorId,
    @Schema(description = "Cantidad nueva (para ajuste de cantidad). Si es null, no se ajusta cantidad.") BigDecimal cantidadNueva,
    @Schema(description = "Precio nuevo (para ajuste de precio). Si es null, no se ajusta precio.") BigDecimal precioNuevo
) {}
