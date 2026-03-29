package com.exodia.inventario.interfaz.dto.peticion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Datos para recibir una transferencia")
public record RecibirTransferenciaRequest(
    @NotNull @Schema(description = "ID de la ubicacion destino en la bodega destino") Long ubicacionDestinoId,
    @NotEmpty @Valid @Schema(description = "Contenedores a recibir") List<RecepcionContenedorRequest> contenedores
) {
    @Schema(description = "Contenedor a recibir en la transferencia")
    public record RecepcionContenedorRequest(
        @NotNull @Schema(description = "ID del contenedor a recibir") Long contenedorId,
        @Schema(description = "Cantidad recibida (si difiere de la despachada)") java.math.BigDecimal cantidadRecibida
    ) {}
}
