package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Respuesta de registro de merma")
public record MermaResponse(
    @Schema(description = "ID") Long id,
    @Schema(description = "ID del contenedor") Long contenedorId,
    @Schema(description = "Cantidad de merma") BigDecimal cantidadMerma,
    @Schema(description = "Tipo de merma") String tipoMerma,
    @Schema(description = "Comentarios") String comentarios,
    @Schema(description = "ID de la operacion generada") Long operacionId,
    @Schema(description = "Fecha de creacion") OffsetDateTime creadoEn
) {}
