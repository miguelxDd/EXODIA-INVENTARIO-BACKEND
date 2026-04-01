package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de una ubicacion")
public record UbicacionResponse(
    @Schema(description = "ID de la ubicacion") Long id,
    @Schema(description = "ID de la bodega") Long bodegaId,
    @Schema(description = "Codigo de la ubicacion") String codigo,
    @Schema(description = "Nombre de la ubicacion") String nombre,
    @Schema(description = "Codigo de barras") String codigoBarras,
    @Schema(description = "Tipo de ubicacion") String tipoUbicacion
) {}
