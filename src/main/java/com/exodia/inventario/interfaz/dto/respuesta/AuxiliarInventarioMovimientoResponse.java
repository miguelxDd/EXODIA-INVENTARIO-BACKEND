package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Movimiento valorizado dentro del auxiliar de inventario")
public record AuxiliarInventarioMovimientoResponse(
    @Schema(description = "ID de la operacion") Long operacionId,
    @Schema(description = "Fecha de la operacion") OffsetDateTime fechaOperacion,
    @Schema(description = "ID del contenedor") Long contenedorId,
    @Schema(description = "Codigo de barras") String codigoBarras,
    @Schema(description = "Codigo del tipo de operacion") String tipoOperacionCodigo,
    @Schema(description = "Tipo de referencia") String tipoReferencia,
    @Schema(description = "ID de referencia") Long referenciaId,
    @Schema(description = "Cantidad con signo") BigDecimal cantidad,
    @Schema(description = "Precio unitario historico") BigDecimal precioUnitario,
    @Schema(description = "Valor del movimiento") BigDecimal valorMovimiento,
    @Schema(description = "Saldo acumulado en cantidad") BigDecimal saldoCantidad,
    @Schema(description = "Saldo acumulado en valor") BigDecimal saldoValor,
    @Schema(description = "Comentarios") String comentarios
) {}
