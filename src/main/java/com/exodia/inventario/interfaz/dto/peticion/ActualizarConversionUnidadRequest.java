package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Datos para actualizar una conversion de unidad")
public record ActualizarConversionUnidadRequest(
    @Positive @Schema(description = "Factor de conversion") BigDecimal factorConversion,
    @Schema(description = "Tipo de operacion: MULTIPLICAR o DIVIDIR") String tipoOperacion
) {}
