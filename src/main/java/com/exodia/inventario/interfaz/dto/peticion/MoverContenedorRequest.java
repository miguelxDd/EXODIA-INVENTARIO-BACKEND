package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Peticion para mover un contenedor a otra ubicacion")
public record MoverContenedorRequest(
        @NotNull
        @Schema(description = "ID de la ubicacion destino")
        Long ubicacionDestinoId,

        @Size(max = 500)
        @Schema(description = "Comentarios del movimiento")
        String comentarios
) {}
