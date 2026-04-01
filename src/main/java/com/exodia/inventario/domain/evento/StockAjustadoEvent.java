package com.exodia.inventario.domain.evento;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Evento publicado cuando se aplica un ajuste de inventario.
 */
public record StockAjustadoEvent(
        Long ajusteId,
        Long empresaId,
        Long contenedorId,
        Long productoId,
        Long bodegaId,
        BigDecimal cantidadAnterior,
        BigDecimal cantidadNueva,
        OffsetDateTime timestamp
) {
    public StockAjustadoEvent(Long ajusteId, Long empresaId, Long contenedorId,
                              Long productoId, Long bodegaId,
                              BigDecimal cantidadAnterior, BigDecimal cantidadNueva) {
        this(ajusteId, empresaId, contenedorId, productoId, bodegaId,
                cantidadAnterior, cantidadNueva, OffsetDateTime.now());
    }
}
