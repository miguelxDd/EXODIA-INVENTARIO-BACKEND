package com.exodia.inventario.domain.servicio;

import java.math.BigDecimal;

/**
 * Servicio de dominio puro (sin Spring) para calculos de stock.
 * Stock disponible = stock total - reservas pendientes.
 */
public class CalculadorStock {

    /**
     * Calcula el stock disponible de un contenedor.
     *
     * @param stockTotal     suma de operaciones activas
     * @param cantidadReservada suma de reservas pendientes
     * @return stock disponible (puede ser cero, nunca negativo en datos consistentes)
     */
    public BigDecimal calcularStockDisponible(BigDecimal stockTotal, BigDecimal cantidadReservada) {
        if (stockTotal == null) {
            stockTotal = BigDecimal.ZERO;
        }
        if (cantidadReservada == null) {
            cantidadReservada = BigDecimal.ZERO;
        }
        return stockTotal.subtract(cantidadReservada);
    }

    /**
     * Verifica si hay stock suficiente para una deduccion.
     *
     * @param stockDisponible stock disponible actual
     * @param cantidadSolicitada cantidad que se desea deducir
     * @return true si hay stock suficiente
     */
    public boolean hayStockSuficiente(BigDecimal stockDisponible, BigDecimal cantidadSolicitada) {
        if (stockDisponible == null || cantidadSolicitada == null) {
            return false;
        }
        return stockDisponible.compareTo(cantidadSolicitada) >= 0;
    }

    /**
     * Calcula la cantidad con signo segun el tipo de operacion.
     * Operaciones de entrada: cantidad positiva. Salida: negativa. Informativa: cero.
     *
     * @param cantidad cantidad base (siempre positiva)
     * @param signo signo del tipo de operacion: 1, -1, o 0
     * @return cantidad con signo aplicado
     */
    public BigDecimal aplicarSigno(BigDecimal cantidad, int signo) {
        if (cantidad == null) {
            throw new IllegalArgumentException("La cantidad no puede ser nula");
        }
        if (signo == 0) {
            return BigDecimal.ZERO;
        }
        return signo < 0 ? cantidad.negate() : cantidad;
    }
}
