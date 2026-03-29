package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear una unidad de medida")
public record CrearUnidadRequest(
    @NotBlank @Size(max = 20) @Schema(description = "Codigo unico de la unidad") String codigo,
    @NotBlank @Size(max = 100) @Schema(description = "Nombre de la unidad") String nombre,
    @Size(max = 10) @Schema(description = "Abreviatura") String abreviatura
) {}
