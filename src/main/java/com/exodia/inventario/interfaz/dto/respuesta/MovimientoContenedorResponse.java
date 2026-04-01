package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Resultado de un movimiento de contenedor")
public record MovimientoContenedorResponse(
        @Schema(description = "ID del contenedor") Long contenedorId,
        @Schema(description = "Codigo de barras del contenedor") String codigoBarras,
        @Schema(description = "Bodega origen") Long bodegaOrigenId,
        @Schema(description = "Ubicacion origen") Long ubicacionOrigenId,
        @Schema(description = "Bodega destino") Long bodegaDestinoId,
        @Schema(description = "Ubicacion destino") Long ubicacionDestinoId,
        @Schema(description = "Cantidad movida") BigDecimal cantidadMovida,
        @Schema(description = "Estado anterior") String estadoAnterior,
        @Schema(description = "Estado nuevo") String estadoNuevo,
        @Schema(description = "Operacion de salida") Long operacionSalidaId,
        @Schema(description = "Operacion de entrada") Long operacionEntradaId
) {}
