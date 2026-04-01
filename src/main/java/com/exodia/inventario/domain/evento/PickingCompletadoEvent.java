package com.exodia.inventario.domain.evento;

import java.time.OffsetDateTime;

/**
 * Evento publicado cuando se completa una orden de picking.
 */
public record PickingCompletadoEvent(
        Long ordenPickingId,
        Long empresaId,
        Long bodegaId,
        int lineasProcesadas,
        OffsetDateTime timestamp
) {
    public PickingCompletadoEvent(Long ordenPickingId, Long empresaId,
                                 Long bodegaId, int lineasProcesadas) {
        this(ordenPickingId, empresaId, bodegaId, lineasProcesadas, OffsetDateTime.now());
    }
}
