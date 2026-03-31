package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Peticion para crear configuracion de producto")
public record CrearConfiguracionProductoRequest(
    @Schema(description = "ID del producto") @NotNull Long productoId,
    @Schema(description = "Maneja lote") Boolean manejaLote,
    @Schema(description = "Maneja vencimiento") Boolean manejaVencimiento,
    @Schema(description = "Tolerancia de merma") BigDecimal toleranciaMerma,
    @Schema(description = "ID de la unidad base") Long unidadBaseId
) {}
