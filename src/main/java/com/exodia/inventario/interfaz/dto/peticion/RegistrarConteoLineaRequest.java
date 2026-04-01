package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

@Schema(description = "Peticion para registrar el conteo de un contenedor")
public record RegistrarConteoLineaRequest(
    @Schema(description = "ID del contenedor") @NotNull Long contenedorId,
    @Schema(description = "Cantidad contada fisicamente") @NotNull @PositiveOrZero BigDecimal cantidadContada
) {}
