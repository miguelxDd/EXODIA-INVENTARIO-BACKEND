package com.exodia.inventario.infraestructura.integracion;

import java.math.BigDecimal;
import java.util.List;

/**
 * Puerto ACL para validar picks y salidas ligadas a ventas.
 */
public interface VentasAdapter {

    void validarPedidoDespachable(Long empresaId,
                                  Long pedidoId,
                                  List<LineaVentaSolicitud> lineas);

    void validarVentaFacturada(Long empresaId,
                               Long ventaId,
                               List<LineaVentaSolicitud> lineas);

    record LineaVentaSolicitud(
            Long productoId,
            Long unidadId,
            BigDecimal cantidadSolicitada
    ) {}
}
