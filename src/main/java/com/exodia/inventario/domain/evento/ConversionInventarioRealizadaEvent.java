package com.exodia.inventario.domain.evento;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Evento publicado cuando se convierte inventario de una unidad a otra.
 */
public record ConversionInventarioRealizadaEvent(
        Long empresaId,
        Long contenedorOrigenId,
        Long contenedorDestinoId,
        Long productoId,
        Long unidadOrigenId,
        Long unidadDestinoId,
        BigDecimal cantidadOrigen,
        BigDecimal cantidadDestino,
        boolean conversionTotal,
        OffsetDateTime timestamp
) {
    public ConversionInventarioRealizadaEvent(Long empresaId, Long contenedorOrigenId,
                                              Long contenedorDestinoId, Long productoId,
                                              Long unidadOrigenId, Long unidadDestinoId,
                                              BigDecimal cantidadOrigen, BigDecimal cantidadDestino,
                                              boolean conversionTotal) {
        this(empresaId, contenedorOrigenId, contenedorDestinoId, productoId,
                unidadOrigenId, unidadDestinoId, cantidadOrigen, cantidadDestino,
                conversionTotal, OffsetDateTime.now());
    }
}
