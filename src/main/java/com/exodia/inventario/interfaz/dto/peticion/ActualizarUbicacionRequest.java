package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para actualizar una ubicacion")
public record ActualizarUbicacionRequest(
    @Size(max = 200) @Schema(description = "Nombre de la ubicacion") String nombre,
    @Size(max = 100) @Schema(description = "Codigo de barras de la ubicacion") String codigoBarras,
    @Schema(description = "Tipo de ubicacion: GENERAL, STANDBY, TEMPORAL, RECEPCION, PRODUCCION") String tipoUbicacion
) {}
