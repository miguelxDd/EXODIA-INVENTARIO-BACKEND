package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Peticion para actualizar configuracion de producto")
public record ActualizarConfiguracionProductoRequest(
    @Schema(description = "Maneja lote") Boolean manejaLote,
    @Schema(description = "Maneja vencimiento") Boolean manejaVencimiento,
    @Schema(description = "Tolerancia de merma") BigDecimal toleranciaMerma,
    @Schema(description = "ID de la unidad base") Long unidadBaseId
) {}
