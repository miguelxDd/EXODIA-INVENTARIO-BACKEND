package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Peticion para ajustar inventario por venta facturada")
public record CrearAjusteVentaRequest(
    @Schema(description = "ID de la bodega origen") @NotNull Long bodegaId,
    @Schema(description = "ID de la venta/factura origen") @NotNull Long ventaId,
    @Schema(description = "Comentarios del ajuste") @Size(max = 2000) String comentarios,
    @Schema(description = "Lineas del ajuste") @NotEmpty @Valid List<AjusteVentaLineaRequest> lineas
) {}
