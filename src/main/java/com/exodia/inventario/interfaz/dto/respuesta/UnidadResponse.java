package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de una unidad de medida")
public record UnidadResponse(
    @Schema(description = "ID de la unidad") Long id,
    @Schema(description = "Codigo de la unidad") String codigo,
    @Schema(description = "Nombre de la unidad") String nombre,
    @Schema(description = "Abreviatura") String abreviatura
) {}
