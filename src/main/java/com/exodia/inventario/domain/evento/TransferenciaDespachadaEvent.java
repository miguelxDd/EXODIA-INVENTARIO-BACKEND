package com.exodia.inventario.domain.evento;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Evento publicado cuando se despacha una transferencia.
 */
public record TransferenciaDespachadaEvent(
        Long transferenciaId,
        Long empresaId,
        Long bodegaOrigenId,
        Long bodegaDestinoId,
        List<Long> contenedorIds,
        OffsetDateTime timestamp
) {
    public TransferenciaDespachadaEvent(Long transferenciaId, Long empresaId,
                                       Long bodegaOrigenId, Long bodegaDestinoId,
                                       List<Long> contenedorIds) {
        this(transferenciaId, empresaId, bodegaOrigenId, bodegaDestinoId,
                contenedorIds, OffsetDateTime.now());
    }
}
