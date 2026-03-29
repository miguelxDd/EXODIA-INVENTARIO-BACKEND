package com.exodia.inventario.domain.evento;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Evento publicado cuando se aplican los resultados de un conteo fisico.
 */
public record ConteoAplicadoEvent(
        Long conteoId,
        Long empresaId,
        Long bodegaId,
        List<Long> ajusteIds,
        int ajustesPositivos,
        int ajustesNegativos,
        OffsetDateTime timestamp
) {
    public ConteoAplicadoEvent(Long conteoId, Long empresaId, Long bodegaId,
                               List<Long> ajusteIds, int ajustesPositivos,
                               int ajustesNegativos) {
        this(conteoId, empresaId, bodegaId, ajusteIds, ajustesPositivos,
                ajustesNegativos, OffsetDateTime.now());
    }
}
