package com.exodia.inventario.interfaz.dto.respuesta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Auxiliar valorizado de inventario por producto")
public record AuxiliarInventarioResponse(
    @Schema(description = "ID de la empresa") Long empresaId,
    @Schema(description = "ID del producto") Long productoId,
    @Schema(description = "ID de la bodega") Long bodegaId,
    @Schema(description = "Fecha desde aplicada") OffsetDateTime fechaDesde,
    @Schema(description = "Fecha hasta aplicada") OffsetDateTime fechaHasta,
    @Schema(description = "Saldo inicial en cantidad") BigDecimal saldoInicialCantidad,
    @Schema(description = "Saldo inicial en valor") BigDecimal saldoInicialValor,
    @Schema(description = "Total de entradas del periodo") BigDecimal totalEntradas,
    @Schema(description = "Total de salidas del periodo") BigDecimal totalSalidas,
    @Schema(description = "Saldo final en cantidad") BigDecimal saldoFinalCantidad,
    @Schema(description = "Saldo final en valor") BigDecimal saldoFinalValor,
    @Schema(description = "Movimientos valorizados") List<AuxiliarInventarioMovimientoResponse> movimientos
) {}
