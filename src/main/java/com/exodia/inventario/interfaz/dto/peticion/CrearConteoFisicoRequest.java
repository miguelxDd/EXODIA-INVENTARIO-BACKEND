package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Peticion para crear un conteo fisico")
public record CrearConteoFisicoRequest(
    @Schema(description = "ID de la bodega") @NotNull Long bodegaId,
    @Schema(description = "Comentarios") String comentarios
) {}
