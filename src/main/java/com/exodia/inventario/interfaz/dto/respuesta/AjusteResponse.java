package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Datos de un ajuste de inventario")
public record AjusteResponse(
    @Schema(description = "ID del ajuste") Long id,
    @Schema(description = "Numero de ajuste") String numeroAjuste,
    @Schema(description = "ID de la bodega") Long bodegaId,
    @Schema(description = "Codigo del tipo de ajuste") String tipoAjusteCodigo,
    @Schema(description = "Nombre del tipo de ajuste") String tipoAjusteNombre,
    @Schema(description = "Motivo del ajuste") String motivo,
    @Schema(description = "Estado") String estado,
    @Schema(description = "Lineas del ajuste") List<AjusteLineaResponse> lineas,
    @Schema(description = "Fecha de creacion") OffsetDateTime creadoEn
) {}
