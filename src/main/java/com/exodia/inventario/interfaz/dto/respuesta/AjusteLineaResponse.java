package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Linea de un ajuste de inventario")
public record AjusteLineaResponse(
    @Schema(description = "ID de la linea") Long id,
    @Schema(description = "ID del contenedor") Long contenedorId,
    @Schema(description = "Codigo de barras") String codigoBarras,
    @Schema(description = "Cantidad anterior") BigDecimal cantidadAnterior,
    @Schema(description = "Cantidad nueva") BigDecimal cantidadNueva,
    @Schema(description = "Cantidad de ajuste (diferencia)") BigDecimal cantidadAjuste,
    @Schema(description = "Precio anterior") BigDecimal precioAnterior,
    @Schema(description = "Precio nuevo") BigDecimal precioNuevo
) {}
