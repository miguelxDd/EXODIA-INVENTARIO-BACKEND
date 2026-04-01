package com.exodia.inventario.domain.evento;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Evento publicado cuando se completa una orden de picking.
 */
public record PickingCompletadoEvent(
        Long ordenPickingId,
        Long empresaId,
        Long bodegaId,
        List<Long> productoIds,
        int lineasProcesadas,
        OffsetDateTime timestamp
) {
    public PickingCompletadoEvent(Long ordenPickingId, Long empresaId,
                                 Long bodegaId, List<Long> productoIds, int lineasProcesadas) {
        this(ordenPickingId, empresaId, bodegaId, productoIds,
                lineasProcesadas, OffsetDateTime.now());
    }
}
