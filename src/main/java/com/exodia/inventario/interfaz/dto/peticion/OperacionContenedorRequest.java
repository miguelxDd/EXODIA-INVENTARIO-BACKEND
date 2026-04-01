package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Peticion simple para operaciones sobre contenedores")
public record OperacionContenedorRequest(
        @Size(max = 500)
        @Schema(description = "Comentarios de la operacion")
        String comentarios
) {}
