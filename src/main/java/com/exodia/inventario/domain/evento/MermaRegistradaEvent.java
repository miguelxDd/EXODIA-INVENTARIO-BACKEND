package com.exodia.inventario.domain.evento;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Evento publicado cuando se registra una merma sobre un contenedor.
 */
public record MermaRegistradaEvent(
        Long mermaId,
        Long empresaId,
        Long contenedorId,
        Long productoId,
        Long bodegaId,
        BigDecimal cantidadMerma,
        String tipoMerma,
        OffsetDateTime timestamp
) {
    public MermaRegistradaEvent(Long mermaId, Long empresaId, Long contenedorId,
                                Long productoId, Long bodegaId,
                                BigDecimal cantidadMerma, String tipoMerma) {
        this(mermaId, empresaId, contenedorId, productoId, bodegaId,
                cantidadMerma, tipoMerma, OffsetDateTime.now());
    }
}
