package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para actualizar una bodega")
public record ActualizarBodegaRequest(
    @Size(max = 200) @Schema(description = "Nombre de la bodega") String nombre,
    @Size(max = 500) @Schema(description = "Direccion de la bodega") String direccion,
    @Size(max = 100) @Schema(description = "Ciudad") String ciudad,
    @Size(max = 100) @Schema(description = "Pais") String pais,
    @Schema(description = "Es bodega de producto terminado") Boolean esProductoTerminado,
    @Schema(description = "Es bodega de consignacion") Boolean esConsignacion
) {}
