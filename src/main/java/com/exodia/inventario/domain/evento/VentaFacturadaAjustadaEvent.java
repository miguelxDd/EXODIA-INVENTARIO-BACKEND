package com.exodia.inventario.domain.evento;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Evento publicado cuando se descuenta inventario contra una venta ya facturada.
 */
public record VentaFacturadaAjustadaEvent(
        Long ajusteId,
        Long empresaId,
        Long bodegaId,
        Long ventaId,
        int lineasProcesadas,
        List<Long> contenedorIds,
        OffsetDateTime timestamp
) {
    public VentaFacturadaAjustadaEvent(Long ajusteId,
                                       Long empresaId,
                                       Long bodegaId,
                                       Long ventaId,
                                       int lineasProcesadas,
                                       List<Long> contenedorIds) {
        this(ajusteId, empresaId, bodegaId, ventaId, lineasProcesadas,
                contenedorIds, OffsetDateTime.now());
    }
}
