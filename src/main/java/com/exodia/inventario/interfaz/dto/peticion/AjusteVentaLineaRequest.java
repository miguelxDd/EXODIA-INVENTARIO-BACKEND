package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Linea de ajuste por venta facturada")
public record AjusteVentaLineaRequest(
    @Schema(description = "ID del producto") @NotNull Long productoId,
    @Schema(description = "ID de la unidad") @NotNull Long unidadId,
    @Schema(description = "Cantidad a descontar") @NotNull @Positive BigDecimal cantidad,
    @Schema(description = "ID del contenedor si la politica es MANUAL") Long contenedorId,
    @Schema(description = "ID de la linea de venta/factura origen") Long referenciaLineaId
) {}
