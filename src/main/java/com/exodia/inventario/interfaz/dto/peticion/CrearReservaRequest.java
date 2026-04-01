package com.exodia.inventario.interfaz.dto.peticion;

import com.exodia.inventario.domain.enums.TipoReferencia;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Peticion para crear una reserva de stock")
public record CrearReservaRequest(
    @Schema(description = "ID del contenedor") @NotNull Long contenedorId,
    @Schema(description = "Cantidad a reservar") @NotNull @Positive BigDecimal cantidadReservada,
    @Schema(description = "Tipo de referencia") @NotNull TipoReferencia tipoReferencia,
    @Schema(description = "ID de referencia") @NotNull Long referenciaId,
    @Schema(description = "ID de linea de referencia") Long referenciaLineaId,
    @Schema(description = "Fecha de expiracion de la reserva") OffsetDateTime fechaExpiracion
) {}
