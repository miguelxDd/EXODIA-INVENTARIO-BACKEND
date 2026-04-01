package com.exodia.inventario.infraestructura.integracion;

import java.util.Optional;

/**
 * Puerto ACL (Anti-Corruption Layer) para consultar productos del microservicio de catálogos.
 * La implementación real se conectará via REST/gRPC al servicio de productos.
 * Por ahora se usa un stub que asume que los productos existen.
 */
public interface ProductoAdapter {

    Optional<ProductoInfo> obtenerProducto(Long empresaId, Long productoId);

    boolean existeProducto(Long empresaId, Long productoId);

    record ProductoInfo(
            Long id,
            String codigo,
            String nombre,
            String unidadBaseCodigo
    ) {}
}
