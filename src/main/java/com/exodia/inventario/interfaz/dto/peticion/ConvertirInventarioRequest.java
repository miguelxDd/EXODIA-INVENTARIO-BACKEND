package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Peticion para convertir inventario de una unidad a otra")
public record ConvertirInventarioRequest(
        @NotNull
        @Schema(description = "ID del contenedor origen")
        Long contenedorId,

        @NotNull
        @Schema(description = "ID de la unidad destino")
        Long unidadDestinoId,

        @NotNull
        @Positive
        @Schema(description = "Cantidad a convertir en la unidad origen")
        BigDecimal cantidadOrigen,

        @Size(max = 500)
        @Schema(description = "Comentarios de la conversion")
        String comentarios
) {}
