package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Respuesta de conteo fisico")
public record ConteoFisicoResponse(
    @Schema(description = "ID") Long id,
    @Schema(description = "Numero de conteo") String numeroConteo,
    @Schema(description = "ID de la bodega") Long bodegaId,
    @Schema(description = "Estado") String estado,
    @Schema(description = "Fecha de conteo") OffsetDateTime fechaConteo,
    @Schema(description = "Comentarios") String comentarios,
    @Schema(description = "ID del ajuste generado") Long ajusteGeneradoId,
    @Schema(description = "Lineas") List<ConteoLineaResponse> lineas,
    @Schema(description = "Fecha de creacion") OffsetDateTime creadoEn
) {}
