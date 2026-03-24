package com.exodia.inventario.repositorio.proyeccion;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ContenedorPorVencerProjection {

    Long getContenedorId();

    String getCodigoBarras();

    Long getProductoId();

    Long getBodegaId();

    LocalDate getFechaVencimiento();

    BigDecimal getStockCantidad();

    Long getDiasRestantes();
}
