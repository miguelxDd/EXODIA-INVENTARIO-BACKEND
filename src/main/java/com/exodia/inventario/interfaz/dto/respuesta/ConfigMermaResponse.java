package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Respuesta de configuracion de merma")
public record ConfigMermaResponse(
    @Schema(description = "ID") Long id,
    @Schema(description = "ID del producto") Long productoId,
    @Schema(description = "ID de la bodega") Long bodegaId,
    @Schema(description = "Tipo de merma") String tipoMerma,
    @Schema(description = "Porcentaje de merma") BigDecimal porcentajeMerma,
    @Schema(description = "Cantidad fija de merma") BigDecimal cantidadFijaMerma,
    @Schema(description = "Frecuencia en dias") Integer frecuenciaDias,
    @Schema(description = "Activo") Boolean activo,
    @Schema(description = "Fecha de creacion") OffsetDateTime creadoEn
) {}
