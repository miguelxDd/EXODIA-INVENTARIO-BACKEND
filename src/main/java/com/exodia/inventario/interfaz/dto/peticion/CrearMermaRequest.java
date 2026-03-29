package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Peticion para registrar una merma manual")
public record CrearMermaRequest(
    @Schema(description = "ID del contenedor") @NotNull Long contenedorId,
    @Schema(description = "Cantidad de merma") @NotNull @Positive BigDecimal cantidadMerma,
    @Schema(description = "Comentarios") String comentarios
) {}
