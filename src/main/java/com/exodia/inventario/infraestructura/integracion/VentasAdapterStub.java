package com.exodia.inventario.infraestructura.integracion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Stub de ventas para desarrollo local.
 */
@Component
@Slf4j
public class VentasAdapterStub implements VentasAdapter {

    @Override
    public void validarPedidoDespachable(Long empresaId,
                                         Long pedidoId,
                                         List<LineaVentaSolicitud> lineas) {
        log.info("Stub ventas: validar picking empresa={}, pedido={}, lineas={}",
                empresaId, pedidoId, lineas.size());
    }

    @Override
    public void validarVentaFacturada(Long empresaId,
                                      Long ventaId,
                                      List<LineaVentaSolicitud> lineas) {
        log.info("Stub ventas: validar ajuste por venta empresa={}, venta={}, lineas={}",
                empresaId, ventaId, lineas.size());
    }
}
