package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Datos para crear una transferencia entre bodegas")
public record CrearTransferenciaRequest(
    @NotNull @Schema(description = "ID de la bodega origen") Long bodegaOrigenId,
    @NotNull @Schema(description = "ID de la bodega destino") Long bodegaDestinoId,
    @NotBlank @Size(max = 30) @Schema(description = "Tipo: POR_CONTENEDOR o POR_PRODUCTO") String tipoTransferencia,
    @Size(max = 2000) @Schema(description = "Comentarios") String comentarios,
    @NotEmpty @Valid @Schema(description = "Lineas de la transferencia") List<TransferenciaLineaRequest> lineas
) {}
