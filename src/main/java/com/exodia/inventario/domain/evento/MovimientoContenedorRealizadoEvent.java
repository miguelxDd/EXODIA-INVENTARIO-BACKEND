package com.exodia.inventario.domain.evento;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Evento publicado cuando un contenedor cambia de ubicacion.
 */
public record MovimientoContenedorRealizadoEvent(
        Long empresaId,
        Long contenedorId,
        Long productoId,
        Long bodegaOrigenId,
        Long ubicacionOrigenId,
        Long bodegaDestinoId,
        Long ubicacionDestinoId,
        BigDecimal cantidadMovida,
        OffsetDateTime timestamp
) {
    public MovimientoContenedorRealizadoEvent(Long empresaId, Long contenedorId,
                                              Long productoId, Long bodegaOrigenId,
                                              Long ubicacionOrigenId, Long bodegaDestinoId,
                                              Long ubicacionDestinoId, BigDecimal cantidadMovida) {
        this(empresaId, contenedorId, productoId, bodegaOrigenId, ubicacionOrigenId,
                bodegaDestinoId, ubicacionDestinoId, cantidadMovida, OffsetDateTime.now());
    }
}
