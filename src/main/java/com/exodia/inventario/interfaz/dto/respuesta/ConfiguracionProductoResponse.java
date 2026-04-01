package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Respuesta de configuracion de producto")
public record ConfiguracionProductoResponse(
    @Schema(description = "ID") Long id,
    @Schema(description = "ID del producto") Long productoId,
    @Schema(description = "Maneja lote") Boolean manejaLote,
    @Schema(description = "Maneja vencimiento") Boolean manejaVencimiento,
    @Schema(description = "Tolerancia de merma") BigDecimal toleranciaMerma,
    @Schema(description = "ID de la unidad base") Long unidadBaseId,
    @Schema(description = "Activo") Boolean activo,
    @Schema(description = "Fecha de modificacion") OffsetDateTime modificadoEn
) {}
