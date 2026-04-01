package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Linea de un conteo fisico")
public record ConteoLineaResponse(
    @Schema(description = "ID") Long id,
    @Schema(description = "ID del contenedor") Long contenedorId,
    @Schema(description = "Cantidad en sistema") BigDecimal cantidadSistema,
    @Schema(description = "Cantidad contada") BigDecimal cantidadContada,
    @Schema(description = "Diferencia") BigDecimal diferencia,
    @Schema(description = "Fue aplicado") Boolean aplicado
) {}
