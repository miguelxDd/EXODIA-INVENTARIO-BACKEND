package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Resultado de una conversion de inventario")
public record ConversionInventarioResponse(
        @Schema(description = "Contenedor origen") Long contenedorOrigenId,
        @Schema(description = "Barcode origen") String codigoBarrasOrigen,
        @Schema(description = "Contenedor destino") Long contenedorDestinoId,
        @Schema(description = "Barcode destino") String codigoBarrasDestino,
        @Schema(description = "Unidad origen") Long unidadOrigenId,
        @Schema(description = "Unidad destino") Long unidadDestinoId,
        @Schema(description = "Cantidad convertida en origen") BigDecimal cantidadOrigen,
        @Schema(description = "Cantidad resultante en destino") BigDecimal cantidadDestino,
        @Schema(description = "Precio unitario origen") BigDecimal precioUnitarioOrigen,
        @Schema(description = "Precio unitario destino") BigDecimal precioUnitarioDestino,
        @Schema(description = "Indica si la conversion fue total") boolean conversionTotal,
        @Schema(description = "Operacion de salida") Long operacionSalidaId,
        @Schema(description = "Operacion de entrada") Long operacionEntradaId
) {}
