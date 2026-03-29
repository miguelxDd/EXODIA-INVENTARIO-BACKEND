package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Peticion para actualizar configuracion de maximo/minimo")
public record ActualizarMaximoMinimoRequest(
    @Schema(description = "Stock minimo") BigDecimal stockMinimo,
    @Schema(description = "Stock maximo") BigDecimal stockMaximo,
    @Schema(description = "Punto de reorden") BigDecimal puntoReorden
) {}
