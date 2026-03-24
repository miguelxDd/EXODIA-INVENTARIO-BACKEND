package com.exodia.inventario.repositorio.proyeccion;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ContenedorStockProjection {

    Long getContenedorId();

    String getCodigoBarras();

    Long getProductoId();

    Long getProveedorId();

    Long getUnidadId();

    Long getBodegaId();

    Long getUbicacionId();

    BigDecimal getPrecioUnitario();

    String getNumeroLote();

    LocalDate getFechaVencimiento();

    String getEstadoCodigo();

    BigDecimal getStockCantidad();

    BigDecimal getCantidadReservada();

    default BigDecimal getCantidadDisponible() {
        BigDecimal stock = getStockCantidad() != null ? getStockCantidad() : BigDecimal.ZERO;
        BigDecimal reservada = getCantidadReservada() != null ? getCantidadReservada() : BigDecimal.ZERO;
        return stock.subtract(reservada);
    }
}
