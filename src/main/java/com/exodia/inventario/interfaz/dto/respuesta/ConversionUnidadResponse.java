package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Datos de una conversion de unidad")
public record ConversionUnidadResponse(
    @Schema(description = "ID de la conversion") Long id,
    @Schema(description = "ID de la unidad de origen") Long unidadOrigenId,
    @Schema(description = "Codigo de la unidad de origen") String unidadOrigenCodigo,
    @Schema(description = "ID de la unidad de destino") Long unidadDestinoId,
    @Schema(description = "Codigo de la unidad de destino") String unidadDestinoCodigo,
    @Schema(description = "Factor de conversion") BigDecimal factorConversion,
    @Schema(description = "Tipo de operacion") String tipoOperacion,
    @Schema(description = "ID del producto") Long productoId
) {}
