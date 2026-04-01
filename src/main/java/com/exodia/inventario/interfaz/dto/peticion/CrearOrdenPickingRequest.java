package com.exodia.inventario.interfaz.dto.peticion;

import com.exodia.inventario.domain.enums.TipoPicking;
import com.exodia.inventario.domain.enums.TipoReferencia;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Peticion para crear una orden de picking")
public record CrearOrdenPickingRequest(
    @Schema(description = "ID de la bodega") @NotNull Long bodegaId,
    @Schema(description = "Tipo de picking") @NotNull TipoPicking tipoPicking,
    @Schema(description = "Tipo de referencia") TipoReferencia tipoReferencia,
    @Schema(description = "ID de referencia") Long referenciaId,
    @Schema(description = "Comentarios") String comentarios,
    @Schema(description = "Lineas de picking") @NotEmpty @Valid List<PickingLineaRequest> lineas
) {}
