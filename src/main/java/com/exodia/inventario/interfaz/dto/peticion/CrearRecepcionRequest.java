package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Datos para crear una recepcion de inventario")
public record CrearRecepcionRequest(
    @NotNull @Schema(description = "ID de la bodega destino") Long bodegaId,
    @NotBlank @Size(max = 30) @Schema(description = "Tipo de recepcion: MANUAL, ORDEN_COMPRA, TRANSFERENCIA, PRODUCCION, DEVOLUCION") String tipoRecepcion,
    @Schema(description = "ID de la referencia de origen (orden de compra, transferencia, etc.)") Long referenciaOrigenId,
    @Schema(description = "ID del proveedor") Long proveedorId,
    @Size(max = 2000) @Schema(description = "Comentarios") String comentarios,
    @NotEmpty @Valid @Schema(description = "Lineas de la recepcion") List<RecepcionLineaRequest> lineas
) {}
