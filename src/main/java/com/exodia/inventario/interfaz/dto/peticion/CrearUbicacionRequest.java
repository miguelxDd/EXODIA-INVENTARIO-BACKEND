package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear una ubicacion")
public record CrearUbicacionRequest(
    @NotNull @Schema(description = "ID de la bodega") Long bodegaId,
    @NotBlank @Size(max = 50) @Schema(description = "Codigo unico de la ubicacion") String codigo,
    @NotBlank @Size(max = 200) @Schema(description = "Nombre de la ubicacion") String nombre,
    @Size(max = 100) @Schema(description = "Codigo de barras de la ubicacion") String codigoBarras,
    @Schema(description = "Tipo de ubicacion: GENERAL, STANDBY, TEMPORAL, RECEPCION, PRODUCCION") String tipoUbicacion
) {}
