package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Respuesta de orden de picking")
public record OrdenPickingResponse(
    @Schema(description = "ID") Long id,
    @Schema(description = "Numero de orden") String numeroOrden,
    @Schema(description = "ID de la bodega") Long bodegaId,
    @Schema(description = "Tipo de picking") String tipoPicking,
    @Schema(description = "Tipo de referencia") String tipoReferencia,
    @Schema(description = "ID de referencia") Long referenciaId,
    @Schema(description = "Estado") String estado,
    @Schema(description = "Comentarios") String comentarios,
    @Schema(description = "Lineas") List<PickingLineaResponse> lineas,
    @Schema(description = "Fecha de creacion") OffsetDateTime creadoEn
) {}
