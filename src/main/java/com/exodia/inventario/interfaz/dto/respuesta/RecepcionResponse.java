package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Datos de una recepcion")
public record RecepcionResponse(
    @Schema(description = "ID de la recepcion") Long id,
    @Schema(description = "Numero de recepcion") String numeroRecepcion,
    @Schema(description = "ID de la bodega") Long bodegaId,
    @Schema(description = "Tipo de recepcion") String tipoRecepcion,
    @Schema(description = "ID de la referencia de origen") Long referenciaOrigenId,
    @Schema(description = "ID del proveedor") Long proveedorId,
    @Schema(description = "Estado de la recepcion") String estado,
    @Schema(description = "Comentarios") String comentarios,
    @Schema(description = "Lineas de la recepcion") List<RecepcionLineaResponse> lineas,
    @Schema(description = "Fecha de creacion") OffsetDateTime creadoEn
) {}
