package com.exodia.inventario.interfaz.dto.peticion;

import com.exodia.inventario.domain.enums.TipoMerma;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Peticion para crear configuracion de merma")
public record CrearConfigMermaRequest(
    @Schema(description = "ID del producto (opcional, null = aplica a todos)") Long productoId,
    @Schema(description = "ID de la bodega (opcional, null = aplica a todas)") Long bodegaId,
    @Schema(description = "Tipo de merma") @NotNull TipoMerma tipoMerma,
    @Schema(description = "Porcentaje de merma permitido") BigDecimal porcentajeMerma,
    @Schema(description = "Cantidad fija de merma permitida") BigDecimal cantidadFijaMerma,
    @Schema(description = "Frecuencia en dias para revision") Integer frecuenciaDias
) {}
