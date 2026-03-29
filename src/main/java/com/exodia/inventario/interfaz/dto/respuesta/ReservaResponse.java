package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Respuesta de reserva de stock")
public record ReservaResponse(
    @Schema(description = "ID") Long id,
    @Schema(description = "ID del contenedor") Long contenedorId,
    @Schema(description = "Codigo de barras") String codigoBarras,
    @Schema(description = "ID del producto") Long productoId,
    @Schema(description = "ID de la bodega") Long bodegaId,
    @Schema(description = "Cantidad reservada") BigDecimal cantidadReservada,
    @Schema(description = "Cantidad cumplida") BigDecimal cantidadCumplida,
    @Schema(description = "Cantidad pendiente") BigDecimal cantidadPendiente,
    @Schema(description = "Estado") String estado,
    @Schema(description = "Tipo de referencia") String tipoReferencia,
    @Schema(description = "ID de referencia") Long referenciaId,
    @Schema(description = "Fecha de expiracion") OffsetDateTime fechaExpiracion,
    @Schema(description = "Fecha de creacion") OffsetDateTime creadoEn
) {}
