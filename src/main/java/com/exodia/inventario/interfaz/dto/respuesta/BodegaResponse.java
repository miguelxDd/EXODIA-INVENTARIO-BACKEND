package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de una bodega")
public record BodegaResponse(
    @Schema(description = "ID de la bodega") Long id,
    @Schema(description = "Codigo de la bodega") String codigo,
    @Schema(description = "Nombre de la bodega") String nombre,
    @Schema(description = "Direccion") String direccion,
    @Schema(description = "Ciudad") String ciudad,
    @Schema(description = "Pais") String pais,
    @Schema(description = "Es de producto terminado") Boolean esProductoTerminado,
    @Schema(description = "Es de consignacion") Boolean esConsignacion,
    @Schema(description = "ID de la ubicacion standby") Long ubicacionStandbyId
) {}
