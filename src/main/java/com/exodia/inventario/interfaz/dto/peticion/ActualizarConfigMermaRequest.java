package com.exodia.inventario.interfaz.dto.peticion;

import com.exodia.inventario.domain.enums.TipoMerma;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Peticion para actualizar configuracion de merma")
public record ActualizarConfigMermaRequest(
    @Schema(description = "Tipo de merma") TipoMerma tipoMerma,
    @Schema(description = "Porcentaje de merma permitido") BigDecimal porcentajeMerma,
    @Schema(description = "Cantidad fija de merma permitida") BigDecimal cantidadFijaMerma,
    @Schema(description = "Frecuencia en dias para revision") Integer frecuenciaDias
) {}
