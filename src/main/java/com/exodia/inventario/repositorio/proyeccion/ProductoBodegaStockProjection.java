package com.exodia.inventario.repositorio.proyeccion;

import java.math.BigDecimal;

public interface ProductoBodegaStockProjection {

    Long getProductoId();

    Long getBodegaId();

    Long getUnidadId();

    BigDecimal getStockCantidad();
}
