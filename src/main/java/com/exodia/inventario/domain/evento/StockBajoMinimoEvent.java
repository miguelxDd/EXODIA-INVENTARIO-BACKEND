package com.exodia.inventario.domain.evento;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Evento publicado cuando el stock de un producto cae por debajo del minimo configurado.
 */
public record StockBajoMinimoEvent(
        Long empresaId,
        Long productoId,
        Long bodegaId,
        BigDecimal stockActual,
        BigDecimal minimo,
        OffsetDateTime timestamp
) {
    public StockBajoMinimoEvent(Long empresaId, Long productoId, Long bodegaId,
                                BigDecimal stockActual, BigDecimal minimo) {
        this(empresaId, productoId, bodegaId, stockActual, minimo, OffsetDateTime.now());
    }
}
