package com.exodia.inventario.infraestructura.integracion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Stub de ProductoAdapter. Asume que todos los productos existen.
 * Sera reemplazado por una implementacion REST cuando el servicio de catalogos este disponible.
 */
@Component
@Slf4j
public class ProductoAdapterStub implements ProductoAdapter {

    @Override
    public Optional<ProductoInfo> obtenerProducto(Long empresaId, Long productoId) {
        log.debug("Stub: obtenerProducto empresaId={}, productoId={}", empresaId, productoId);
        return Optional.of(new ProductoInfo(
                productoId,
                "PROD-" + productoId,
                "Producto " + productoId,
                "UND"));
    }

    @Override
    public boolean existeProducto(Long empresaId, Long productoId) {
        return true;
    }
}
