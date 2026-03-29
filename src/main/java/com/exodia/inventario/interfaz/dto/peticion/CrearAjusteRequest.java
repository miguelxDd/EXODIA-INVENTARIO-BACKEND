package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Datos para crear un ajuste de inventario")
public record CrearAjusteRequest(
    @NotNull @Schema(description = "ID de la bodega") Long bodegaId,
    @NotBlank @Size(max = 30) @Schema(description = "Codigo del tipo de ajuste: CANTIDAD, PRECIO, CANTIDAD_PRECIO") String tipoAjusteCodigo,
    @Size(max = 2000) @Schema(description = "Motivo del ajuste") String motivo,
    @NotEmpty @Valid @Schema(description = "Lineas del ajuste") List<AjusteLineaRequest> lineas
) {}
