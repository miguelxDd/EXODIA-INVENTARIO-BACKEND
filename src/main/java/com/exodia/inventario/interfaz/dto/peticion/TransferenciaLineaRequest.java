package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Linea de transferencia")
public record TransferenciaLineaRequest(
    @NotNull @Schema(description = "ID del producto") Long productoId,
    @NotNull @Schema(description = "ID de la unidad") Long unidadId,
    @NotNull @Positive @Schema(description = "Cantidad solicitada") BigDecimal cantidadSolicitada,
    @Schema(description = "ID del contenedor especifico (solo para POR_CONTENEDOR)") Long contenedorId
) {}
