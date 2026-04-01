package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Datos para crear una conversion de unidad")
public record CrearConversionUnidadRequest(
    @NotNull @Schema(description = "ID de la unidad de origen") Long unidadOrigenId,
    @NotNull @Schema(description = "ID de la unidad de destino") Long unidadDestinoId,
    @NotNull @Positive @Schema(description = "Factor de conversion") BigDecimal factorConversion,
    @NotNull @Schema(description = "Tipo de operacion: MULTIPLICAR o DIVIDIR") String tipoOperacion,
    @Schema(description = "ID del producto (opcional, para conversion especifica)") Long productoId
) {}
