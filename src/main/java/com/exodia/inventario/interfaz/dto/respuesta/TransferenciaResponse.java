package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Datos de una transferencia")
public record TransferenciaResponse(
    @Schema(description = "ID de la transferencia") Long id,
    @Schema(description = "Numero de transferencia") String numeroTransferencia,
    @Schema(description = "Tipo de transferencia") String tipoTransferencia,
    @Schema(description = "ID de la bodega origen") Long bodegaOrigenId,
    @Schema(description = "Codigo de la bodega origen") String bodegaOrigenCodigo,
    @Schema(description = "ID de la bodega destino") Long bodegaDestinoId,
    @Schema(description = "Codigo de la bodega destino") String bodegaDestinoCodigo,
    @Schema(description = "Estado actual") String estadoCodigo,
    @Schema(description = "Comentarios") String comentarios,
    @Schema(description = "Fecha de despacho") OffsetDateTime fechaDespacho,
    @Schema(description = "Fecha de recepcion") OffsetDateTime fechaRecepcion,
    @Schema(description = "Lineas de la transferencia") List<TransferenciaLineaResponse> lineas,
    @Schema(description = "Contenedores asignados") List<TransferenciaContenedorResponse> contenedores,
    @Schema(description = "Fecha de creacion") OffsetDateTime creadoEn
) {}
