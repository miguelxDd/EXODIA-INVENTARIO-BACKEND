package com.exodia.inventario.domain.evento;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Evento publicado cuando se recibe (total o parcial) una transferencia.
 */
public record TransferenciaRecibidaEvent(
        Long transferenciaId,
        Long empresaId,
        Long bodegaDestinoId,
        List<Long> contenedorIds,
        boolean recepcionCompleta,
        OffsetDateTime timestamp
) {
    public TransferenciaRecibidaEvent(Long transferenciaId, Long empresaId,
                                     Long bodegaDestinoId, List<Long> contenedorIds,
                                     boolean recepcionCompleta) {
        this(transferenciaId, empresaId, bodegaDestinoId, contenedorIds,
                recepcionCompleta, OffsetDateTime.now());
    }
}
