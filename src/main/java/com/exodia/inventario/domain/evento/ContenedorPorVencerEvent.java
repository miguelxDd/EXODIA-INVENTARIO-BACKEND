package com.exodia.inventario.domain.evento;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Evento publicado cuando un contenedor esta proximo a vencer.
 */
public record ContenedorPorVencerEvent(
        Long contenedorId,
        Long empresaId,
        Long productoId,
        LocalDate fechaVencimiento,
        long diasRestantes,
        OffsetDateTime timestamp
) {
    public ContenedorPorVencerEvent(Long contenedorId, Long empresaId, Long productoId,
                                   LocalDate fechaVencimiento, long diasRestantes) {
        this(contenedorId, empresaId, productoId, fechaVencimiento, diasRestantes,
                OffsetDateTime.now());
    }
}
