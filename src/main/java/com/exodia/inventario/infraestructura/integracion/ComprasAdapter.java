package com.exodia.inventario.infraestructura.integracion;

import java.math.BigDecimal;
import java.util.List;

/**
 * Puerto ACL para validar recepciones contra ordenes de compra.
 */
public interface ComprasAdapter {

    void validarRecepcionOrdenCompra(Long empresaId,
                                     Long ordenCompraId,
                                     List<LineaRecepcionCompra> lineas);

    record LineaRecepcionCompra(
            Long productoId,
            Long unidadId,
            BigDecimal cantidad,
            BigDecimal precioUnitario
    ) {}
}
