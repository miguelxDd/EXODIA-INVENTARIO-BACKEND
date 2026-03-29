package com.exodia.inventario.infraestructura.integracion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stub de ContabilidadAdapter. Solo registra en log.
 * Sera reemplazado por implementación REST/Kafka cuando contabilidad este disponible.
 */
@Component
@Slf4j
public class ContabilidadAdapterStub implements ContabilidadAdapter {

    @Override
    public void notificarMovimientoInventario(MovimientoContable movimiento) {
        log.info("Stub contabilidad: tipo={}, empresa={}, ref={}, monto={}",
                movimiento.tipoMovimiento(), movimiento.empresaId(),
                movimiento.referenciaId(), movimiento.monto());
    }
}
