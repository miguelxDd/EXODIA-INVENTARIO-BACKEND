package com.exodia.inventario.domain.evento;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Evento publicado cuando se completa una recepcion de inventario.
 */
public record InventarioRecibidoEvent(
        Long recepcionId,
        Long empresaId,
        Long bodegaId,
        List<Long> contenedorIds,
        int totalLineas,
        OffsetDateTime timestamp
) {
    public InventarioRecibidoEvent(Long recepcionId, Long empresaId, Long bodegaId,
                                   List<Long> contenedorIds, int totalLineas) {
        this(recepcionId, empresaId, bodegaId, contenedorIds, totalLineas, OffsetDateTime.now());
    }
}
