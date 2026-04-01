package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Respuesta de configuracion maximo/minimo")
public record MaximoMinimoResponse(
    @Schema(description = "ID") Long id,
    @Schema(description = "ID del producto") Long productoId,
    @Schema(description = "ID de la bodega") Long bodegaId,
    @Schema(description = "ID de la unidad") Long unidadId,
    @Schema(description = "Stock minimo") BigDecimal stockMinimo,
    @Schema(description = "Stock maximo") BigDecimal stockMaximo,
    @Schema(description = "Punto de reorden") BigDecimal puntoReorden,
    @Schema(description = "Stock actual calculado") BigDecimal stockActualCalculado,
    @Schema(description = "Ultima verificacion") OffsetDateTime ultimaVerificacion,
    @Schema(description = "Activo") Boolean activo
) {}
