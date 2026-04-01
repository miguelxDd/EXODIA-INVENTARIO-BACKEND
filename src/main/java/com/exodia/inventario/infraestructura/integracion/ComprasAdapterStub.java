package com.exodia.inventario.infraestructura.integracion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Stub de compras para desarrollo local.
 */
@Component
@Slf4j
public class ComprasAdapterStub implements ComprasAdapter {

    @Override
    public void validarRecepcionOrdenCompra(Long empresaId,
                                            Long ordenCompraId,
                                            List<LineaRecepcionCompra> lineas) {
        log.info("Stub compras: validar recepcion empresa={}, ordenCompra={}, lineas={}",
                empresaId, ordenCompraId, lineas.size());
    }
}
