package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para actualizar una unidad de medida")
public record ActualizarUnidadRequest(
    @Size(max = 100) @Schema(description = "Nombre de la unidad") String nombre,
    @Size(max = 10) @Schema(description = "Abreviatura") String abreviatura
) {}
