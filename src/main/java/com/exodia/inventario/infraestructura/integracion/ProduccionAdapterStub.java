package com.exodia.inventario.infraestructura.integracion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Stub de produccion para desarrollo local.
 */
@Component
@Slf4j
public class ProduccionAdapterStub implements ProduccionAdapter {

    @Override
    public void validarIngresoProduccion(Long empresaId,
                                         Long ordenProduccionId,
                                         List<LineaProduccion> lineas) {
        log.info("Stub produccion: validar ingreso empresa={}, ordenProduccion={}, lineas={}",
                empresaId, ordenProduccionId, lineas.size());
    }

    @Override
    public void validarConsumoProduccion(Long empresaId,
                                         Long ordenProduccionId,
                                         List<LineaProduccion> lineas) {
        log.info("Stub produccion: validar consumo empresa={}, ordenProduccion={}, lineas={}",
                empresaId, ordenProduccionId, lineas.size());
    }
}
