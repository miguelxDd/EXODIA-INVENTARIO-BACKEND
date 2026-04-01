package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Schema(description = "Operacion del kardex")
public record OperacionResponse(
    @Schema(description = "ID de la operacion") Long id,
    @Schema(description = "ID del contenedor") Long contenedorId,
    @Schema(description = "Codigo de barras") String codigoBarras,
    @Schema(description = "ID del producto") Long productoId,
    @Schema(description = "ID de la bodega") Long bodegaId,
    @Schema(description = "ID de la ubicacion") Long ubicacionId,
    @Schema(description = "ID de la unidad") Long unidadId,
    @Schema(description = "Codigo del tipo de operacion") String tipoOperacionCodigo,
    @Schema(description = "Cantidad (con signo)") BigDecimal cantidad,
    @Schema(description = "Precio unitario") BigDecimal precioUnitario,
    @Schema(description = "Numero de lote") String numeroLote,
    @Schema(description = "Fecha de vencimiento") LocalDate fechaVencimiento,
    @Schema(description = "Tipo de referencia") String tipoReferencia,
    @Schema(description = "ID de la referencia") Long referenciaId,
    @Schema(description = "Comentarios") String comentarios,
    @Schema(description = "Fecha de la operacion") OffsetDateTime fechaOperacion
) {}
