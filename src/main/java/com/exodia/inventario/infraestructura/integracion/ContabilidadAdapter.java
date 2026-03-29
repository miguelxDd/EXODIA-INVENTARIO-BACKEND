package com.exodia.inventario.infraestructura.integracion;

import java.math.BigDecimal;

/**
 * Puerto ACL para integración con el microservicio de contabilidad.
 * Notifica movimientos de inventario para generar asientos contables.
 */
public interface ContabilidadAdapter {

    void notificarMovimientoInventario(MovimientoContable movimiento);

    record MovimientoContable(
            Long empresaId,
            String tipoMovimiento,
            Long referenciaId,
            BigDecimal monto,
            String descripcion
    ) {}
}
