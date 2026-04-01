package com.exodia.inventario.infraestructura.integracion;

import java.math.BigDecimal;
import java.util.List;

/**
 * Puerto ACL para validar flujos de inventario asociados a produccion.
 */
public interface ProduccionAdapter {

    void validarIngresoProduccion(Long empresaId,
                                  Long ordenProduccionId,
                                  List<LineaProduccion> lineas);

    void validarConsumoProduccion(Long empresaId,
                                  Long ordenProduccionId,
                                  List<LineaProduccion> lineas);

    record LineaProduccion(
            Long productoId,
            Long unidadId,
            BigDecimal cantidad
    ) {}
}
